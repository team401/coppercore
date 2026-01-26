# PhotonVision Java Client Migration Guide: v2025.3.1 → v2026.1.1

This guide focuses exclusively on changes that affect **Java robot code** consuming PhotonVision data in FRC applications.

---

## Required Changes (Breaking API Changes)

### 1. PhotonPoseEstimator Constructor Change

**Status**: ⚠️ **BREAKING CHANGE** - Code updates required

**What Changed**:
The `PhotonPoseEstimator` constructor and usage pattern has been completely redesigned.

#### Old API (v2025.3.1) - DEPRECATED:
```java
// Old constructor - now deprecated
PhotonPoseEstimator estimator = new PhotonPoseEstimator(
    aprilTagFieldLayout,           // Field layout
    PoseStrategy.LOWEST_AMBIGUITY, // Strategy enum
    camera,                         // PhotonCamera instance
    robotToCamera                   // Transform3d
);

// Old update method - now deprecated
Optional<EstimatedRobotPose> result = estimator.update();
```

#### New API (v2026.1.1) - RECOMMENDED:
```java
// New simplified constructor (strategy and camera removed)
PhotonPoseEstimator estimator = new PhotonPoseEstimator(
    aprilTagFieldLayout,  // Field layout
    robotToCamera         // Transform3d
);

// New: Call specific strategy methods with explicit result
PhotonPipelineResult pipelineResult = camera.getLatestResult();
Optional<EstimatedRobotPose> result = estimator.estimateLowestAmbiguityPose(pipelineResult);
```

**Why It Changed**:
- More explicit control over which strategy to use per update
- Eliminates hidden state (no cached camera reference)
- Clearer separation of concerns

**Migration Steps**:
1. Remove `PoseStrategy` parameter from constructor
2. Remove `PhotonCamera` parameter from constructor
3. Replace `estimator.update()` calls with specific strategy methods
4. Pass `PhotonPipelineResult` explicitly to strategy methods

---

### 2. Pose Estimation Strategy Methods

**Status**: 🆕 **NEW API** - Old methods deprecated

The strategy enum pattern has been replaced with individual methods:

#### Available Strategy Methods:

##### a) Lowest Ambiguity (most common)
```java
/**
 * Choose the pose with the lowest ambiguity
 */
Optional<EstimatedRobotPose> estimateLowestAmbiguityPose(PhotonPipelineResult result)
```

##### b) Multi-Tag on Coprocessor
```java
/**
 * Use coprocessor's multi-tag PNP result
 * Requires multi-tag to be enabled in PhotonVision web UI
 */
Optional<EstimatedRobotPose> estimateCoprocMultiTagPose(PhotonPipelineResult result)
```

##### c) Distance + Heading (NEW!)
```java
/**
 * Use distance to best tag + robot heading to estimate pose
 * Requires periodic calls to addHeadingData()
 */
Optional<EstimatedRobotPose> estimatePnpDistanceTrigSolvePose(PhotonPipelineResult result)
```

**Deprecated Strategy Methods** (still work but marked for removal):
- `PoseStrategy.CLOSEST_TO_CAMERA_HEIGHT`
- `PoseStrategy.CLOSEST_TO_REFERENCE_POSE`
- `PoseStrategy.CLOSEST_TO_LAST_POSE`
- `PoseStrategy.AVERAGE_BEST_TARGETS`
- `PoseStrategy.MULTI_TAG_PNP_ON_RIO`

---

## Migration Examples

### Example 1: Basic Vision Subsystem (Lowest Ambiguity)

#### Before (v2025.3.1):
```java
public class VisionSubsystem extends SubsystemBase {
    private final PhotonCamera camera;
    private final PhotonPoseEstimator poseEstimator;

    public VisionSubsystem(AprilTagFieldLayout fieldLayout, Transform3d robotToCamera) {
        camera = new PhotonCamera("photonvision");
        poseEstimator = new PhotonPoseEstimator(
            fieldLayout,
            PoseStrategy.LOWEST_AMBIGUITY,
            camera,
            robotToCamera
        );
    }

    public Optional<EstimatedRobotPose> getEstimatedGlobalPose() {
        return poseEstimator.update();
    }
}
```

