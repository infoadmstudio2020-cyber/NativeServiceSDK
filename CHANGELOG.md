# Changelog

All notable changes to NativeServiceSDK are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com).

## [1.0.1] — 2026-10-30

### Added
- Integrated Layer 0 universal contracts `com.github.infoadmstudio2020-cyber:NativeRuntimeContracts:v1.0.1`.
- Integrated Layer 1 unified logging `com.github.infoadmstudio2020-cyber:NativeLoggerSDK:1.0.1`.
- `IService` now implements `com.roni.library.contracts.service.IService`.
- `ServiceRegistry` now implements `com.roni.library.contracts.service.IServiceRegistry` and `com.roni.library.contracts.common.IDisposable`.
- `ServiceCommandHandler` now implements `com.roni.library.contracts.api.IApiHandler` with `NativeApiRequest` / `NativeApiResponse`.
- Builtin telemetry services `BatteryStatusService` and `DeviceInfoService` provide contract DTO adapters (`toContractDTO()`).
- Complete implementation of `ServiceRegistry` microkernel with concurrent thread-safe service lookup and priority sorting.
- Implemented `BaseService` lifecycle state machine (`CREATED`, `INITIALIZED`, `RUNNING`, `STOPPED`, `DISPOSED`).
- Implemented `NativeServiceSDK` master facade, `NativeServiceSDKConfig.Builder`, and `NativeServiceSDKResult<T>`.
- Added automated `@ServiceInjector.InjectService` dependency injection utility.
- Created interactive companion demo Android application in `app` module with live diagnostic logging.
- Comprehensive automated JUnit 4 test suite with 100% pass rate.
- Modernized GitHub Actions release workflow with `permissions: contents: write` and `softprops/action-gh-release@v2`.
- Verified JitPack publishing configuration with `maven-publish` producing Release AAR, Sources JAR, and Javadoc JAR.


## [Unreleased]

### Planned
- Additional features
- Performance improvements
