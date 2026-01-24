# Analysis of 2026 Changes in Vision Template

This document summarizes all changes made to `template_projects/sources/vision/` for the 2026 season.

## 1. WPILib 2026 API Change

**Commit:** `42b2f85`
**File:** `src/main/java/frc/robot/Robot.java:111`

Changed command scheduling syntax:

```java
// Before (2025)
autonomousCommand.schedule();

// After (2026)
CommandScheduler.getInstance().schedule(autonomousCommand);
```

This reflects a WPILib API change where commands no longer have a direct `schedule()` method.

---

## 2. Modern Switch Statement Syntax

**Commit:** `74edc6b`
**File:** `src/main/java/frc/robot/Robot.java:36-42`

Modernized metadata logging using Java switch expressions:

```java
// Before (verbose switch/case/break)
switch (BuildConstants.DIRTY) {
  case 0:
    Logger.recordMetadata("GitDirty", "All changes committed");
    break;
  case 1:
    Logger.recordMetadata("GitDirty", "Uncommitted changes");
    break;
  default:
    Logger.recordMetadata("GitDirty", "Unknown");
    break;
}

// After (expression syntax)
Logger.recordMetadata(
    "GitDirty",
    switch (BuildConstants.DIRTY) {
      case 0 -> "All changes committed";
      case 1 -> "Uncommitted changes";
      default -> "Unknown";
    });
```

---

## 3. Array Conversion Optimization

**Commit:** `d4d21d0`
**File:** `src/main/java/frc/robot/subsystems/vision/Vision.java:143-167`

Changed `toArray()` calls to use zero-length arrays for better performance/idiom:

```java
// Before
tagPoses.toArray(new Pose3d[tagPoses.size()])
robotPoses.toArray(new Pose3d[robotPoses.size()])
robotPosesAccepted.toArray(new Pose3d[robotPosesAccepted.size()])
robotPosesRejected.toArray(new Pose3d[robotPosesRejected.size()])

// After
tagPoses.toArray(new Pose3d[0])
robotPoses.toArray(new Pose3d[0])
robotPosesAccepted.toArray(new Pose3d[0])
robotPosesRejected.toArray(new Pose3d[0])
```

This is considered more idiomatic in modern Java and can be faster due to JVM optimizations.

---

## 4. Copyright Year Update

**Commit:** `49b56c9`
**Files:** All `.java` files in the vision template

Updated copyright header from `2021-2025` to `2021-2026`.

---

## 5. PhotonLib Vendor Dependency

**Commit:** `5dbc08a`
**File:** `template_projects/vendordeps/photonlib.json`

Updated PhotonLib vendor dependency to 2026 version. This affects vision functionality but the change is in the shared vendordeps directory, not in the vision source directly.

---

## Summary

The vision template received minimal breaking changes for 2026:

| Category | Impact |
|----------|--------|
| API Breaking Change | `Command.schedule()` → `CommandScheduler.getInstance().schedule()` |
| Code Modernization | Switch expressions, array conversion idioms |
| Dependency Updates | PhotonLib 2026 |

Teams upgrading from 2025 should primarily watch for the `schedule()` API change, as this will cause compilation errors until updated.
