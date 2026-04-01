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

Without this, SDK APIs used in `SdkActivity` may not behave correctly.

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

- **`android:name`** — Your SDK host Activity (e.g. `SdkActivity`).
- **`android:parentActivityName`** — Activity to go back to (e.g. `MainActivity`).
- **`<data android:scheme="...">`** — Must match the `fanaticssdkstaging` scheme. Note that the deeplink_scheme is already a defined string resource in the SDK.

The **launcher** Activity (e.g. `MainActivity`) stays as the `MAIN` / `LAUNCHER` entry point; from there you can start `SdkActivity` or send VIEW intents with your scheme to open the SDK via deeplink.

---

## SdkActivity

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

In short: **manifest** (Application + SdkActivity with correct scheme), **Application** (`LeapMobileSDK.initialize`), and **SdkActivity** (container + deeplink/root resolution + back handling) are the three pieces needed to run the SDK correctly.

---

## Compose and Fragment Compatibility

Jetpack Compose and Fragments are built on different UI paradigms, which can lead to incompatibility and integration issues:

- **Lifecycle Management:** Compose manages its own lifecycle and recomposition, while Fragments rely on the traditional View system. Mixing them can cause unexpected behavior, especially with navigation and state restoration.
- **Recomposition Issues:** Compose UI can recompose at any time, which may interfere with Fragment transactions or cause Fragments to be recreated unexpectedly.
- **View Hierarchy:** Fragments expect to be attached to a ViewGroup (like FrameLayout or FragmentContainerView). Compose does not provide a direct equivalent, so inflating Fragments inside Compose layouts is not recommended.
- **Navigation Conflicts:** Compose navigation and Fragment navigation can conflict, leading to back stack issues or navigation bugs.

**Best Practice:**

For SDKs or libraries that require Fragments (such as LeapMobileSDK), always create a dedicated Activity that hosts a `FrameLayout` or `FragmentContainerView`. This container should be used to inflate and display Fragments. Avoid mixing Compose UI and Fragment transactions in the same layout to prevent lifecycle and navigation problems.

This approach ensures predictable behavior, proper back stack management, and compatibility with SDKs that rely on Fragments.
