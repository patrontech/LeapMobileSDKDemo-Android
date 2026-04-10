# Analytics Integration Guide

## Overview

The Leap Mobile SDK provides a comprehensive analytics data layer that exposes all user interactions and screen views through a delegation pattern. This allows container apps (like Fanatics) to receive analytics events from the SDK and forward them to their own analytics stack (Firebase, internal tools, third-party services, etc.).

**Key Principle**: The SDK **emits** analytics events but does **not** send them to any external service directly. The container app is responsible for receiving, processing, and routing these events.

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Integration Steps](#integration-steps)
3. [Event Categories](#event-categories)
4. [Complete Event Catalog](#complete-event-catalog)
5. [Best Practices](#best-practices)
6. [Troubleshooting](#troubleshooting)

---

## Architecture Overview

### Core Components

The SDK's analytics system is built on the following components:

- **`MappedProvider` Interface**: The interface you implement to receive analytics events
- **`EventName`**: Strongly-typed event identifiers (e.g., `screen_view`, `widget_tap`)
- **`EventParameter`**: Parameter keys (e.g., `screen_name`, `item_id`, `item_name`)
- **`AggregateMetricsService`**: Internal service that distributes events to all registered providers
- **`MappedName`**: Base class that supports provider-specific name mapping

### Data Flow

```
SDK User Interaction
       ↓
Analytics Event Created
       ↓
AggregateMetricsService
       ↓
Your MappedProvider Implementation
       ↓
Your Analytics Stack (Firebase, etc.)
```

### Key Characteristics

✅ **Privacy-Aware**: SDK only emits raw events. Container app handles consent and compliance.  
✅ **Multi-Provider Support**: Register multiple analytics providers simultaneously.  
✅ **Provider-Specific Mapping**: Customize event names per provider (e.g., Firebase vs. Segment).  
✅ **Type-Safe**: All events and parameters are strongly typed.  
✅ **Comprehensive Coverage**: Tracks navigation, widgets, webviews, registration, Fan Scan, badges, surveys, and more.

---

## Integration Steps

### Step 1: Implement the `MappedProvider` Interface

Create a class that implements `MappedProvider`:

```kotlin
import com.greencopper.leapmobilesdk.core.metrics.labels.*
import com.greencopper.leapmobilesdk.core.metrics.provider.*

class FanaticsAnalyticsProvider : MappedProvider {
    // Required: Unique identifier for your provider
    override val name: Provider = Provider("fanatics")
    
    // Required: Track analytics events
    override fun track(event: EventName, parameters: Map<EventParameter, String>) {
        // Extract event name for your provider
        val eventName = event[this.name] ?: event[Provider.default] ?: "unknown_event"
        
        // Convert parameters to your analytics format
        val paramsMap = parameters.mapKeys { param ->
            param.key[this.name] ?: param.key[Provider.default] ?: param.key.toString()
        }.mapValues { it.value }
        
        // Forward to your analytics system
        YourAnalytics.logEvent(eventName, paramsMap)
    }
    
    // Required: Track user properties
    override fun track(parameters: Map<UserProperty, String>) {
        // Set user properties in your analytics system
        parameters.forEach { (property, value) ->
            val propertyName = property[this.name] ?: property[Provider.default] ?: property.toString()
            YourAnalytics.setUserProperty(propertyName, value)
        }
    }
    
    // Required: Enable/disable tracking
    override fun enable() {
        YourAnalytics.setAnalyticsCollectionEnabled(true)
    }
    
    override fun disable() {
        YourAnalytics.setAnalyticsCollectionEnabled(false)
    }
}
```

### Step 2: Register Your Provider During SDK Initialization

```kotlin
import com.greencopper.leapmobilesdk.LeapMobileSDK
import com.greencopper.leapmobilesdk.toolkit.logging.multilogging.configurations.ConsoleLoggingConfiguration

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Create your analytics provider
        val fanaticsAnalytics = FanaticsAnalyticsProvider()
        
        // Initialize the SDK with your provider
        LeapMobileSDK.initialize(
            context = this@MainApplication,
            logging = ConsoleLoggingConfiguration(),
            metrics = fanaticsAnalytics  // ← Your provider
        )
    }
}
```

### Step 3: Handle Analytics Events

Once registered, your provider will automatically receive all analytics events from the SDK. The `AggregateMetricsService` distributes events to all registered providers automatically.

**Note**: Currently, the SDK supports registering one provider at a time. If you need multiple providers, you can create an aggregate provider that forwards events to multiple analytics systems.

---

## Event Categories

The SDK tracks the following categories of events:

### 1. **Navigation & Screen Views**

- Screen view events with screen name and class
- Tab bar navigation
- Deep link navigation

### 2. **Widget Interactions**

- Widget taps
- Widget link clicks
- Ad impressions and taps

### 3. **Content Interactions**

- Schedule item add/remove
- Performer favorites
- Activity favorites
- Location favorites

### 4. **User Actions**

- Login button taps
- Profile view taps
- Filter selections
- Search interactions

### 5. **Webviews**

- Webview screen views with custom analytics parameters
- Link clicks within webviews

### 6. **Registration & Onboarding**

- Registration completion
- Onboarding page views
- Onboarding action taps

### 7. **Fan Scan**

- Check-in success/failure
- Permission requests
- Settings navigation

### 8. **Badges & Gamification**

- Badge taps with badge IDs

### 9. **Surveys**

- Survey screen views

### 10. **Permissions**

- Location permission requests
- Notification permission requests
- Bluetooth permission requests

### 11. **Account Management**

- Logout actions
- Account deletion flow

### 12. **Maps**

- Map point taps
- Location detail views

### 13. **Push Notifications**

- Notification taps

---

## Complete Event Catalog

### Core Events

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `screen_view` | User viewed a screen | `screen_name`, `screen_class` |

### InterfaceKit Events

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `widget_collection/widget_tap` | User tapped a widget | `item_name`, `item_id`, `item_category`, `screen_name` |
| `widget_collection/link_tap` | User tapped a link in a widget | `item_name`, `screen_name` |
| `tab_bar/tab_tap` | User tapped a tab | `item_name`, `screen_name` |
| `top_bar/button_tap` | User tapped a top bar button | `item_name`, `screen_name` |
| `inbox/item_tap` | User tapped an inbox item | `item_id`, `item_name` |
| `ad_tap` | User tapped an ad | `item_id`, `item_name` |
| `ad_impression` | Ad was displayed | `item_id`, `item_name` |
| `filter_select` | User selected a filter | Filter-specific parameters |
| `unregisteredaccountwidget/login_button_tap` | User tapped login | `screen_name` |
| `accountsummarywidget/view_profile_button_tap` | User tapped view profile | `screen_name` |
| `project_switcher/project_tap` | User switched projects | `item_name`, `item_id` |
| `permission/location` | Location permission request | Permission status |
| `permission/notifications` | Notification permission request | Permission status |
| `permission/bluetooth` | Bluetooth permission request | Permission status |
| `onboarding/ad/close_tap` | User closed ad onboarding | Onboarding context |
| `onboarding/ad/ad_tap` | User tapped ad in onboarding | Onboarding context |
| `interests_picker/select` | User selected an interest | `item_name`, `item_id` |
| `interests_picker/unselect` | User unselected an interest | `item_name`, `item_id` |
| `interests_picker/close` | User closed interests picker | Selected interests |

### Event Module Events

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `schedule/day_picker_tap` | User tapped a day in picker | `item_name` (date), `screen_name` |
| `schedule/next_button_tap` | User tapped next on schedule | `item_name` (date), `screen_name` |
| `my_schedule/add` | User added to schedule | `item_id`, `item_name`, `screen_name` |
| `my_schedule/remove` | User removed from schedule | `item_id`, `item_name`, `screen_name` |
| `my_activities/add` | User added activity | `item_id`, `item_name`, `screen_name` |
| `my_activities/remove` | User removed activity | `item_id`, `item_name`, `screen_name` |
| `my_performers/add` | User added performer | `item_id`, `item_name`, `screen_name` |
| `my_performers/remove` | User removed performer | `item_id`, `item_name`, `screen_name` |

### Activations Module Events (Fan Scan, Badges, Registration)

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `thuzi_registration` | User completed registration | `item_category` (screen) |
| `fan_scan/checkin_success` | Successful Fan Scan check-in | `item_id` (module ID) |
| `fan_scan/checkin_failure` | Failed Fan Scan check-in | `item_id` (module ID) |
| `fan_scan/os_settings_click` | User clicked OS settings | Context info |
| `badges/badge_click` | User tapped a badge | `item_id` (badge ID) |
| `logout/logout_click` | User tapped logout | Context info |
| `account_deletion/confirmed` | User confirmed deletion | Context info |
| `account_deletion/success` | Deletion succeeded | Context info |
| `account_deletion/fail` | Deletion failed | Context info |
| `account_deletion/retry` | User retried deletion | Context info |

### Maps Module Events

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `geo_map/point_tap` | User tapped a map point | `item_id`, `item_name` |
| `my_locations/add` | User added location | `item_id`, `item_name`, `screen_name` |
| `my_locations/remove` | User removed location | `item_id`, `item_name`, `screen_name` |

### Push Notification Events

| Event Name | Description | Common Parameters |
|------------|-------------|-------------------|
| `notification/tap` | User tapped notification | Notification metadata |

### Screen Classes

Screen classes provide context about the type of screen being viewed:

**InterfaceKit**:

- `launch`, `search`, `editorial_page`, `inbox`, `webview`, `widget_collection`, `tab_bar`
- `full_screen_media`, `main_action_card_onboarding_page`, `ad_onboarding_page`
- `project_switcher`, `project_switching`, `interests_picker`

**Event Module**:

- `schedule`, `schedule_detail`, `schedule_reminders_selector`
- `activities_list`, `activity_detail`, `performers_list`, `performer_detail`

**Activations (Thuzi)**:

- `thuzi_registration`, `thuzi_event_pass`, `thuzi_fan_scan`, `thuzi_fan_scan_permission`
- `thuzi_fan_scan_checkin`, `thuzi_badges`, `thuzi_survey`, `thuzi_microsite`
- `thuzi_account_deletion`, `thuzi_logout`

**Maps**:

- `geo_map`, `location_detail`, `locations_list`

**Ticketing**:

- `ticketing_tickets_scan`, `ticketing_showclix_login`

### Event Parameters

| Parameter | Description | Type |
|-----------|-------------|------|
| `screen_name` | Name of the screen | String |
| `screen_class` | Class/type of screen | String |
| `item_id` | Unique identifier | String |
| `item_name` | Display name | String |
| `item_category` | Category or type | String |
| `uri` | URI or URL | String |

---

## Best Practices

### 1. **Implement User Consent**

The SDK emits raw events without any privacy filtering. Your provider implementation **must** check user consent:

```kotlin
override fun track(event: EventName, parameters: Map<EventParameter, String>) {
    // ✅ Always check consent first
    if (!userConsentManager.hasAnalyticsConsent) return
    
    // Then forward event
    YourAnalytics.logEvent(...)
}
```

### 2. **Handle Provider-Specific Limitations**

Different analytics platforms have different constraints. Be aware of:

- Parameter key length limits
- Parameter value length limits
- Maximum number of parameters per event
- Maximum number of user properties

### 3. **Use Provider-Specific Event Names**

The SDK supports provider-specific event name mapping through the `MappedName` system:

```kotlin
// EventName and EventParameter extend MappedName
// You can map different names for different providers
val eventName = event[this.name] ?: event[Provider.default] ?: "unknown_event"
```

### 4. **Batch Events for Performance**

If sending events to a remote API, consider batching:

```kotlin
class BatchingAnalyticsProvider : MappedProvider {
    private val eventQueue = mutableListOf<AnalyticsEvent>()
    private val batchSize = 10
    
    override fun track(event: EventName, parameters: Map<EventParameter, String>) {
        val analyticsEvent = convertToEvent(event, parameters)
        eventQueue.add(analyticsEvent)
        
        if (eventQueue.size >= batchSize) {
            flushEvents()
        }
    }
    
    private fun flushEvents() {
        apiClient.sendBatch(eventQueue)
        eventQueue.clear()
    }
}
```

### 5. **Error Handling**

Handle analytics errors gracefully without affecting user experience:

```kotlin
override fun track(event: EventName, parameters: Map<EventParameter, String>) {
    try {
        sendAnalyticsEvent(event, parameters)
    } catch (e: Exception) {
        // Log error but don't crash
        Log.e("Analytics", "Error tracking event", e)
        // Optionally queue for retry
    }
}
```

---

## Troubleshooting

### Events Not Appearing

**Problem**: Your provider's `track` methods are not being called.

**Solutions**:

1. Verify your provider is registered during initialization:
   ```kotlin
   LeapMobileSDK.initialize(
       context = this,
       metrics = yourProvider  // ← Make sure this is not null
   )
   ```

2. Check that your provider implements `MappedProvider` correctly:
   ```kotlin
   class YourProvider : MappedProvider {
       override val name: Provider  // ← Required
       override fun track(event: EventName, parameters: Map<EventParameter, String>)  // ← Required
       override fun track(parameters: Map<UserProperty, String>)  // ← Required
       override fun enable()  // ← Required
       override fun disable()  // ← Required
   }
   ```

3. Ensure the SDK is fully initialized before displaying UI:
   ```kotlin
   LeapMobileSDK.initialize(...)  // ← Must complete before showing UI
   ```

### Events Missing Parameters

**Problem**: Some expected parameters are null or missing.

**Solutions**:

1. Check parameter extraction for your provider:
   ```kotlin
   // SDK uses Provider.default by default
   val value = parameters[EventParameter.screenName]
   ```

2. Remember that parameters are always `String` type in Android:
   ```kotlin
   val screenName = parameters[EventParameter.screenName] ?: ""
   ```

3. Not all events include all parameters. Check the event catalog above.

### Duplicate Events

**Problem**: Events are being tracked multiple times.

**Solutions**:

1. Check if you've registered the same provider multiple times.
2. Verify you're not also tracking SDK events separately in your app code.

### Performance Issues

**Problem**: Analytics tracking is causing performance problems.

**Solutions**:

1. Implement batching (see Best Practices).
2. Send analytics asynchronously:
   ```kotlin
   override fun track(event: EventName, parameters: Map<EventParameter, String>) {
       CoroutineScope(Dispatchers.IO).launch {
           // Send analytics on background thread
       }
   }
   ```
3. Consider rate limiting in high-frequency scenarios.

---

## Support & Resources

### SDK Version Information

- This guide is for LeapMobileSDK (Android)
- Last updated: February 2026

### Additional Documentation

- [SDK Integration Guide](README.md) - Main SDK documentation

### Key Files in SDK

- `LeapMobileSDK/src/main/java/com/greencopper/leapmobilesdk/core/metrics/provider/MappedProvider.kt` - Provider interface
- `LeapMobileSDK/src/main/java/com/greencopper/leapmobilesdk/core/metrics/labels/EventName.kt` - Event name definitions
- `LeapMobileSDK/src/main/java/com/greencopper/leapmobilesdk/core/metrics/labels/EventParameter.kt` - Parameter definitions
- `LeapMobileSDK/src/main/java/com/greencopper/leapmobilesdk/LeapMobileSDK.kt` - SDK initialization

---

**Document Version**: 1.0  
**Date**: February 06, 2026  
**Author**: Leap Mobile SDK Team
