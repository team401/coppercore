# standalone

A scratch space for **standalone Java programs** (classes with a `main` method) that
compile and run against the coppercore library modules and everything they depend on
(WPILib, AdvantageKit, and vendordeps such as CTRE Phoenix).

This subproject is **never published** to Maven Central and is **not bundled** into any
released coppercore artifact. No library module depends on it, and the publishing plugin
is disabled for it in the root `build.gradle`, so it cannot affect building or publishing
the library.

## Adding a program

Put `.java` files under `src/main/java`, in whatever packages you like. Any of the
coppercore modules (`geometry`, `math`, `controls`, `parameter_tools`, `monitors`,
`vision`, `metadata`, `wpilib_interface`) can be imported directly, along with WPILib and
the vendordeps.

### Included examples

- `coppercore.standalone.HelloCopperCore` — minimal template; uses `coppercore.geometry.Point`.
- `coppercore.standalone.TransformJsonDump` — serializes a WPILib `Transform3d` and
  `Transform2d` to JSON with coppercore's `parameter_tools` JSON framework and prints it.

## Running a program

### With the wrapper script (recommended)

`run.sh` takes the fully-qualified main class and any program arguments. It runs Gradle
quietly so only your program's output appears, and works from any directory:

```bash
standalone/run.sh coppercore.standalone.TransformJsonDump
standalone/run.sh my.pkg.MyProgram foo bar
```

### With Gradle directly

Specify the fully-qualified main class with `-PmainClass=`:

```bash
./gradlew :standalone:run -PmainClass=coppercore.standalone.HelloCopperCore
```

Pass program arguments with `--args`:

```bash
./gradlew :standalone:run -PmainClass=my.pkg.MyProgram --args="foo bar"
```

Either way, the `run` task extracts the WPILib and vendordep JNI native libraries and puts
them on `java.library.path` (and the OS loader path), so programs that touch native code —
e.g. WPILib math/HAL or Phoenix devices in simulation — work from the command line.

> The wrapper is a bash script (Linux/macOS, or Git Bash/WSL on Windows). On native
> Windows shells, use the `./gradlew` form above.

## Vendordeps

GradleRIO loads vendordeps from this project's own `vendordeps/` directory. Rather than
committing a second copy that could drift, the build **mirrors** the library modules'
`*/vendordeps/*.json` into this project's `vendordeps/` whenever a `:standalone` task is
run (just before GradleRIO is applied), so it is always in sync with the modules. That
directory is git-ignored and regenerated — never edit or commit it. To change vendordeps,
edit them in the owning module (e.g. `wpilib_interface/vendordeps/`); `standalone` picks
the change up automatically.

## Notes

- `standalone` is configured only when one of its own tasks is requested, so it is **not**
  part of the aggregate `./gradlew build` and a program that fails to compile here can never
  break the library build or publishing. Use `./gradlew :standalone:build` to compile-check
  this module on its own.
- Native libraries default to the **release** variant. Set `wpi.java.debugJni = true` in
  `build.gradle` to use debug natives.
- The simulation GUI and driver station are disabled by default so programs run headless.