#### After (v2026.1.1):
```java
public class VisionSubsystem extends SubsystemBase {
    private final PhotonCamera camera;
    private final PhotonPoseEstimator poseEstimator;

    public VisionSubsystem(AprilTagFieldLayout fieldLayout, Transform3d robotToCamera) {
        camera = new PhotonCamera("photonvision");
        poseEstimator = new PhotonPoseEstimator(
            fieldLayout,
            robotToCamera  // Only 2 parameters now
        );
    }

    public Optional<EstimatedRobotPose> getEstimatedGlobalPose() {
        var result = camera.getLatestResult();
        return poseEstimator.estimateLowestAmbiguityPose(result);
    }
}
```

---

### Example 2: Multi-Tag Pose Estimation

#### Before (v2025.3.1):
```java
poseEstimator = new PhotonPoseEstimator(
    fieldLayout,
    PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
    camera,
    robotToCamera
);
Optional<EstimatedRobotPose> pose = poseEstimator.update();
```

#### After (v2026.1.1):
```java
poseEstimator = new PhotonPoseEstimator(fieldLayout, robotToCamera);

var result = camera.getLatestResult();
Optional<EstimatedRobotPose> pose = poseEstimator.estimateCoprocMultiTagPose(result);
```

---

### Example 3: Using Distance-Based Estimation (NEW!)

This new strategy uses distance to the best visible tag plus robot heading data:

```java
public class VisionSubsystem extends SubsystemBase {
    private final PhotonCamera camera;
    private final PhotonPoseEstimator poseEstimator;
    private final Supplier<Rotation2d> headingSupplier;  // e.g., from gyro

    public VisionSubsystem(
        AprilTagFieldLayout fieldLayout,
        Transform3d robotToCamera,
        Supplier<Rotation2d> headingSupplier
    ) {
        camera = new PhotonCamera("photonvision");
        poseEstimator = new PhotonPoseEstimator(fieldLayout, robotToCamera);
        this.headingSupplier = headingSupplier;
    }

    @Override
    public void periodic() {
        // REQUIRED: Feed heading data every loop
        poseEstimator.addHeadingData(
            Timer.getFPGATimestamp(),
            headingSupplier.get()
        );
    }

    public Optional<EstimatedRobotPose> getEstimatedGlobalPose() {
        var result = camera.getLatestResult();
        return poseEstimator.estimatePnpDistanceTrigSolvePose(result);
    }

    // Call this when you reset your pose/gyro
    public void resetPose(Pose2d newPose) {
        poseEstimator.resetHeadingData(
            Timer.getFPGATimestamp(),
            newPose.getRotation()
        );
    }
}
```

---

### Example 4: Integrating with SwerveDrivePoseEstimator

This is the typical FRC use case - feeding vision measurements into the drive pose estimator:

#### Before (v2025.3.1):
```java
@Override
public void periodic() {
    // Old API
    var visionPose = visionSubsystem.getEstimatedGlobalPose();

    visionPose.ifPresent(pose -> {
        poseEstimator.addVisionMeasurement(
            pose.estimatedPose.toPose2d(),
            pose.timestampSeconds
        );
    });
}
```

#### After (v2026.1.1):
```java
@Override
public void periodic() {
    // New API - same usage pattern, just different internal implementation
    var visionPose = visionSubsystem.getEstimatedGlobalPose();

    visionPose.ifPresent(pose -> {
        poseEstimator.addVisionMeasurement(
            pose.estimatedPose.toPose2d(),
            pose.timestampSeconds
        );
    });
}
```

**Note**: The `EstimatedRobotPose` structure hasn't changed, so integration with `SwerveDrivePoseEstimator` or `DifferentialDrivePoseEstimator` remains the same.

---

## New Features (Optional)

### 1. Heading Data Management

