# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this project is

One repository producing **two things from one shared `TeamCode`**: the Android FTC robot
app, and a JavaFX desktop simulator. An OpMode written once builds for both — deployed to a
Control Hub, or run on a PC against a 2D physics simulation.

The simulator is a fork of [`Beta8397/virtual_robot`](https://github.com/Beta8397/virtual_robot)
brought in as a Gradle module.

## Commands

```bash
./gradlew :Controller:run              # launch the simulator (JavaFX desktop app)
./gradlew :Controller:compileJava      # compile the simulator + shared OpModes
./gradlew :TeamCode:assembleDebug      # build the robot APK
```

There is **no test suite** — no test source sets exist anywhere. Do not invent test commands.

### The JDK gotcha — read before running Gradle

Gradle 9.1 requires **JDK 17+**, but the `java` on PATH here is **1.8** and `JAVA_HOME` is
unset, so `./gradlew` fails from a plain shell with *"Gradle requires JVM 17 or later"*.
Android Studio works because it uses its bundled JBR. From a shell, scope `JAVA_HOME` to it:

```bash
JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew :Controller:run
```

Stop the Gradle daemon (`./gradlew --stop`) before any history rewrite — it locks
`Controller/libs/*.jar` and `git reset`/`checkout` then cannot unlink them.

## Architecture

| Module | Plugin | Produces |
|---|---|---|
| `:FtcRobotController` | `com.android.library` | stock FTC SDK app shell |
| `:TeamCode` | `com.android.application` | the robot APK; team OpModes live here |
| `:Controller` | `application` + `org.openjfx.javafxplugin` | the desktop simulator |

Android side: FTC SDK 11.2.1, AGP 8.13.2, `compileSdk 34`, `minSdk 24`, Java 8.
Simulator: plain Java, no toolchain pinned — it uses whatever JVM runs Gradle.

### The central invariant

**`:Controller` must never depend on `:FtcRobotController` or the real FTC AARs.** It carries
its own hand-written, source-level *approximation* of the FTC SDK under `Controller/src` in the
real package names (`com.qualcomm.**`, `org.firstinspires.ftc.**`). The two targets compile the
same OpMode sources against two different definitions of the SDK; they never share a classpath,
which is what stops the duplicate classes colliding. (The FTC artifacts are Android AARs anyway,
which a plain-Java module cannot consume.)

Corollary: **do not change a signature in the approximation to make something compile.** It
mirrors the real SDK deliberately; diverging breaks the team code it exists to serve. Add the
missing member instead, copying the real SDK's own implementation.

### Shared OpModes are copied, not referenced

`:TeamCode` compiles `TeamCode/src/main/java` directly. `:Controller` cannot also point a source
root there — IDEs require every source root to belong to exactly one module, and sharing it makes
Android Studio report *"Duplicate content roots detected"* and pull the folder out of `:TeamCode`.

So `syncSharedOpModes` (a `Sync` task) copies them into `Controller/build/shared-opmodes`, and
that is the simulator's source root. **Simulator compile errors name files under
`build/shared-opmodes/…` — the file to edit is the original in `TeamCode`.**

If an OpMode cannot compile for the simulator, add its path to `simIncompatibleOpModes` in
`Controller/build.gradle` (relative to `TeamCode/src/main/java`, `**` allowed). That only
excludes it from the simulator; it still deploys to the robot. Currently empty.

### PedroPathing is real, not stubbed

Paths genuinely follow in the simulator. `com.pedropathing:core` is a plain jar used directly;
`ftc`, `telemetry` and `ivy` are AARs a plain-Java module can't consume, so `extractPedroPathing*`
tasks unpack each `classes.jar` onto the classpath, where their `com.qualcomm.*` calls bind to
the approximation.

**Versions are declared twice and must stay in sync**: `pedroPathingVersion` /
`pedroPathingTelemetryVersion` / `pedroPathingIvyVersion` in `Controller/build.gradle`, against
the `com.pedropathing:*` lines in `build.dependencies.gradle`.

### Stand-ins for things the simulator has no equivalent of

Under `Controller/src`: Limelight (`com.qualcomm.hardware.limelightvision`), Panels
(`com.bylazar.*`), FTC Dashboard (`com.acmerobotics.dashboard`), and enough `android.*` /
`androidx.*` / `org.json` to compile SDK-shaped code off-device. These accept every call and
report nothing detected — there is no camera and no web dashboard. They exist so OpModes using
them compile and run; they are not features.

Some `android.*` shims are load-bearing for the approximation itself (`Context` for
`HardwareMap.appContext`, `Canvas` for the AprilTag processor signatures, `Log` for `RobotLog`,
the `androidx.annotation` set). Others exist purely for team code. Don't delete them as dead
weight.

### Runtime discovery

OpModes are found by scanning the classpath at run time for `@TeleOp` / `@Autonomous` — the same
annotations the Driver Station uses, so nothing needs registering. Robot configurations are found
the same way, scanning `virtual_robot.robots.classes` for `@BotConfig`. `ConfigUtilities`
reports the robot picked in the simulator's dropdown, because the simulator emulates the Robot
Controller's settings store rather than asking team code to branch on where it runs.

`Controller/src/virtual_robot/config/Config.java` holds run-time options — `USE_VIRTUAL_GAMEPAD`
(virtual panel vs real USB gamepads), field size and image, and `GAME` (`new NoGame()` removes
field obstacles).

## Documentation

- `Controller/README.md` — the detailed module guide: what the simulator can and cannot do, the
  robot configurations, settings, and how the two builds differ. Keep it current when changing
  simulator capability.
- Root `README.md` is **upstream FtcRobotController's file**, rewritten by every SDK merge. It
  carries only a short pointer section to `Controller/README.md`; keep additions there minimal,
  since every added line is a future merge conflict.
