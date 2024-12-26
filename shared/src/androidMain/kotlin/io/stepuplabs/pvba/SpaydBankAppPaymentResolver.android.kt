@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.stepuplabs.pvba

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import io.github.g0dkar.qrcode.QRCode
import io.ktor.http.Url
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import java.lang.ref.WeakReference

/**
 * This is the main entry point for paying directly via a bank app supporting SPAYD format.
 */
actual class SpaydPayViaBankAppResolver(
    private val application: Application,
    private val fileProviderAuthority: String,
    private val bankAppPackages: List<String> = DEFAULT_BANK_APP_PACKAGES
) {
    /**
     * Shows whether the device supports Pay via Bank App and if the feature should be visible.
     */
    actual fun isPayViaBankAppSupported(): Boolean {
        return getSpaydReceivers().isNotEmpty() || getImageReceivers().isNotEmpty()
    }

    /**
     * When there is just one bank app supporting bank app payment, this returns the icon of that bank app.
     */
    fun getSupportedBankAppIcon(): Drawable? {
        val spaydReceivers = getSpaydReceivers()
        val imageReceivers = getImageReceivers()
        return if (spaydReceivers.size == 1 && imageReceivers.isEmpty()) {
            application.packageManager.getApplicationIcon(spaydReceivers.first())
        } else if (imageReceivers.size == 1 && spaydReceivers.isEmpty()) {
            application.packageManager.getApplicationIcon(imageReceivers.first())
        } else {
            null
        }
    }

    /**
     * Opens a supported bank app with a payment flow or shows a chooser with multiple apps.
     */
    actual fun payViaBankApp(
        spayd: String,
        navigationParams: NavigationParameters
    ): Result<Unit> {
        val spaydReceivers = getSpaydReceivers()
        val imageReceivers = getImageReceivers()

        val genericSpaydIntent = {
            Intent().apply {
                action = Intent.ACTION_VIEW
                putExtra("spayd", spayd)
                type = "application/x-shortpaymentdescriptor"
            }
        }

        val genericImageIntent = { qrCodeUri: Url ->
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, Uri.parse(qrCodeUri.toString()))
                type = "image/*"

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("", Uri.parse(qrCodeUri.toString()))
            }
        }

        val spaydIntents = {
            spaydReceivers.map {
                Intent(genericSpaydIntent()).apply { `package` = it }
            }
        }

        val imageIntents = { qrCodeUri: Url ->
            imageReceivers.map {
                Intent(genericImageIntent(qrCodeUri)).apply { `package` = it }
            }
        }

        try {
            when {
                // only one spayd receiver, open straight away
                spaydReceivers.size == 1 && imageReceivers.isEmpty() -> {
                    navigationParams.activityReference.get()?.startActivity(spaydIntents().first())
                }
                // only one image receiver, open straight away
                imageReceivers.size == 1 && spaydReceivers.isEmpty() -> {
                    val qrCodeUri = generateQrCodeUri(spayd)
                    navigationParams.activityReference.get()
                        ?.startActivity(imageIntents(qrCodeUri).first())
                }
                // many spayds receivers and no image receiver, show chooser with spayds prioritized (no QR preview)
                spaydReceivers.isNotEmpty() && imageReceivers.isEmpty() -> {
                    val intent = Intent.createChooser(genericSpaydIntent(), "").apply {
                        putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            spaydIntents().toTypedArray()
                        )
                    }
                    navigationParams.activityReference.get()?.startActivity(intent)
                }
                // many image receivers and no spayd receiver, show chooser with image receivers prioritized (with QR preview)
                imageReceivers.isNotEmpty() && spaydReceivers.isEmpty() -> {
                    val qrCodeUri = generateQrCodeUri(spayd)
                    val intent = Intent.createChooser(genericImageIntent(qrCodeUri), "").apply {
                        putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            imageIntents(qrCodeUri).toTypedArray()
                        )
                    }
                    navigationParams.activityReference.get()?.startActivity(intent)
                }
                // both image and spayd receivers are empty, show nondiscriminatory chooser with everything that can process image
                else -> {
                    val qrCodeUri = generateQrCodeUri(spayd)
                    val intent = Intent.createChooser(genericImageIntent(qrCodeUri), "").apply {
                        putExtra(
                            Intent.EXTRA_INITIAL_INTENTS,
                            imageIntents(qrCodeUri).plus(spaydIntents())
                                .toTypedArray(),
                        )
                    }
                    navigationParams.activityReference.get()?.startActivity(intent)
                }
            }
            return Result.success(Unit)
        } catch (e: QrCodeFileException) {
            return Result.failure(e)
        } catch (e: Exception) {
            return Result.failure(BankAppOpeningException(e))
        }
    }

    private fun getSpaydReceivers(): List<String> {
        return application.packageManager.intentReceivers(Intent(Intent.ACTION_VIEW).apply {
            type = "application/x-shortpaymentdescriptor"
        })
    }

    private fun getImageReceivers(): Set<String> {
        return application.packageManager.intentReceivers(Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
        }).intersect(bankAppPackages.toSet())
    }

    private fun generateQrCodeUri(spayd: String): Url {
        try {
            val cachePath = application.cacheDir.path

            val dir = "$cachePath${Path.DIRECTORY_SEPARATOR}$DIR".toPath(true)

            if (!FileSystem.SYSTEM.exists(dir)) {
                FileSystem.SYSTEM.createDirectories(dir)
            }

            val path = "$dir${Path.DIRECTORY_SEPARATOR}$FILE".toPath(normalize = true)

            FileSystem.SYSTEM.sink(path).use {
                val outputStream = it.buffer().outputStream()
                QRCode(spayd)
                    .render(30, 30)
                    .writeImage(outputStream)
                outputStream.flush()
            }

            return Url(
                (if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    Uri.fromFile(path.toFile())
                } else {
                    FileProvider.getUriForFile(application, fileProviderAuthority, path.toFile())
                }).toString()
            )
        } catch (e: Exception) {
            throw QrCodeFileException(e)
        }
    }

    companion object {
        val DEFAULT_BANK_APP_PACKAGES = listOf(
            "cz.rb.app.smartphonebanking",
            "eu.inmite.prj.kb.mobilbank",
            "cz.kb.ndb",
            "cz.csas.georgego",
            "cz.csob.smart",
            "cz.moneta.smartbanka",
            "cz.fio.sb2",
            "cz.airbank.android",
            "cz.creditas.richee",
            "cz.pbktechnology.partners.client"
        )
        private const val DIR = "qr"
        private const val FILE = "transfer-payment-qr.png"
    }

}

/**
 * This class should contain any platform-specific parameters which the system needs to launch navigation to the bank app or show a chooser.
 */
actual class NavigationParameters(val activityReference: WeakReference<Activity>)

/**
 * This exception is returned when there is unexpected error when opening bank app Activity.
 */
class BankAppOpeningException(override val cause: Throwable?): Exception()

/**
 * This exception is returned when there is unexpected error when creating a file for transfer QR code.
 */
class QrCodeFileException(override val cause: Throwable?): Exception()