**New Methods**:
```java
/**
 * Add robot heading data. Required for PNP_DISTANCE_TRIG_SOLVE strategy.
 * Call this every loop if using distance-based estimation.
 */
void addHeadingData(double timestampSeconds, Rotation2d heading)
void addHeadingData(double timestampSeconds, Rotation3d heading)

/**
 * Clear heading buffer and add new seed.
 * Use after pose/rotation resets to prevent stale heading data.
 */
void resetHeadingData(double timestampSeconds, Rotation2d heading)
void resetHeadingData(double timestampSeconds, Rotation3d heading)
```

**When to Use**:
- `addHeadingData()`: Call in `periodic()` if using `estimatePnpDistanceTrigSolvePose()`
- `resetHeadingData()`: Call when you reset your robot's pose or gyro

---

### 2. FPS Limiting (Per-Camera)

**New Feature**: Control camera frame rate from robot code via NetworkTables.

**PhotonCamera Methods** (inferred from NT topic changes):
```java
// Note: These are NetworkTables topics, access via:
NetworkTableInstance.getDefault()
    .getTable("photonvision")
    .getSubTable("your-camera-name")
    .getIntegerTopic("fpsLimitRequest")
    .publish()
    .set(30);  // Set to desired FPS

// To read current FPS limit:
long currentFps = NetworkTableInstance.getDefault()
    .getTable("photonvision")
    .getSubTable("your-camera-name")
    .getIntegerTopic("fpsLimit")
    .subscribe(-1)
    .get();
```

**Use Case**: Reduce processing load by limiting FPS during non-critical match periods.

---

### 3. Version Checking Control

**New Method**:
```java
/**
 * Disable version mismatch warnings.
 * Use with caution - ensure PhotonVision and PhotonLib versions are compatible.
 */
PhotonCamera.setVersionCheckEnabled(boolean enabled)
```

**Why This Matters**:
- Previously, version mismatches between PhotonLib and WPILib would throw exceptions
- Now version checking is relaxed and can be disabled entirely
- Useful if you need to use a newer PhotonLib with an older WPILib version (or vice versa)

**Example**:
```java
// Disable version warnings (do this before creating PhotonCamera instances)
PhotonCamera.setVersionCheckEnabled(false);

PhotonCamera camera = new PhotonCamera("photonvision");
```

---

## Dependency Updates

### Update build.gradle

Update your PhotonLib version in `build.gradle`:

```gradle
dependencies {
    // Update from 2025.x.x to 2026.x.x
    implementation "org.photonvision:photonlib-java:2026.1.1"
    implementation "org.photonvision:photontargeting-java:2026.1.1"
}
```

### WPILib Version

PhotonVision v2026.1.1 is designed for **WPILib 2026.2.1**, but version checking is now relaxed:

```gradle
plugins {
    id "edu.wpi.first.GradleRIO" version "2026.2.1"
}
```

**Compatibility Note**: Unlike v2025.3.1, the version check is no longer strict. Mismatched versions will warn but not throw exceptions.

---

## Summary of Required Code Changes

### ✅ Must Do:
1. **Update PhotonPoseEstimator constructor**: Remove `PoseStrategy` and `PhotonCamera` parameters
2. **Replace `update()` calls**: Use specific strategy methods like `estimateLowestAmbiguityPose()`
3. **Pass PhotonPipelineResult explicitly**: Get result from camera and pass to strategy method
4. **Update dependencies**: Use PhotonLib 2026.1.1 and WPILib 2026.2.1

### 🆕 Consider Using:
1. **Distance-based estimation**: Try `estimatePnpDistanceTrigSolvePose()` if you have good heading data
2. **Heading management**: Use `resetHeadingData()` when resetting robot pose
3. **FPS limiting**: Reduce camera FPS during non-critical periods to save processing power

### ❌ No Longer Recommended:
1. **Strategy enum pattern**: `PoseStrategy.LOWEST_AMBIGUITY` still works but is deprecated
2. **Strategy setter methods**: `setPrimaryStrategy()`, `setMultiTagFallbackStrategy()` are deprecated
3. **Reference pose methods**: `setReferencePose()`, `setLastPose()` are deprecated

---

## Deprecated API Reference

These methods still work in v2026.1.1 but are marked for removal:

