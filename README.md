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

These are the imports (dependencies) used by the app and the Leap Mobile SDK. In `app/build.gradle.kts` use the version catalog aliases:

```kotlin
dependencies {
    // Leap Mobile SDK — core
    implementation("tech.leapevent:mobile-sdk-core:{{ latest version }}")
    // Fanatics bundle (same version as mobile-sdk-core). Remove if the app provides assets under content/ itself.
    implementation("tech.leapevent:mobile-sdk-fanatics-bundle:{{ latest version }}")
    // Code Scanner library
    implementation("com.code-scanner:library:{{ latest version }}")
}
```

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

Register the Activity that will host the SDK. If you want the system (and external apps) to route deeplinks to this Activity automatically, add an `<intent-filter>` for your custom scheme:

```xml
<activity
    android:name=".SdkActivity"
    android:exported="true"
    android:parentActivityName=".MainActivity"
    android:theme="@style/Theme.SDKDeeplinking">
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

> **Compose entry point — the intent-filter is optional.**
> When using the Compose integration (`LeapMobileSDK.Content`), you can skip the `<intent-filter>` and open the SDK with a deeplink by starting your host Activity with an explicit intent, passing the deeplink URI as `intent.data`. See [Programmatic deeplink navigation (Compose)](#programmatic-deeplink-navigation-compose) below.

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

The hosting Activity only needs to extend **`ComponentActivity`** (or any subclass such as `AppCompatActivity` or `FragmentActivity`). The SDK manages its own `FragmentController` internally, so `FragmentActivity` is **not** required for the Compose integration path.

The sample `SdkComposeNavigationActivity`:

1. Extends **`ComponentActivity`** — the minimal base class for Compose apps.
2. Calls **`setContent { ... }`** and places **`LeapMobileSDK.Content`** in your composable tree (wrapped in your app theme as needed).
3. Passes:
    - **`modifier`** — typically `Modifier.fillMaxSize()` so the SDK fills the screen.
    - **`deeplink`** — optional `Uri` when the user opened the app via your scheme (e.g. build from `intent.data` in `onCreate` / `onNewIntent` after `setIntent(intent)`).
    - **`deeplinkHandler`** — optional lambda that intercepts deeplink URIs before the SDK falls back to `ACTION_VIEW`. Return `true` if the deeplink was consumed (e.g. you started an Activity), `false` to let the SDK proceed with the default intent-based behaviour. Defaults to `{ false }` (no interception). See [Handling CMS deeplink buttons](#handling-cms-deeplink-buttons-compose) below.
    - **`onBack`** — invoked when the user navigates back inside the SDK; commonly `finish()` the Activity.
    - **`showBackButton`** — whether to show the SDK’s back affordance when applicable.
    - **`onBackStackChanged`** — optional callback invoked whenever the SDK’s internal navigation depth changes. Receives `isRoot: Boolean` (`true` when the user is on the SDK’s root screen) and `depth: Int` (total back-stack entries). Use this to reactively show/hide custom toolbar elements (e.g. FanCash pill, profile button) based on whether the user is at the SDK root or a deeper screen.

Example shape (see `SdkComposeNavigationActivity` and `LeapSdkScreen` in the demo app):

```kotlin
setContent {
    YourAppTheme {
        var isRoot by remember { mutableStateOf(true) }

        LeapMobileSDK.Content(
            modifier = Modifier.fillMaxSize(),
            deeplink = intent.data, // or null if not a deeplink launch
            onBack = { finish() },
            showBackButton = true,
            onBackStackChanged = { root, depth ->
                isRoot = root
            },
        )

        // Use isRoot to show/hide custom toolbar elements
        if (isRoot) {
            MyCustomToolbar()
        }
    }
}

