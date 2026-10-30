# Ecosystem Blueprint & Complete Dependency Matrix

> **ডকুমেন্ট আইডি:** DOC-ARCH-01  
> **ভাষা:** বাংলা (Bangla)  
> **বিশ্লেষণ ভিত্তি:** `build.gradle` ফাইলসমূহ এবং `NativeRuntimeLayeredArchitectureDiagram_87a563dc.png`

---

## ১. লেয়ার ট্রানজিশন রুলস (Layer Rules & Directionality)

আর্কিটেকচার ডায়াগ্রাম অনুসারে নিচের কঠোর নিয়মাবলী বলবৎ:

1. **আপওয়ার্ড কলিং সীমাবদ্ধতা:** নিম্ন লেয়ার কখনো উচ্চ লেয়ারের ওপর ডিপেন্ড করতে পারবে না (লেয়ার ০ কোনো লেয়ারের ওপর ডিপেন্ডেন্ট নয়)।
2. **Layer 0 (Universal Foundation Contracts):** সমস্ত ইঞ্জিন ও এসডিকে এই ইন্টারফেস ইমপ্লিমেন্ট বা ইনহেরিট করবে।
3. **Layer 1 (Core Infrastructure):** `NativeServiceSDK` এবং `NativeLoggerSDK` সমস্ত ডোমেইন ইঞ্জিনের লাইফসাইকেল এবং ট্র্যাকিং নিয়ন্ত্রণ করে।
4. **Layer 2 (Domain Engines):** সমস্ত বিজনেস লজিক বহন করে এবং হ্যান্ডলারের মাধ্যমে Layer 3 এবং Layer 4-এ নিজেকে এক্সপোজ করে।
5. **Layer 3 (Bridge):** ওয়েব ও জাভার মধ্যে আরপিসি মেসেজ ডিকোড ও সিরিয়ালাইজ করে।
6. **Layer 4 (Master Orchestration):** একক পয়েন্ট অফ এন্ট্রি প্রদান করে।

---

## ২. পূর্ণাঙ্গ ১৫x১৫ ডিপেনডেন্সি ম্যাট্রিক্স (15x15 Dependency Matrix)

চিহ্ন নির্দেশিকা:
- **`D` (Direct Dependency):** `build.gradle`-এ `api` অথবা `implementation` হিসেবে ঘোষিত।
- **`C` (CompileOnly):** শুধু কম্পাইল টাইমে প্রয়োজন।
- **`T` (TestImplementation):** টেস্ট চালনার জন্য ডিপেন্ডেন্ট।
- **`-` (None):** কোনো সরাসরি ডিপেনডেন্সি নেই।

| ক্র. | লাইব্রেরি নাম | 01 | 02 | 03 | 04 | 05 | 06 | 07 | 08 | 09 | 10 | 11 | 12 | 13 | 14 | 15 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 01 | `NativeRuntimeContracts` | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 02 | `NativeServiceSDK` | **D** | - | **D** | - | - | - | - | - | - | - | - | - | - | - | - |
| 03 | `NativeLoggerSDK` | **D** | - | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 04 | `NativeRouterSDK` | **D** | **D** | **D** | - | - | - | - | - | - | - | - | - | - | - | - |
| 05 | `NativePlatformEngine` | **D** | **D** | **C** | - | - | - | - | - | - | - | - | - | - | - | - |
| 06 | `NativeStorageEngine` | **D** | **D** | **T** | **T** | - | - | - | - | - | - | - | - | - | - | - |
| 07 | `NativeSecurityEngine` | **D** | **D** | **T** | **T** | - | - | - | - | - | - | - | - | - | - | - |
| 08 | `NativeNetworkEngine` | **D** | **D** | **T** | **T** | - | - | - | - | - | - | - | - | - | - | - |
| 09 | `NativeAnalyticsEngine` | **D** | **D** | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 10 | `NativeThemeEngine` | **D** | - | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 11 | `NativeDevToolsSDK` | **D** | **D** | **T** | **T** | - | - | - | **T** | - | - | - | - | - | - | - |
| 12 | `NativeRuntimeEngine` | **D** | - | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 13 | `NativeRuntimeUI` | **D** | - | - | - | - | - | - | - | - | - | - | - | - | - | - |
| 14 | `NativeWebBridgeSDK` | **D** | **D** | **D** | **D** | - | - | - | - | - | - | - | - | - | - | - |
| 15 | `NativeRuntimeSDK` | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | **D** | - |

---

## ৩. সার্কুলার ডিপেনডেন্সি অডিট (Circular Dependency Audit)

- **অডিট ফলাফল:** কোনো সার্কুলার ডিপেনডেন্সি (Circular Dependency / Cyclic Loop) নেই!
- **বিশ্লেষণ:** 
  - `NativeRuntimeContracts` সম্পূর্ণ স্বাধীন (Zero Dependency Root)।
  - `NativeServiceSDK` ও `NativeLoggerSDK` একে অপরের সাথে ক্লিয়ার ইউনি-ডিরেকশনাল সম্পর্কে যুক্ত (`NativeServiceSDK` ব্যবহার করে `NativeLoggerSDK`)।
  - `NativeRuntimeSDK` শীর্ষ লেয়ারে থেকে বাকি ১৪টি মডিউলকে অ্যাসেম্বল করে।
  - ফলস্বরূপ, একটি পরিষ্কার Directed Acyclic Graph (DAG) গঠিত হয়েছে যা বিল্ড টুল এবং কমপ্যাক্ট আর৮ অপ্টিমাইজেশনের জন্য অত্যন্ত উপযোগী।
