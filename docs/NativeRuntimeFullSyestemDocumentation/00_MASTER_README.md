# NativeRuntime Android SDK Ecosystem - মাস্টার আর্কিটেকচার ও টেকনিক্যাল ডকুমেন্টেশন

> **প্রস্তুতকারক:** Senior Android SDK Architect & Technical Documentation Engineer  
> **ইকোসিস্টেম সংস্করণ:** 1.0.0-provisional  
> **সর্বমোট মডিউল:** ১৫টি স্বতন্ত্র Android/Java লাইব্রেরি  
> **মোট সোর্স লাইন সংখ্যা (LOC):** 12329 লাইন  
> **গড় সমাপ্তির হার (Average Ecosystem Completion):** 86.8%

---

## ১. ইকোসিস্টেমের সামগ্রিক স্থাপত্য (Ecosystem Architecture)

NativeRuntime হলো একটি আধুনিক, স্তরভিত্তিক (Layered), ডিকাপলড হাইব্রিড রানটাইম আর্কিটেকচার যা ওয়েব অ্যাপ্লিকেশনকে উচ্চ-ক্ষমতাসম্পন্ন অ্যান্ড্রয়েড অ্যাপ্লিকেশনে রূপান্তর এবং পরিচালনা করতে সক্ষম। 

প্রদত্ত আর্কিটেকচার চিত্র (`NativeRuntimeLayeredArchitectureDiagram_87a563dc.png`) অনুযায়ী পুরো সিস্টেমটি **৬টি পরিষ্কার স্তরে (Layer 0 থেকে Layer 5)** বিন্যস্ত:

```
+-----------------------------------------------------------------------------------+
| Layer 5: Application / Client                                                     |
|  - Android Application / Host App (Launches)                                      |
|  - Demo Sample Activity (Direct API Call)                                         |
+-----------------------------------------------------------------------------------+
                                        | Launches / Direct API Call
+-----------------------------------------------------------------------------------+
| Layer 4: Master Orchestration (Turnkey SDK)                                       |
|  - NativeRuntimeSDK (Master Facade, NativeRuntimeActivity, NativeRuntimeBootstrap)|
+-----------------------------------------------------------------------------------+
                                        | Registers, Boots, Assembles
+-----------------------------------------------------------------------------------+
| Layer 3: Transport & Communication Bridge                                         |
|  - NativeWebBridgeSDK (WebBridge, NativeJavascriptInterface, BridgeEventBus)      |
+-----------------------------------------------------------------------------------+
                                        | Hosts DOM / Dispatches RPC
+-----------------------------------------------------------------------------------+
| Layer 2: Domain Engine Subsystems                                                 |
|  - Routing & Dispatch: NativeRouterSDK, Interceptors (Auth, Logging, Perf)        |
|  - Core Domain Engines: NativePlatformEngine, NativeStorageEngine,                |
|                         NativeSecurityEngine, NativeNetworkEngine,                |
|                         NativeAnalyticsEngine, NativeThemeEngine,                 |
|                         NativeDevToolsSDK                                         |
|  - UI & Web Host:       NativeRuntimeUI, NativeRuntimeEngine                      |
+-----------------------------------------------------------------------------------+
                                        | Managed Service / Diagnostic Logs
+-----------------------------------------------------------------------------------+
| Layer 1: Core Infrastructure                                                      |
|  - NativeServiceSDK (BaseService, Lifecycle & ServiceRegistry)                    |
|  - NativeLoggerSDK (Multi-Sink Logger: Logcat, File, Remote, WebConsoleMirror)    |
+-----------------------------------------------------------------------------------+
                                        | Implements & Inherits
+-----------------------------------------------------------------------------------+
| Layer 0: Universal Foundation Contracts                                           |
|  - NativeRuntimeContracts (RuntimeState, IApiHandler, NativeApiRequest/Response)  |
+-----------------------------------------------------------------------------------+
```

---

## ২. ১৫টি লাইব্রেরির তালিকা ও সংক্ষিপ্ত ভূমিকা (Library Inventory)

| ক্র. | লাইব্রেরির নাম | আর্কিটেকচারাল লেয়ার | প্লাগইন ধরন | সোর্স ফাইল | LOC | সমাপ্তি (%) |
|---|---|---|---|---|---|---|
| ১ | `NativeRuntimeContracts` | Layer 0: Foundation | com.android.library | 23 | 895 | 95% |
| ২ | `NativeServiceSDK` | Layer 1: Core Infra | com.android.library | 10 | 776 | 85% |
| ৩ | `NativeLoggerSDK` | Layer 1: Core Infra | com.android.library | 11 | 1141 | 85% |
| ৪ | `NativeRouterSDK` | Layer 2: Routing | com.android.library | 13 | 837 | 92% |
| ৫ | `NativePlatformEngine` | Layer 2: Core Engine | com.android.library | 7 | 723 | 78% |
| ৬ | `NativeStorageEngine` | Layer 2: Core Engine | java-library | 9 | 1624 | 90% |
| ৭ | `NativeSecurityEngine` | Layer 2: Core Engine | java-library | 12 | 1005 | 80% |
| ৮ | `NativeNetworkEngine` | Layer 2: Core Engine | java-library | 14 | 975 | 88% |
| ৯ | `NativeAnalyticsEngine` | Layer 2: Core Engine | com.android.library | 6 | 597 | 88% |
| ১০ | `NativeThemeEngine` | Layer 2: Core Engine | com.android.library | 5 | 417 | 90% |
| ১১ | `NativeDevToolsSDK` | Layer 2: Core Engine | java-library | 12 | 968 | 86% |
| ১২ | `NativeRuntimeEngine` | Layer 2: Web Host | com.android.library | 5 | 570 | 84% |
| ১৩ | `NativeRuntimeUI` | Layer 2: UI Host | com.android.library | 6 | 469 | 82% |
| ১৪ | `NativeWebBridgeSDK` | Layer 3: Transport | com.android.library | 13 | 871 | 92% |
| ১৫ | `NativeRuntimeSDK` | Layer 4: Master Orchestration | com.android.library | 3 | 461 | 88% |

