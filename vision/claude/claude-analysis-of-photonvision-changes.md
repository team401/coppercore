# PhotonVision Changes Analysis: v2025.3.1 to v2026.1.1

This document analyzes the changes between PhotonVision tags **v2025.3.1** and **v2026.1.1**, categorizing them into API-impacting changes and non-API changes.

## Summary Statistics

- **Total Commits**: 298 commits
- **Files Changed**: Extensive changes across Java, C++, Python libraries, documentation, and infrastructure
- **Major Version Change**: 2025 → 2026 (WPILib season update)

---

## Breaking API Changes

### 1. PhotonPoseEstimator Constructor and Strategy System Overhaul

**Impact**: HIGH - Requires code changes for all users of PhotonPoseEstimator

**All Languages (Java, C++, Python)**:
- **Old API**: Constructor required `PoseStrategy` parameter and `PhotonCamera` instance
  ```java
  // Old - DEPRECATED
  new PhotonPoseEstimator(fieldTags, PoseStrategy.LOWEST_AMBIGUITY, camera, robotToCamera)
  ```
- **New API**: Simplified 2-argument constructor; strategies moved to individual methods
  ```java
  // New - Recommended
  var estimator = new PhotonPoseEstimator(fieldTags, robotToCamera);
  var result = estimator.estimateLowestAmbiguityPose(cameraResult);
  ```

**Changed Methods** (now individual strategy methods):
- `estimateLowestAmbiguityPose(PhotonPipelineResult)`
- `estimateCoprocMultiTagPose(PhotonPipelineResult)`
- `estimatePnpDistanceTrigSolvePose(PhotonPipelineResult)` - **NEW**

**Deprecated Methods** (marked for removal in 2026):
- `update()` method
- `getPrimaryStrategy()` / `setPrimaryStrategy()`
- `getMultiTagFallbackStrategy()` / `setMultiTagFallbackStrategy()`
- `getReferencePose()` / `setReferencePose()`
- `setLastPose()` / `getLastPose()`

**Python Specific Changes**:
- Removed `PoseStrategy` enum entirely
- Removed `camera` parameter from constructor
- Removed internal `_update()`, `_multiTagOnCoprocStrategy()`, `_lowestAmbiguityStrategy()` methods

**References**:
- Python: `photon-lib/py/photonlibpy/photonPoseEstimator.py`
- Java: `photon-lib/src/main/java/org/photonvision/PhotonPoseEstimator.java`
- C++: `photon-lib/src/main/native/include/photon/PhotonPoseEstimator.h`

### 2. New Heading Data Management API

**Impact**: MEDIUM - Required for new PNP_DISTANCE_TRIG_SOLVE strategy

**All Languages**:
- **NEW**: `addHeadingData(timestamp, heading)` - Must be called periodically for distance-based pose estimation
- **NEW**: `resetHeadingData(timestamp, heading)` - Clear heading buffer (useful after pose resets)

**Purpose**: Enables trigonometric pose solving using distance and heading data

### 3. WPILib Version Checking Removed

**Impact**: MEDIUM - More flexible version compatibility

**Java**:
- Removed strict WPILib version requirement check that previously threw exceptions
- OpenCV version checking now only warns instead of throwing exceptions
- New method: `PhotonCamera.setVersionCheckEnabled(boolean)` to disable version warnings entirely

**Reference**: Commit a952bab4 "Remove strict WPILib version requirement (#2307)"

### 4. PhotonCamera FPS Limit Control

**Impact**: LOW - New feature, not breaking

**All Languages**:
- **NEW NetworkTables Topics**:
  - `fpsLimitRequest` (publish)
  - `fpsLimit` (subscribe)
- Latency/FPS settings moved from global to per-camera

**Reference**:
- Commit 224ce46f "Make the latency/fps setting per camera instead of global (#2260)"
- Commit 058ca192 "Publish FPS with camera (#2083)"

---

## New API Features (Non-Breaking)

### 1. New Pose Estimation Strategies