```java
// Deprecated constructors
@Deprecated(forRemoval = true, since = "2026")
public PhotonPoseEstimator(
    AprilTagFieldLayout fieldTags,
    PoseStrategy strategy,
    Transform3d robotToCamera
)

// Deprecated methods
@Deprecated(forRemoval = true, since = "2026")
public Optional<EstimatedRobotPose> update()

@Deprecated(forRemoval = true, since = "2026")
public PoseStrategy getPrimaryStrategy()

@Deprecated(forRemoval = true, since = "2026")
public void setPrimaryStrategy(PoseStrategy strategy)

@Deprecated(forRemoval = true, since = "2026")
public void setMultiTagFallbackStrategy(PoseStrategy strategy)

@Deprecated(forRemoval = true, since = "2026")
public Pose3d getReferencePose()

@Deprecated(forRemoval = true, since = "2026")
public void setReferencePose(Pose3d referencePose)

@Deprecated(forRemoval = true, since = "2026")
public void setLastPose(Pose3d lastPose)
```

**Migration Timeline**: Plan to update before these methods are removed in a future release.

---

## Quick Migration Checklist

- [ ] Updated `build.gradle` to PhotonLib 2026.1.1
- [ ] Updated to WPILib 2026.2.1 (or verified compatibility)
- [ ] Removed `PoseStrategy` parameter from `PhotonPoseEstimator` constructor
- [ ] Removed `PhotonCamera` parameter from `PhotonPoseEstimator` constructor
- [ ] Replaced `estimator.update()` with `estimator.estimateLowestAmbiguityPose(result)`
- [ ] Added `camera.getLatestResult()` calls before pose estimation
- [ ] Tested vision pose estimation on robot
- [ ] (Optional) Implemented `addHeadingData()` if using distance-based estimation
- [ ] (Optional) Added `resetHeadingData()` calls when resetting robot pose

---

## Additional Resources

- Full change analysis: `claude-analysis-of-photonvision-changes.md`
- PhotonVision docs: https://docs.photonvision.org
- PhotonLib Javadocs: https://photonvision.github.io/photonvision/
- Example code: `photonlib-java-examples/` in PhotonVision repository

---

## Questions & Troubleshooting

### Q: My old code still compiles. Do I need to change it?
**A**: Yes, eventually. The old API is marked `@Deprecated(forRemoval = true)`, meaning it will be removed in a future version. Migrate now to avoid breaking changes later.

### Q: Which strategy method should I use?
**A**:
- **Single tags or general use**: `estimateLowestAmbiguityPose()`
- **Multiple tags visible**: `estimateCoprocMultiTagPose()` (requires PhotonVision web UI config)
- **Good gyro + single tag**: `estimatePnpDistanceTrigSolvePose()` (requires `addHeadingData()` calls)

### Q: Do I need to call `addHeadingData()` for all strategies?
**A**: No, only for `estimatePnpDistanceTrigSolvePose()`. Other strategies don't use heading data.

### Q: Can I still use multiple strategies in the same code?
**A**: Yes! The new API makes this easier:
```java
var result = camera.getLatestResult();

// Try multi-tag first
var pose = poseEstimator.estimateCoprocMultiTagPose(result);

// Fall back to single-tag if multi-tag fails
if (pose.isEmpty()) {
    pose = poseEstimator.estimateLowestAmbiguityPose(result);
}
```

### Q: What if I get version mismatch warnings?
**A**:
1. First, try to update all dependencies to matching versions
2. If needed, you can disable warnings with `PhotonCamera.setVersionCheckEnabled(false)`
3. Test thoroughly - version mismatches may cause unexpected behavior

### Q: My pose estimator isn't returning results anymore
**A**: Check that you're passing the `PhotonPipelineResult` correctly:
```java
// WRONG - won't compile with new constructor
Optional<EstimatedRobotPose> pose = poseEstimator.update();

// RIGHT
var result = camera.getLatestResult();
Optional<EstimatedRobotPose> pose = poseEstimator.estimateLowestAmbiguityPose(result);
```