---

## ৩. ১০-ধাপের বুটস্ট্র্যাপ সিকোয়েন্স (10-Step Bootstrap Sequence)

`NativeRuntimeBootstrap` ক্লাসে এনফোর্স করা সুনির্দিষ্ট বুটস্ট্র্যাপ ধাপসমূহ:

1. **Step 0: Logger (`NativeLoggerSDK`)** - সেন্ট্রাল সিঙ্গলটন লগার ইনিশিয়ালাইজ ও ফরম্যাটার কনফিগারেশন।
2. **Step 1: Theme (`NativeThemeEngine`)** - থিম ম্যানেজার এবং সিস্টেম ডার্ক/লাইট মোড অবজারভেশন শুরু।
3. **Step 2: Services (`NativeServiceSDK`)** - সার্ভিস রেজিস্ট্রি সিঙ্গলটন তৈরি।
4. **Step 3: Platform (`NativePlatformEngine` & Core Engines)** - ব্যাটারি, ডিভাইস, ক্লিপবোর্ড, স্টোরেজ, সিকিউরিটি এবং নেটওয়ার্ক ইঞ্জিন প্রস্তুতকরণ।
5. **Step 4: Analytics (`NativeAnalyticsEngine`)** - সেশন এবং পারফরম্যান্স ট্র্যাকার স্টার্ট ও রেজিস্ট্রিতে যুক্ত করা (Priority 25)।
6. **Step 5: API Router (`NativeRouterSDK`)** - সেন্ট্রাল রাউটার সিঙ্গলটন ইনস্ট্যান্স তৈরি ও ইন্টারসেপ্টর চেইন স্থাপন।
7. **Step 6: Handlers (RPC Command Handlers)** - সমস্ত ডোমেইন কমান্ড হ্যান্ডলার রাউটারে রেজিস্টার করা (`logger`, `service`, `echo`, `diagnostic`, `platform`, `storage`, `security`, `network`, `devtools`, `analytics`)।
8. **Step 7: UI (`NativeRuntimeUI`)** - `NativeUIHost` ইনিশিয়ালাইজ এবং ইউআই ও থিম কমান্ড হ্যান্ডলার বাইন্ডিং।
9. **Step 8: Engine (`NativeRuntimeEngine`)** - `WebSourceResolver`, এমবেডেড `LocalWebServer` (127.0.0.1) এবং `WebViewContainer` কনফিগারেশন।
10. **Step 9: Bridge (`NativeWebBridgeSDK`)** - `WebBridge` তৈরি, `@JavascriptInterface` বাইন্ডিং এবং লোকালহোস্ট ইউআরএল লোড করা।

---

## ৪. ডেভেলপার কুইক-স্টার্ট (Quick Start Guide)

একটি ক্লায়েন্ট অ্যাপ্লিকেশনে পুরো রানটাইম বুট করার জন্য:

```java
import com.roni.library.sdk.NativeRuntime;
import com.roni.library.runtime.source.WebSource;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 1. Initialize NativeRuntime Facade
        NativeRuntime runtime = NativeRuntime.getInstance();
        
        // 2. Configure Web Asset Source & Boot Subsystems
        WebSource webSource = WebSource.fromAssets("dist", "index.html");
        runtime.initialize(webSource, true /* enableDevTools */);
        
        // 3. System is now ready for WebBridge RPC dispatch!
    }
}
```

---

## ৫. মূল পর্যবেক্ষণ ও আর্কিটেকচারাল মূল্যায়ন (Architectural Assessment)

- **ডিকাপলড আর্কিটেকচার:** কোডবেসটি অসাধারণ কৌশলে পিওর জাভা অ্যাবস্ট্রাকশনের মাধ্যমে তৈরি করা হয়েছে। ফলে কোড সরাসরি সিআই/সিডি এবং জেভিএম টেস্টে কোনো ভারী এমুলেটর ছাড়াই বিদ্যুৎ গতিতে ভেরিফাই করা যায়।
- **প্রোডাকশন ফাঁক:** `consumer-rules.pro` ফাইলগুলো ডিক্লেয়ার থাকা সত্ত্বেও আর্কাভে ছিল না। বাস্তব ডিভাইসে চালানোর সময় অ্যান্ড্রয়েড ব্রডকাস্ট রিসিভার ও অ্যান্ড্রয়েডভিউ উইজেট ইমপ্লিমেন্টেশনের ব্রিজ তৈরি করতে হবে।

প্রতিটি লাইব্রেরির বিস্তারিত ডকুমেন্টেশন `documentation/libraries/` ডিরেক্টরিতে সংরক্ষিত আছে।
