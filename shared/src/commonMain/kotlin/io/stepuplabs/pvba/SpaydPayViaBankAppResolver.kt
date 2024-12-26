@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.stepuplabs.pvba

/**
 * This is the main entry point for paying directly via a bank app supporting SPAYD format.
 */
expect class SpaydPayViaBankAppResolver {
    /**
     * Shows whether the device supports Pay via Bank App and if the feature should be visible.
     */
    fun isPayViaBankAppSupported(): Boolean

    /**
     * Opens a supported bank app with a payment flow or shows a chooser with multiple apps.
     */
    fun payViaBankApp(spayd: String, navigationParams: NavigationParameters): Result<Unit>
}

/**
 * This class should contain any platform-specific parameters which the system needs to launch navigation to the bank app or show a chooser.
 */
expect class NavigationParameters
