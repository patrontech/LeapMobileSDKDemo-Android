# Logging & Health Monitoring Integration Guide

## Overview

The Leap Mobile SDK exposes a logging layer so the container app can receive all SDK logs and errors. The SDK does **not** send logs to any external service. The container app forwards them to its own logging, crash reporting, and observability tools.

---

## Architecture

### Components

- **`LoggingConfiguration`**: Interface you implement. Its `log()` is called for every SDK log.
- **`LogLevel`**: VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT.
- **`Logging`**: The logger instance. Access via `LeapMobileSDK.getLog()`. Supports multiple configurations (add/remove).

### Data flow

```
SDK code → Logging.log(priority, message, tag, throwable)
         → each LoggingConfiguration.log()
         → your crash reporting / logging backend
```

---

## Integration

### 1. Implement LoggingConfiguration

```kotlin
import com.greencopper.leapmobilesdk.toolkit.logging.LogLevel
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.LoggingConfiguration

class FanaticsLoggingConfiguration(
    private val crashlytics: FirebaseCrashlytics
) : LoggingConfiguration { 
    override fun log(
        priority: LogLevel,
        message: String,
        tag: String?,
        throwable: Throwable?,
        vararg args: Any?
    ) {
        val fullMessage = buildString {
            if (tag != null) append("[$tag] ")
            append(message)
        }

        when (priority) {
            LogLevel.ERROR -> {
                crashlytics.log(fullMessage)
                (throwable ?: Exception(message)).let { crashlytics.recordException(it) }
            }
            LogLevel.WARN -> crashlytics.log("WARN: $fullMessage")
            else -> if (BuildConfig.DEBUG) {
                android.util.Log.println(priority.value, tag ?: "LeapSDK", message)
            }
        }
    }
}
```

### 2. Register at init

```kotlin
LeapMobileSDK.initialize(
    context = applicationContext,
    logging = FanaticsLoggingConfiguration(FirebaseCrashlytics.getInstance()),
    metrics = myProvider  // optional
)
```

### 3. Add or remove configurations at runtime

```kotlin
val log = LeapMobileSDK.getLog()
log.addConfiguration(AnotherLoggingConfiguration())
log.removeConfiguration(someConfig)
log.removeAllConfigurations()
```

---

## Log Levels and Parameters

| Level   | Typical use |
|---------|-------------|
| VERBOSE | Detailed traces |
| DEBUG   | Debug info |
| INFO    | Normal operations |
| WARN    | Recoverable issues |
| ERROR   | Failures, exceptions |
| ASSERT  | Critical failures |

`log()` parameters:

- **priority**: `LogLevel`
- **message**: Log text (may contain format placeholders used by the SDK).
- **tag**: Optional tag (e.g. class or feature name). Can be null.
- **throwable**: Optional exception. Often non-null for ERROR.
- **args**: Optional format arguments for the message.

---

## What the SDK Logs

- **Errors**: Parsing, network, configuration, runtime errors (often with `Throwable`).
- **Network**: HTTP/API errors, retries.
- **Operations**: Initialization, content loading, feature resolution, OTA updates.
- **State**: Navigation, user actions, feature lifecycle.
- **Debug**: Internal state at VERBOSE/DEBUG when you use a config that forwards them.

---

## Built-in configurations

- **ConsoleLoggingConfiguration**: Writes to `android.util.Log` in debug builds. Good for development.
- **FileLoggingConfiguration** / **TagFileLoggingConfiguration**: File-based logging (see SDK source for constructors).

You can pass one at init or add it alongside your custom config:

```kotlin
LeapMobileSDK.initialize(
    context = applicationContext,
    logging = ConsoleLoggingConfiguration()
)
```

---

## Troubleshooting

**No logs received**  

- Ensure you pass a `LoggingConfiguration` in `initialize()` (or add one via `getLog().addConfiguration()`).
- Ensure the SDK has been initialized before the code paths that log.

**Too many logs**  

- In your `log()` implementation, ignore levels below WARN (or INFO) in release builds.
- Filter by `tag` if your backend supports it.

**Errors not in crash reports**  

- For ERROR, call your crash reporting API (e.g. `recordException`) with the `throwable` or a fallback exception.

---

## Using the SDK logger from the container app

After init, you can log from your app through the same pipeline:

```kotlin
LeapMobileSDK.getLog().e("Container app error", throwable = myException)
LeapMobileSDK.getLog().w("Warning from container")
LeapMobileSDK.getLog().i("Info message", tag = "MyFeature")
```

Those calls go to all registered `LoggingConfiguration` instances.

---

## Privacy

The SDK does not send logs anywhere. Your implementation must:

- Redact or avoid logging PII before sending to third parties.
- Follow consent and retention policies (e.g. GDPR, CCPA).

---

**SDK**: Leap Mobile SDK (Android)  
**Key types**: `LoggingConfiguration`, `LogLevel`, `Logging` (from `LeapMobileSDK.getLog()`)
