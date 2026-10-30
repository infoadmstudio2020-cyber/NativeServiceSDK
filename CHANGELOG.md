# Changelog

All notable changes to NativeServiceSDK are documented here.
Format based on [Keep a Changelog](https://keepachangelog.com).

## [1.0.0] — 2026-10-30

### Added
- Complete implementation of `ServiceRegistry` microkernel with concurrent thread-safe service lookup and priority sorting.
- Implemented `BaseService` lifecycle state machine (`CREATED`, `INITIALIZED`, `RUNNING`, `STOPPED`, `DISPOSED`).
- Implemented `NativeServiceSDK` master facade, `NativeServiceSDKConfig.Builder`, and `NativeServiceSDKResult<T>`.
- Added automated `@ServiceInjector.InjectService` dependency injection utility.
- Added built-in system telemetry services: `DeviceInfoService`, `BatteryStatusService`, and `NetworkStateService`.
- Added `ServiceCommandHandler` for dispatching native RPC command queries.
- Created interactive companion demo Android application in `app` module with live diagnostic logging.
- Comprehensive automated JUnit 4 test suite with 100% pass rate.
- Modernized GitHub Actions release workflow with `permissions: contents: write` and `softprops/action-gh-release@v2`.
- Verified JitPack publishing configuration with `maven-publish` producing Release AAR, Sources JAR, and Javadoc JAR.


## [Unreleased]

### Planned
- Additional features
- Performance improvements
