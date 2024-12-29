# Pay via bank app

This library allows your app users to pay directly via their bank app. You can implement this into your app and save money on transaction fees to card companies. It's also very convenient for users. It's a Kotlin Multiplatform library, targeting both Android & iOS.

The library was created with a significant contribution from Alza.cz a.s. It's also used by [Settle Up](https://settleup.io/) app.

## Screenshots

<img src="doc/android_screenshot_1.png" width="200" /> <img src="doc/android_screenshot_2.png" width="200" />
TODO @c4t-dr34m

## Bank support

Currently, we support only banks that accept the [SPAYD format](https://en.wikipedia.org/wiki/Short_Payment_Descriptor), known as "QR payment" in the Czech Republic. The list of tested bank apps can be found [here](shared/src/androidMain/kotlin/io/stepuplabs/pvba/SpaydBankAppPaymentResolver.android.kt#L191). We welcome pull requests about new bank support. Or open an issue if you found something about supported bank.

## Integration

### Android

Add this to your dependencies:

`implementation 'io.stepuplabs.pvba:pvba-android:<latest-version>'`

#### Initialization

```kotlin
val payViaBankAppResolver = SpaydPayViaBankAppResolver(application, fileProviderAuthority, bankAppPackages)
```

where:
- `application` is the Application context
- `fileProviderAuthority` is the authority of your [file provider](https://developer.android.com/reference/androidx/core/content/FileProvider). If your app doesn't have one, create one similar to the [sample app](androidApp/src/androidMain).
- `bankAppPackages` is the list of supported bank app package names. It uses [this list](shared/src/androidMain/kotlin/io/stepuplabs/pvba/SpaydBankAppPaymentResolver.android.kt#L191) as default, but it's optional. However, it's recommended to pass this list dynamically via some service like Firebase Remote Config. This way, you can turn the support on/off for a specific bank without updating the app.

#### Checking device support

```kotlin
payViaBankAppResolver.isPayViaBankAppSupported()
```

This is `false` if the user doesn't have any supported bank app installed. It's recommended to hide the feature.

#### Showing supported bank app icon

You can get the icon of a supported bank app by calling:

```kotlin
payViaBankAppResolver.getSupportedBankAppIcon()
```

It's recommended to show this icon on the payment button to improve UX. If this method returns null, show some fallback icon or no icon at all.

#### Initiating payment flow

```kotlin
val result = payViaBankAppResolver.payViaBankApp(spayd, navigationParams)
```

where:
- `spayd` is the string containing the payment information in a [SPAYD format](https://en.wikipedia.org/wiki/Short_Payment_Descriptor). We recommend [our library](https://github.com/step-up-labs/spayd-kmp) for generating SPAYD on Android,
- `navigationParams` should contain a [WeakReference](https://developer.android.com/reference/java/lang/ref/WeakReference) of your Activity to avoid memory leaks,
- `result` is a [Result class](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-result/). If it's successful, the bank app has been successfully opened. Failure could be caused by two exceptions:
  - `BankAppOpeningException` is a problem with opening the bank app's Activity. You can get more details in the Exceptions' `cause`.
  - `QrCodeFileException` is a problem with providing the file via a file provider. It's likely caused by a bad file provider configuration. You can get more details in the Exceptions' `cause`.

### iOS

Add dependency using Swift Package Manager:

#### Xcode

Go to you project and select Package Dependencies, click `+` in the lower left corner and add `https://github.com/stepuplabs/bank-app-payment-poc.git` in the upper right corner of the presented dialog (marked "Search or Enter Package URL").

#### Package.swift

Add a new entry into `dependencies`: `.package(url: "https://github.com/stepuplabs/bank-app-payment-poc.git", from: "<latest-version>"),`

#### Showing user the Share sheet

Where appropriate, create an instance of `SpaydPayViaBankAppResolver` and call `.payViaBankApp()` on it.

```swift
SpaydPayViaBankAppResolver().payViaBankApp(spayd: spayd)
```

where:
- `spayd` is a string containing the payment information in the [SPAYD format](https://en.wikipedia.org/wiki/Short_Payment_Descriptor). We recommend [our library](https://github.com/step-up-labs/spayd-kmp) for generating SPAYD on iOS.

## How does it work?

### Android
On Android, the library uses intent APIs. Some bank apps can be opened directly with an intent containing the SPAYD string. Other bank apps accept images with a QR code containing SPAYD. If there is just one supported app, the QR code is not visible to the user and the bank app is opened directly. If the user has multiple supported bank apps, the system dialog is shown. The QR code is visible there and the user has to select their preferred bank app.

### iOS
On iOS, the library generates the QR code and opens a sheet to share the QR code to another app. The user can then pick the bank app that can decode the QR code and pre-fill a payment order. If the bank app does not support sharing images, the user can save the generated QR code in their photo library and open it within the bank app for the same purpose. Bank app icons is not supported on iOS due to system limitation.


## For bank apps: how to support this library?

### Android
It's preferred to accept Intent with SPAYD payload:

```xml
<receiver android:name=".YourSpaydReceiver" android:exported="true">
  <intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <data android:mimeType="application/x-shortpaymentdescriptor" />
  </intent-filter>
</receiver>
```

In your receiver, get SPAYD payload via `intent.getStringExtra("spayd")`.

### iOS
Support accepting an image with QR code containing SPAYD.

## Future plans & contributing

We would like to add support to more banks and markets, such as banks supporting the [EPC code format](https://en.wikipedia.org/wiki/EPC_QR_code).

Any contributions via pull requests are welcome.
