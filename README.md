# Leap Mobile SDK

## Prerequisites

- **JDK 17**
- **Android SDK** (API 29+, compile/target 36)
- **Android Studio** (recommended) or command-line tools

---

## Setting up the project to download the SDK

Currently the SDK is hosted as a [GitHub Package](https://github.com/patrontech/LeapMobileSDK-Android/packages/2818095) inside the [repository](https://github.com/patrontech/LeapMobileSDK-Android).  
This means that only those with access to the repository can download the package using their git username and token.  
To generate a token, go to your GitHub settings -> Developer Settings -> Personal Access Tokens -> Tokens (classic) -> Generate new token.
You can also click here: [Generate GitHub Token](https://github.com/settings/tokens/new)
Make sure to give it at least the `read:packages` scope.  
Then, add the following to your `build.gradle.kts` file in the root of your project:

```kotlin
allprojects {
    repositories {
        ...
        // Leap Mobile SDK
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/patrontech/LeapMobileSDK-Android/")
            credentials {
                username = {{ githubUser }}
                password = {{ githubToken }}
            }
        }

        // Code Scanner library
        maven {
            url = uri("https://maven.pkg.github.com/patrontech/code-scanner/")
            credentials {
                username = {{ githubUser }}
                password = {{ githubToken }}
            }

        }
    }
}
```

### App module — required dependencies

These are the imports (dependencies) used by the app and the Leap Mobile SDK. In `app/build.gradle.kts` use the full Maven coordinates:

```kotlin
dependencies {
    // Leap Mobile SDK
    implementation("tech.leapevent:mobile-sdk:{{ latest version }}")
    // Code Scanner library
    implementation("com.code-scanner:library:{{ latest version }}")

    // SDK runtime
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("androidx.datastore:datastore-preferences:1.2.0")
    implementation("androidx.work:work-runtime-ktx:2.11.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:<your_kotlin_version>")
    implementation("net.lingala.zip4j:zip4j:2.9.0")
    implementation("com.toggl:komposable-architecture:1.0.0-preview04")
    implementation("com.google.zxing:core:3.4.1")
}
```

Ensure the Leap Mobile SDK and its dependencies (e.g. `code-scanner`, `parsimonious`) are present so the SDK runs correctly.  
Ensure to use exactly `zip4j` version 2.9.0.

---

## Assets / Initial content

Runtime content lives under `app/src/main/assets/content/`. This content, including images, must be bundled with the app to ensure offline support.

- We'll provide the necessary files for the initial setup.
- Note that the **X** is the version number:
- Place **`content_vX.zip`** and **`runConfig.json`** in a folder called **`content`** in the assets folder:
   ```
   app/src/main/assets/content/
   ```
- Unzip **`images_full_vX.zip`** into a folder called **`Assets`** inside the content folder:
   ```
   app/src/main/assets/content/Assets/
   ```
   The password for the zip file is **`images_full_vX<secret>zip`**, where **`<secret>`** is the value of the `secret` field in `runConfig.json`.

- **`runConfig.json`** — config used by the app/SDK (e.g. content version, project id, schema).
- Other assets in `content/` (e.g. images, zips) are referenced by this config.

Ensure `runConfig.json` exists and matches the format expected by the app. The deeplink **scheme** used in the app (e.g. `fanaticssdkstaging`) should be consistent with your project/backend; the sample manifest uses this scheme for the SDK Activity.

---

# Running the SDK

To run the Leap Mobile SDK with deeplinks and a single host Activity, you need: **Application** initialization, the correct **AndroidManifest** setup, and an **Activity** that hosts the SDK UI and handles back/deeplinks.

---

## Initialize SDK

```kotlin
fun initialize(
    context: Context,
    logging: LoggingConfiguration? = null,
    metrics: MappedProvider? = null,
)
```

- Call `initialize` once before accessing the SDK, ideally in an Application class. Depending on how much content there is, and if there's an OTA update to process, initialization can be costly.
- LoggingConfiguration: Anything logged by the SDK is sent to the LoggingConfigurations provided. ConsoleLoggingConfiguration is a good default one to use.
- MappedProvider: Any analytics tracked by the SDK are sent to the MappedProviders.

You can also initialize the SDK in your `Application#onCreate` so it is ready before any Activity uses it:

```kotlin
class AppHostApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LeapMobileSDK.initialize(this)
    }
}
```

Without this, SDK APIs used in your host Activity (for example `SdkActivity` or `SdkComposeNavigationActivity`) may not behave correctly.

---

## AndroidManifest

### Application class

In case you initialized the SDK into your Application class, point the app to your custom `Application` so the SDK can be initialized once at process start:

```xml
<application
    android:name=".AppHostApplication"
    ...
>
```

Use your own package path if different (e.g. `.AppHostApplication` if it lives in the root package).

### SDK Activity and deeplink scheme

Register the Activity that will host the SDK and handle VIEW intents for your custom scheme:

```xml
<activity
    android:parentActivityName=".MainActivity"
    ...
    >
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="@string/deeplink_scheme" />
    </intent-filter>
</activity>
```

- **`android:name`** — Your SDK host Activity (e.g. `SdkActivity` for the Fragment-based flow, or `SdkComposeNavigationActivity` for Jetpack Compose).
- **`android:parentActivityName`** — Activity to go back to (e.g. `MainActivity`).
- **`<data android:scheme="...">`** — Must match the `fanaticssdkstaging` scheme. Note that the deeplink_scheme is already a defined string resource in the SDK.

The **launcher** Activity (e.g. `MainActivity`) stays as the `MAIN` / `LAUNCHER` entry point; from there you can start your SDK host Activity or send `VIEW` intents with your scheme to open the SDK via deeplink.

---

## SdkActivity — Fragment integration

**Use this path when you integrate with the traditional Android View system and Fragments.** The SDK’s root UI is a `Fragment`; this Activity owns a container and performs fragment transactions, deeplink resolution, and back handling.

The Activity that hosts the SDK must:

1. Use a layout that includes a **Fragment container** (e.g. `FragmentContainerView`, `FrameLayout` with an id that will be referenced on some parts of the code).
2. On create, **resolve the incoming intent**: if it's a deeplink, resolve it to a Fragment; otherwise get the SDK's root layout (Fragment) and show it.
3. **Display the Fragment** in the container and handle **back** so the SDK and navigation stacks are respected.

Summary of what the sample `SdkActivity` does:

- **Layout:** `setContentView(R.layout.activity_sdk)` — layout must contain a view with id `fragmentContainer` (e.g. `FragmentContainerView`).
- **Redirect logic:**
    - If `intent.data` is present, call `LeapMobileSDK.resolveDeeplink(intent.data)`; if a Fragment is returned, show it.

```kotlin
fun resolveDeeplink(uri: Uri): Layout?
```
- Returns a layout for a deeplink, if it exists. Otherwise, it returns null.
- The `deeplink_scheme` value in strings.xml should be changed to match the value for the deeplinks generated by the CMS.
- **Back handling:** Use `OnBackPressedDispatcher` to:
    - Delegate to `RootLayoutHolder.onBackPressDispatcher` when the SDK has callbacks.
    - Otherwise pop from `NavigationController` child fragment manager, or from the Activity's `supportFragmentManager`, or call `finish()`.
- Otherwise, collect `LeapMobileSDK.getRootLayout(supportFragmentManager)` and show the emitted Fragment.

```kotlin
fun getRootLayout(fragmentManager: FragmentManager): Flow<Layout>
```

- A flow of the root layout of the SDK. This value will emit once content has been applied, and will re-emit if that content changes, such as a new OTA being applied.

Plus Android/AndroidX for Activity, Fragment, `lifecycleScope`, and the container view.

In short: **manifest** (Application + an SDK host Activity with the correct scheme), **Application** (`LeapMobileSDK.initialize`), and either **SdkActivity** (Fragment container + deeplink/root resolution + back handling) or **SdkComposeNavigationActivity** (Compose + `LeapMobileSDK.Content`; see **SdkComposeNavigationActivity — Compose integration** below) are what you need to run the SDK correctly.

---

## SdkComposeNavigationActivity — Compose integration

**Use this path when you integrate with Jetpack Compose.** The SDK exposes a Compose entry point, `LeapMobileSDK.Content`, so you do not need a `FragmentContainerView` or manual fragment transactions for the root SDK UI.

The sample `SdkComposeNavigationActivity`:

1. Extends **`FragmentActivity`** (same as other SDK activities in this project).
2. Calls **`setContent { ... }`** and places **`LeapMobileSDK.Content`** in your composable tree (wrapped in your app theme as needed).
3. Passes:
   - **`modifier`** — typically `Modifier.fillMaxSize()` so the SDK fills the screen.
   - **`deeplink`** — optional `Uri` when the user opened the app via your scheme (e.g. build from `intent.data` in `onCreate` / `onNewIntent` after `setIntent(intent)`).
   - **`onBack`** — invoked when the user navigates back inside the SDK; commonly `finish()` the Activity.
   - **`showBackButton`** — whether to show the SDK’s back affordance when applicable.

Example shape (see `SdkComposeNavigationActivity` and `LeapSdkScreen` in the demo app):

```kotlin
setContent {
    YourAppTheme {
        LeapMobileSDK.Content(
            modifier = Modifier.fillMaxSize(),
            deeplink = intent.data, // or null if not a deeplink launch
            onBack = { finish() },
            showBackButton = true,
        )
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Recomposition can pick up the new intent if you pass intent.data into Content
}
```

Register this Activity in the manifest the same way as `SdkActivity`: **`VIEW`** intent filter with your **`android:scheme`**, **`singleTask`** if you want one task for deeplinks, and **`parentActivityName`** for Up navigation.

For **Fragment integration**, use **SdkActivity** above instead.