override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // Recomposition can pick up the new intent if you pass intent.data into Content
}
```

There are two ways to register this Activity in the manifest and open it with a deeplink:

#### Option 1: Manifest intent-filter (same as `SdkActivity`)

Register a `VIEW` intent-filter with your `android:scheme`, `singleTask` if you want one task for deeplinks, and `parentActivityName` for Up navigation. The system (and external apps like browsers or push notifications) will route matching URIs to the Activity automatically.

#### Option 2: Programmatic deeplink navigation (Compose)

Skip the `<intent-filter>` and start your SDK host Activity with an explicit intent, passing the deeplink URI as `intent.data`. The SDK resolves it automatically:

```kotlin
val deeplinkUri = "fanaticssdkstaging://schedule".toUri()
context.startActivity(
    Intent(context, YourSdkActivity::class.java).apply {
        data = deeplinkUri
    }
)
```

This is useful when you want to navigate to a specific SDK screen from within your app without declaring a custom scheme in the manifest.

Both options can coexist. For example, you can use Option 2 for in-app navigation and still declare the intent-filter (Option 1) if you also need external apps to route deeplinks to your Activity.

For **Fragment integration**, use **SdkActivity** above instead.

### Handling CMS deeplink buttons (Compose)

When a button configured in the CMS triggers a deeplink with your app's scheme, there are two ways to intercept it:

#### Option 1: Manifest intent filter

Register a dedicated Activity with an `<intent-filter>` for your deeplink scheme. The system (and the SDK's default `ACTION_VIEW` fallback) will route matching URIs to that Activity automatically:

```xml
<activity
    android:name=".deeplink.DeepLinkActivity"
    android:exported="true"
    android:theme="@style/Theme.MyApplication.NoToolbar">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="@string/sample_app_deeplink_scheme" />
    </intent-filter>
</activity>
```

This is the traditional Android approach — no code changes inside the composable are needed.

#### Option 2: `deeplinkHandler` parameter

Pass a `deeplinkHandler` lambda to `LeapMobileSDK.Content`. The SDK calls this handler before falling back to `ACTION_VIEW`, giving you the opportunity to intercept the URI in-process:

```kotlin
LeapMobileSDK.Content(
    modifier = Modifier.fillMaxSize(),
    deeplink = deeplink,
    deeplinkHandler = { uri ->
        if (uri.scheme == getString(R.string.sample_app_deeplink_scheme)) {
            val intent = Intent(this, DeepLinkActivity::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true  // consumed
        } else {
            false // let the SDK handle it
        }
    },
    onBack = { finish() },
    showBackButton = true,
)
```

With this approach the `DeepLinkActivity` does **not** need an `<intent-filter>` in the manifest — routing is handled entirely by the lambda. The handler is lifecycle-safe: it is registered via `DisposableEffect` and automatically cleared when the composable leaves the composition, so it will not leak the enclosing Activity or Context.

Both options can coexist: if the `deeplinkHandler` returns `false` (or is not provided), the SDK falls back to `ACTION_VIEW`, which the system will route to any Activity whose manifest filter matches the URI.

---

## Analytics Integration

The Leap Mobile SDK provides a comprehensive analytics data layer that exposes all user interactions and screen views through a delegation pattern. This allows container apps to receive analytics events from the SDK and forward them to their own analytics stack (Firebase, internal tools, third-party services, etc.).

**Key Principle**: The SDK **emits** analytics events but does **not** send them to any external service directly. The container app is responsible for receiving, processing, and routing these events.

### Quick Start

For a quick integration guide, see [ANALYTICS_QUICK_START.md](ANALYTICS_QUICK_START.md). This guide will help you get started with analytics integration in just a few minutes.

### Complete Documentation

For comprehensive documentation including:

- Detailed architecture overview
- Complete event catalog
- Best practices and troubleshooting
- Privacy and compliance guidelines

See [ANALYTICS_INTEGRATION.md](ANALYTICS_INTEGRATION.md).

### Basic Integration

To integrate analytics, you need to:

1. Implement the `MappedProvider` interface to receive analytics events
2. Register your provider during SDK initialization using the `metrics` parameter
3. Your provider will automatically receive all analytics events from the SDK

Example:

```kotlin
class MyAnalyticsProvider : MappedProvider { 
    override val name: Provider = Provider("my_provider")
    
    override fun track(event: EventName, parameters: Map<EventParameter, String>) { 
        // Forward events to your analytics system 
    }
    
    override fun track(parameters: Map<UserProperty, String>) { 
        // Handle user properties 
    }
    
    override fun enable() {}
    override fun disable() {}
}

// In your Application class:
LeapMobileSDK.initialize(
    context = this, 
    metrics = MyAnalyticsProvider()
)
```

## Logging & Health Monitoring

The SDK exposes a logging layer so the container app can receive all SDK logs and errors and forward them to its own logging, crash reporting, and observability tools. The SDK does **not** send logs to any external service.

For documentation (integration, best practices, health monitoring, troubleshooting), see [LOGGING_INTEGRATION.md](LOGGING_INTEGRATION.md).
