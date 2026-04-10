# Analytics Integration Quick Start

## 30-Second Integration

### 1. Create Your Provider

```kotlin
import com.greencopper.leapmobilesdk.core.metrics.labels.*
import com.greencopper.leapmobilesdk.core.metrics.provider.*

class MyAnalyticsProvider : MappedProvider { 
    override val name: Provider = Provider("my_provider")
    
    override fun track(event: EventName, parameters: Map<EventParameter, String>) { 
        val eventName = event[Provider.default] ?: "unknown" 
        val paramsMap = parameters.mapKeys { 
            it.key[Provider.default] ?: it.key.toString() 
        }
        
        // Send to your analytics system 
        YourAnalytics.logEvent(eventName, paramsMap)
    }
    
    override fun track(parameters: Map<UserProperty, String>) { 
        // Handle user properties 
        parameters.forEach { (property, value) -> 
            val key = property[Provider.default] ?: property.toString()
            YourAnalytics.setUserProperty(key, value) 
        } 
    }
    
    override fun enable() {}
    override fun disable() {}
}
```

### 2. Register During Initialization

```kotlin
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.configurations.ConsoleLoggingConfiguration

class MainApplication : Application() {
    override fun onCreate() { 
        super.onCreate() 
        val myProvider = MyAnalyticsProvider()
        LeapMobileSDK.initialize(
            context = this@MainApplication, 
            logging = ConsoleLoggingConfiguration(), 
            metrics = myProvider  // ← Add your provider here
        ) 
    }
}
```

### 3. Done! 🎉

Your provider will now receive all SDK analytics events automatically through the `AggregateMetricsService`.

---

## Common Events You'll See

### Screen Views

```kotlin
Event: "screen_view"
Parameters: 
  - screen_name: "Home page"
  - screen_class: "widget_collection"
```

### Widget Taps

```kotlin
Event: "widget_collection/widget_tap"
Parameters:
  - item_name: "Featured Content"
  - item_category: "banner"
  - screen_name: "Home page"
```

### Registration

```kotlin
Event: "thuzi_registration"
Parameters:
  - item_category: "registration_screen"
```

### Fan Scan

```kotlin
Event: "fan_scan/checkin_success"
Parameters:
  - item_id: "module_123"
```

---

## Full Event List

See [ANALYTICS_INTEGRATION.md](ANALYTICS_INTEGRATION.md#complete-event-catalog) for the complete catalog of all events, parameters, and screen classes.

## Key Points

✅ SDK emits events, your provider receives them  
✅ No data is sent to external services by the SDK  
✅ You control where events go (Firebase, internal API, etc.)  
✅ You handle user consent and privacy  
✅ Multiple providers supported simultaneously (via aggregate pattern)  
✅ Events are distributed automatically to all registered providers

## Important Notes

### Provider Name Mapping

The SDK uses a `MappedName` system that allows different event names for different providers:

```kotlin
// Extract event name for your specific provider
val eventName = event[this.name] ?: event[Provider.default] ?: "unknown_event"

// Extract parameter names for your provider
val paramName = param[this.name] ?: param[Provider.default] ?: param.toString()
```

### Automatic Distribution

Once registered, your provider automatically receives all events through the `AggregateMetricsService`. No additional code is needed - just implement the interface and register it during initialization.

### Manual Tracking

Note that screen view tracking is **not** automatic. Each fragment must manually track screen views in `onResume()`:

```kotlin
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.core.metrics.events.ScreenViewEvent
import com.greencopper.leapmobilesdk.core.metrics.Screen

// ...

override fun onResume() { 
    super.onResume()
    LeapMobileSDK.track(ScreenViewEvent(Screen("MyScreen", "my_screen_class")))
}
```

However, once tracked, the event is automatically distributed to all registered providers.

## Need Help?

Read the full guide: [ANALYTICS_INTEGRATION.md](ANALYTICS_INTEGRATION.md)
