@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.stepuplabs.pvba

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.*
import platform.CoreImage.*
import platform.Foundation.*
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreGraphics.CGSize
import platform.LinkPresentation.LPLinkMetadata
import platform.Foundation.NSItemProvider
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.UIKit.UIImagePNGRepresentation
import platform.darwin.NSObject
import objcnames.classes.LPLinkMetadata as ObjcLPLinkMetadata

/**
 * This is the main entry point for paying directly via a bank app supporting SPAYD format.
 */
actual class SpaydPayViaBankAppResolver {
    /**
     * Shows whether the device supports Pay via Bank App and if the feature should be visible.
     */
    actual fun isPayViaBankAppSupported(): Boolean = true

    /**
     * Opens a supported bank app with a payment flow or shows a chooser with multiple apps.
     */
    @OptIn(ExperimentalForeignApi::class)
    actual fun payViaBankApp(spayd: String, navigationParams: NavigationParameters): Result<Unit> {
        val qrCode = generateQRCode(spayd as NSString)
        val activityItems = listOf(generateActivityItems(qrCode = qrCode))
        val activityViewController = UIActivityViewController(
            activityItems,
            applicationActivities = null
        )

        // Assuming `UIApplication.sharedApplication.keyWindow?.rootViewController`
        // is available as the root view controller
        val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootViewController?.presentViewController(
            activityViewController,
            animated = true,
            completion = null,
        )

        return Result.success(Unit)
    }

    @ExperimentalForeignApi
    private fun generateActivityItems(
        qrCode: UIImage,
    ) = object : NSObject(), UIActivityItemSourceProtocol {
        override fun activityViewControllerPlaceholderItem(
            activityViewController: UIActivityViewController,
        ): Any {
            return UIImage()
        }

        override fun activityViewController(
            activityViewController: UIActivityViewController,
            thumbnailImageForActivityType: String?,
            suggestedSize: CValue<CGSize>
        ): UIImage {
            return qrCode
        }

        override fun activityViewController(
            activityViewController: UIActivityViewController,
            itemForActivityType: UIActivityType?,
        ): Any? {
            // Instead of sharing the UIImage, share the file URL to ensure PNG format is preserved
            return saveImageToTemporaryLocation(qrCode) ?: UIImage()
        }

        override fun activityViewControllerLinkMetadata(
            activityViewController: UIActivityViewController,
        ): ObjcLPLinkMetadata? {
            return try {
                val metadata = LPLinkMetadata()
                metadata.title = "QR Code"
                saveImageToTemporaryLocation(qrCode)?.let { tempFileUrl ->
                    val provider = NSItemProvider(tempFileUrl)
                    metadata.imageProvider = provider
                }

                metadata as? ObjcLPLinkMetadata
            } catch (e: Exception) {
                NSLog("Error in activityViewControllerLinkMetadata: ${e.message}")
                null
            }
        }

        private fun saveImageToTemporaryLocation(image: UIImage): NSURL? {
            // Save the UIImage as PNG data
            val imageData = UIImagePNGRepresentation(image)
            // The system may delete files in this directory when it needs to reclaim
            // disk space or when the app is terminated.
            val tempDirectory = NSTemporaryDirectory()
            // Generate a unique filename using UUID
            val uniqueFilename = "qr_code_${NSUUID.UUID().UUIDString}.png"
            val tempFilePath = tempDirectory + uniqueFilename
            val success = imageData?.writeToFile(tempFilePath, atomically = true) ?: false

            return if (success) NSURL.fileURLWithPath(tempFilePath) else null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun generateQRCode(text: NSString): UIImage {
        val data = text.dataUsingEncoding(NSUTF8StringEncoding)
        val qrFilter = CIFilter.filterWithName("CIQRCodeGenerator")
        qrFilter?.setDefaults()
        qrFilter?.setValue(data, forKey = "inputMessage")

        val ciImage = qrFilter?.outputImage
            ?: throw QrCodeFileException(Throwable(message = "No CIFilter to generate QR code"))

        val scaleX = 30.0
        val scaleY = 30.0

        // Apply transformation to scale the image
        val transform = CGAffineTransformMakeScale(scaleX, scaleY)
        val transformedImage = ciImage.imageByApplyingTransform(transform)

        // Convert CIImage to UIImage
        val context = CIContext.contextWithOptions(null)
        val cgImage = context.createCGImage(transformedImage, transformedImage.extent)

        return UIImage.imageWithCGImage(cgImage)
    }
}

/**
 * This class should contain any platform-specific parameters which the system needs
 * to launch navigation to the bank app or show a chooser.
 */
actual class NavigationParameters

/**
 * This exception is returned when there is unexpected error when creating a file for transfer QR code.
 */
class QrCodeFileException(override val cause: Throwable?): Exception()