**C++/Java/Python**:
- `CONSTRAINED_SOLVEPNP` - Constrained PnP solving (C++ added in #1908)
- `PNP_DISTANCE_TRIG_SOLVE` - Distance-based trigonometric solving

**C++ Specific**:
```cpp
struct ConstrainedSolvepnpParams {
  bool headingFree{false};
  double headingScalingFactor{0.0};
};
```

### 2. Simulation API Enhancements

**All Languages**:
- Fixed rendering of AprilTags 30, 31, and 32 in simulated camera streams
- Improved `SimCameraProperties` and `VisionSystemSim`

**Reference**: Commit e0880509

### 3. HAL Usage Reporting

**Java/C++**:
- PhotonPoseEstimator now reports usage via `HAL.report()` for telemetry
- Instance counting starts at 1 instead of 0

---

## Hardware & Platform Support Changes

### 1. Rubik Pi 3 Support

**Impact**: HIGH - New hardware platform

**New Capabilities**:
- Object detection support on Rubik Pi 3 using RKNN (Rockchip Neural Network)
- RKNN model conversion tools and notebooks
- COCO trained models for RKNN
- Dedicated image building support

**References**:
- Commit 92779600 "Add support for object detection on Rubik Pi 3 (#2005)"
- Commit d341ebba "Initial hardware support for Rubik pi (#1989)"
- Commit 79793686 "Add support for building rubik image (#2110)"

### 2. Luma P1 Support

**Reference**: Commit ee4501f1 "Add Luma P1 support (#2135)"

### 3. Limelight 4 Support

**Reference**: Commit 4b01b66a "Add limelight 4 support (#1807)"

---

## Configuration & Settings Changes

### 1. Camera Focus Mode

**Impact**: MEDIUM - New user-configurable setting

**Reference**: Commit 618072c3 "Add Camera Focus Mode (#2180)"

### 2. Settings Migration from JSON to SQLite

**Impact**: LOW for users - Internal storage change

**Neural Network Model Settings** now stored in SQLite database instead of JSON

**Reference**: Commit cc7923ee "Migrate NNM Settings to SQLITE (#1894)"

### 3. Per-Camera Latency/FPS Settings

**Impact**: MEDIUM - UI and configuration change

Previously global settings are now configurable per-camera

**Reference**: Commit 224ce46f

---

## Object Detection Improvements

### 1. RKNN Model Support

- Conversion tools for ONNX → RKNN
- Quantization checking before import
- COCO pre-trained models
- NMS (Non-Maximum Suppression) slider exposed in UI

**References**:
- Commit ba1c0db7 "RKNN conversion tool (#2024)"
- Commit 2eb224a5 "Preload OD models before import to check quantization (#2056)"
- Commit 0ea108e1 "Expose and document NMS slider (#2028)"

### 2. 3D Mode Disabled for OD

**Reference**: Commit 831df409 "Disable 3d mode for OD (#2121)"

---

## NetworkTables & Data Exchange Changes

### 1. Metrics Publisher

**Impact**: LOW - New telemetry feature

**NEW**: PhotonVision metrics now published to NetworkTables
- Metrics moved to root PhotonVision table
- Metrics publisher hostname updates with system hostname changes
- Reduced log spam from metrics

**References**:
- Commit 7f6edcd5 "feat: add metrics publisher for NT (#1791)"
- Commit 02e6b6d3 "Move metrics subtable to root PV table (#2007)"
- Commit 758fbb91 "Update metrics publisher hostname when hostname is changed (#2008)"

### 2. WireShark Dissector

**Impact**: LOW - Debugging tool

**NEW**: WireShark dissector for PhotonVision NetworkTables protocol

**Reference**: Commit 98f4a864 "Add WireShark dissector (#2140)"

### 3. Serialization Fixes

**Impact**: MEDIUM - Bug fixes for data consistency

- Fixed Jackson deserialization of neural network configs
- Consistent serialization of neural network data

**References**:
- Commit c2433e03
- Commit 6285f1ee

---

## Build System & Dependencies

### 1. WPILib 2026 Update

**Impact**: HIGH - Season version update

- Updated to WPILib 2026.2.1
- Updated examples to 2026 season
- RobotPy 2026 beta support

**References**:
- Commit 8a141904 "WPILib 2026.2.1 (#2306)"
- Commit 9490c2f2 "Bump wpilib to 2026 beta (#2192)"
- Commit 07affd4f "RobotPy 2026 beta (#2237)"

### 2. Gradle & Build Improvements

- Upgraded to Gradle 8.14.3
- Fixed most Gradle deprecation warnings
- Improved build task organization
- Parallel Python workflow (build, test, deploy separated)

**References**:
- Commit 2ab7a2e3 "Bump Gradle to 8.14.3 (#2064)"
- Commit 373ed2ff "Fix most gradle deprecation warnings (#2093)"
- Commit afb73b39 "refactor: separate build, test, and deploy in Python workflow (#2308)"

### 3. Dependency Updates

- Javalin bumped
- numpy pinned to 2.3
- libcamera and mrcal versions updated
- macOS mrcal support added

**References**:
- Commit 85f155c7 "chore: Bump Javalin (#2126)"
- Commit 121433fd "Loosely pin numpy to 2.3 (#2303)"
- Commit a780c9dc "Add macOS mrcal support (#2264)"

---

## Python Library (photonlibpy) Specific Changes

### 1. Package Structure

- Unit tests no longer shipped in wheel
- Added `py.typed` marker for type checking support
- Improved buildAndTest.sh script

**References**:
- Commit 9011e285 "Stop shipping unit tests in photonlibpy wheel (#2309)"

### 2. Type Annotations

- More comprehensive type hints
- Support for mypy static type checking

---

## Testing & Quality Improvements

### 1. Playwright E2E Tests

**Impact**: LOW - Developer tooling

**Reference**: Commit 017b074e "Add playwright E2E tests (#2174)"

### 2. Smoketests Integration

**Impact**: LOW - CI improvement

Smoketests integrated into image build process

**Reference**: Commit 50f22859 "Integrate smoketests into image build (#2248)"

### 3. Test Fixes

- Disabled flaky Alerts test
- Fixed and re-enabled other flaky tests
- Download artifacts for testing examples in CI

**References**:
- Commit 4b740a54 "Disable Alerts test in PhotonCameraTest (#1969)"
- Commit dbbb00f9 "Reenable and fix flaky tests (#1837)"

---

## UI/UX Changes (Non-API)

### 1. Vue 3 Upgrade

**Impact**: NONE for users - Internal framework update

**Reference**: Commit bec80926 "Vue 3 Upgrade (#1900)"

### 2. Dark Mode

**Impact**: NONE for API

**Reference**: Commit fce54d12 "Dark mode and minor interface tweaks (#2016)"

### 3. Custom Theming

**Reference**: Commit b43d0dde "Add custom theming (#2081)"

### 4. UI Performance Optimization

- Reduced CPU usage
- Dynamic import of non-critical dependencies
- Progress bars for file uploads

**References**:
- Commit 9d7222a1 "Optimize UI CPU usage (#2168)"
- Commit d649a9cb "Use progress bar for file uploads (#2148)"

### 5. Camera Mismatch Detection

**NEW**: Banner showing when camera configuration doesn't match detected hardware

**Reference**: Commit 054ed8b6 "Add camera mismatch banner to dashboard (#1921)"

---

## Calibration Improvements

### 1. Calibration Format Caching

**Reference**: Commit dbd6eea4 "Cache requested calibration format outside of state store (#2310)"

### 2. Board Outliers Display

**Reference**: Commit fddff5db "Show board outliers in calibration info card (#1267)"

### 3. Calibration Resolution Fix

**Reference**: Commit 9f6d8caf "Fix calibration resolution default bug (#2156)"

### 4. Chessboard Discouragement

**NEW**: Message discouraging chessboard usage in favor of AprilTag/ChArUco

**Reference**: Commit cd502a22 "Add message discouraging chessboard usage (#2160)"

---

## System & Infrastructure Changes (Non-API)

### 1. System Monitoring Rewrite

**Impact**: LOW - Internal improvement

Switched from custom monitoring to OSHI (Operating System and Hardware Information) library

**Reference**: Commit 5409573f "Rewrite system monitoring to use OSHI (#2255)"

### 2. GPIO Library Change

**Impact**: LOW - Internal change

Switched to diozero for GPIO operations

**Reference**: Commit 467f22bf "Use diozero for GPIO (#2171)"

### 3. Hostname Management

- Fixed incorrect hostname on non-managed devices
- NT client name now matches hostname

**References**:
- Commit a585a1d3 "Fix incorrect hostname on non-managed devices (#2203)"
- Commit 1ee2ecb6 "Make NT client name the same as hostname (#2107)"

### 4. Network Manager Improvements

- Wait for NetworkManager on first call
- Null-check before getting MAC address
- Reduced log spam from network queries

**References**:
- Commit 7a884871
- Commit 0e33aef8
- Commit 29e18366

---

## Documentation Changes

### 1. Documentation Reorganization

- Camera configuration pages reorganized
- Added camera matching documentation
- Cross-compilation toolchain documentation
- Linting documentation added
- Driver mode documentation

**References**:
- Commit e754f594 "[docs] Reorganize camera configuration pages and add camera matching documentation (#1917)"
- Commit 36b43732 "Add cross-compilation toolchain to docs (#2172)"
- Commit 2cde701c "Add documentation for linting (#2166)"
- Commit d22abdfd "[docs] Document driver mode (#1890)"

### 2. Documentation Site Improvements

- Monorepo landing page
- Release and development docs versions
- Source code linking in Javadocs
- Updated dependencies

**References**:
- Commit 163dbe58 "feat!: monorepo landing page (#1868)"
- Commit c26a7cc5 "feat: release and development docs versions (#1872)"
- Commit ea9bd4ac "feat: link methods in javadocs to source code (#1866)"

### 3. Hardware Documentation

- RUBIK Pi wiring instructions
- Luma P1 documentation
- Updated coprocessor list
- Removed etcher from recommended flashers (use Raspberry Pi Imager)

**References**:
- Commit e993cca0
- Commit 1798b67d "Recommend Raspberry Pi Imager over Balena Etcher (#1858)"

---

## Bug Fixes (Non-API)

### 1. Rendering Fixes

- Fixed AprilTag 30, 31, 32 rendering in simulation
- Fixed "ArUco" and "ChArUco" spellings

**References**:
- Commit e0880509
- Commit 7d2c69db

### 2. Configuration Bugs

- Fixed pipeline type off-by-one error when creating new pipeline
- Fixed calibration array length bug

**References**:
- Commit 0c4c310c
- Commit 9a88e565

### 3. UI Bugs

- Fixed camera setup modal not closing
- Fixed slider going past bounds
- Fixed button theming for custom themes
- Force reload after restart

**References**:
- Commit cc7923ee
- Commit d44d9fbb
- Commit 369d10eb
- Commit 3f9e2a9f

### 4. Platform-Specific Fixes

- OSHI spamming failures on Windows fixed
- OV9281 resolution options when using libcamera

**References**:
- Commit 1bedadde
- Commit 8c7ca169

---

## Removed/Cleaned Up

### 1. Removed Features

- PhotonJNICommon in favor of CombinedRuntimeLoader
- Unused scripting system
- Manual links from README
- MacOS builds from releases
- Codecov from workflow

**References**:
- Commit 1bb05a0e
- Commit 00ca5e06
- Commit db591f72
- Commit 35c79b13
- Commit 354f4e94

### 2. Code Cleanup

- Refactored instanceof usage
- Use text blocks (Java feature)
- Use Math.hypot instead of manual calculation
- Use WPILib Pair instead of Apache Commons Pair
- Made various classes into records (Java)
- Removed blacklistedResIndices from HardwareConfig

**References**: Multiple commits in the "Miscellaneous clean up" series

---

## Migration Guide Summary

### For PhotonPoseEstimator Users:

**Before (v2025.3.1)**:
```java
PhotonPoseEstimator estimator = new PhotonPoseEstimator(
    fieldLayout,
    PoseStrategy.LOWEST_AMBIGUITY,
    camera,
    robotToCamera
);
Optional<EstimatedRobotPose> pose = estimator.update();
```

**After (v2026.1.1)**:
```java
PhotonPoseEstimator estimator = new PhotonPoseEstimator(
    fieldLayout,
    robotToCamera
);
PhotonPipelineResult result = camera.getLatestResult();
Optional<EstimatedRobotPose> pose = estimator.estimateLowestAmbiguityPose(result);
```

### For Users of PNP_DISTANCE_TRIG_SOLVE Strategy:

```java
PhotonPoseEstimator estimator = new PhotonPoseEstimator(fieldLayout, robotToCamera);

// In periodic/telemetry code:
estimator.addHeadingData(Timer.getFPGATimestamp(), gyro.getRotation2d());

// When getting pose:
PhotonPipelineResult result = camera.getLatestResult();
Optional<EstimatedRobotPose> pose = estimator.estimatePnpDistanceTrigSolvePose(result);
```

### Version Compatibility:

- Users can now use PhotonLib v2026.1.1 with WPILib versions that don't exactly match
- Strict version checking removed; warnings can be disabled with `PhotonCamera.setVersionCheckEnabled(false)`

---

## Conclusion

The v2025.3.1 to v2026.1.1 update includes:

**Major API Changes**:
1. PhotonPoseEstimator constructor and strategy system redesign (breaking)
2. New heading data management API
3. Relaxed version compatibility checking

**New Features**:
1. Rubik Pi 3 hardware support with RKNN object detection
2. New pose estimation strategies (CONSTRAINED_SOLVEPNP, PNP_DISTANCE_TRIG_SOLVE)
3. Per-camera FPS limiting
4. Camera focus mode control
5. Metrics publishing to NetworkTables

**Platform Updates**:
1. WPILib 2026 season update
2. Support for additional hardware (Luma P1, Limelight 4)
3. Improved build system and dependencies

**Non-Breaking Improvements**:
1. UI overhaul (Vue 3, dark mode, custom theming)
2. Enhanced documentation and developer tools
3. Better system monitoring and error handling
4. Calibration improvements

The most significant user-facing change is the PhotonPoseEstimator API redesign, which provides more flexibility and clearer semantics but requires code updates for existing users.
