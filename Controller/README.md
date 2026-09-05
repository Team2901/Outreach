# Virtual Robot simulator (`:Controller`)

A 2D simulator that runs OpModes on a PC, forked from
[`Beta8397/virtual_robot`](https://github.com/Beta8397/virtual_robot). It compiles the
*same* files as the Android `:TeamCode` module, so an OpMode written for a robot runs
here without modification — and vice versa.

Run it with the **`Run Simulator`** configuration in Android Studio, or:

```
./gradlew :Controller:run
```

Gradle needs a JDK 17+ to run, which Android Studio's bundled JBR already satisfies.
There is no separate JDK to install and no setup script.

## What it can do

**Run your OpModes unchanged.** Write them in `org.firstinspires.ftc.teamcode`,
annotate `@TeleOp` / `@Autonomous`, extend `OpMode` / `LinearOpMode`. They appear in the
simulator's Op Mode dropdown and on the Driver Station, from one copy of the source.

**Drive a robot with real physics.** Motor powers turn into motion through a rigid body
physics engine (dyn4j), including momentum, wheel slip and collisions with field
elements. Encoders, the IMU and the odometry computer report values derived from that
simulated motion, not from dead reckoning.

**Follow PedroPathing paths for real.** The simulator uses the *same* PedroPathing
artifacts a robot does, so autonomous paths genuinely run: the follower computes
corrections, the drive moves the robot, and the simulated Pinpoint feeds the pose back.
`Tuning` and its selector menu work here too. This is the main reason to simulate — you
can see whether a path clips an obstacle or ends facing the wrong way before trusting it
on the field. The Ivy command scheduler is here too, so OpModes built on
`com.pedropathing.ivy` run unchanged. Keep the `pedroPathingVersion`,
`pedroPathingTelemetryVersion` and `pedroPathingIvyVersion` constants in `build.gradle` in
sync with `build.dependencies.gradle`.

**Report the active robot configuration.** `ConfigUtilities.getRobotConfigurationName()`
returns whatever is selected in the **Configuration** dropdown (`"Mecanum Bot"`,
`"Claw Bot"`, ...), through the same code path it uses on a robot — the simulator
emulates the Robot Controller's settings store rather than asking team code to branch on
where it is running. Before a configuration is chosen it reports `"No Configuration"`.

**Fail like a real robot when hardware is missing.** Each configuration declares its own
devices. Asking for one it does not have raises the same error the Robot Controller
raises when the Control Hub is configured without it.

## What it cannot do

**See anything.** There is no camera. The Limelight is a stand-in that reports "not
connected" and never returns a result, so vision OpModes always take their no-target
path. AprilTag detection, colour blob detection and Limelight pipelines cannot be tested
here. The mecanum configurations carry a `"limelight"` device so that code expecting one
still initialises.

**Show a Panels dashboard.** Panels (`com.bylazar`) is a stand-in: `@Configurable`
fields keep their source values, so you cannot tune them live; field drawing is
discarded; and Panels telemetry is forwarded to the simulator's own telemetry window.
FTC Dashboard is likewise a stand-in.

**Substitute for tuning on a real robot.** The simulated robots are generic chassis with
a physics model that only approximates a real one, and PedroPathing constants describe a
specific physical machine — its mass, PIDF gains and track width. Path *geometry* and
autonomous logic are worth checking here; tracking accuracy is not. Numbers produced by
the tuners here describe the simulated robot and should not be copied to a real one.

**Model your mechanisms.** Your intake, launcher, lift or claw does not exist. Motors
configured for them fall back to mock devices that accept commands and do nothing.

**Track the FTC SDK exactly.** `Controller/src` holds a hand-written approximation of the
SDK, not the real thing. It covers what team code has needed so far and is extended as
gaps turn up; a class or method that has never been needed here may simply be absent.
Adding it is usually a small edit — see the existing classes under `com/qualcomm/` for
the pattern, and copy the real SDK's own implementation where you can.

## The virtual gamepad

The panel along the bottom stands in for a driver station gamepad: sticks, D-pad, ABXY,
bumpers, triggers, and Back/Start. Clicking its controls does nothing until you press
INIT, since an OpMode only sees gamepad input once it is running.

Drag a stick with the left mouse button. **Right-click a stick** to press its stick button
(`left_stick_button` / `right_stick_button`) — the handle is ringed in black while it is
held, and you can right-hold and left-drag the same stick at once.

Controls spring back when you let go of them. Hold **SHIFT** (or ALT) and they stay put
instead: a stick keeps its position, a trigger its value, and a button latches down until
you click it again. `HOLD_CONTROLS_BY_DEFAULT` in `Config.java` swaps which of the two is
the default, and SHIFT then gives you the other.

A **second gamepad** for OpModes that read `gamepad2` is hidden by default: its selector
starts on `Hide`, and picking anything else puts the panel on screen. Most OpModes only
use `gamepad1`, and a second panel always on screen costs space. While hidden, `gamepad2`
reports nothing pressed.

### Physical USB gamepads

A physical gamepad drives a panel rather than replacing it. The **Gamepad 1** and
**Gamepad 2** dropdowns on the right choose what drives each one: `Virtual gamepad`
(the panel itself, under the mouse), `Physical gamepad A` or `Physical gamepad B` — plus
`Hide` on gamepad 2, which takes its panel off screen entirely. A physical gamepad is
only offered while it is plugged in, and a selection whose gamepad is unplugged falls
back to `Virtual gamepad`. One physical gamepad cannot drive both, so claiming it hands
the other back to its own panel.

You can also claim a gamepad from the hardware itself, without reaching for the mouse:
**Start+A** hands that controller to `Gamepad 1` and **Start+B** hands it to `Gamepad 2`,
the same gesture the Driver Station uses. The dropdown follows along.

While a physical gamepad drives a panel, the panel's controls follow the hardware — the
stick handles move, the triggers slide, the buttons light up — and the panel is what the
OpMode reads, so what you see is genuinely the input. That panel ignores the mouse for as
long as the gamepad is driving it; giving `Gamepad 2` anything but `Hide` puts its panel
on screen. `gamepad.rumble(...)` vibrates the physical gamepad as well as tinting the
trigger sliders.

If SDL cannot start — no gamepad support on the machine — the simulator says so on the
console, offers `Virtual gamepad` only, and runs mouse-only.

## Excluding an OpMode from the simulator

If you write an OpMode the simulator cannot compile — usually because it uses a library
or an FTC SDK class the approximation does not provide — add its path to the
**`simIncompatibleOpModes`** list in `build.gradle`:

```groovy
def simIncompatibleOpModes = [
        'org/firstinspires/ftc/teamcode/teleop/MyNewTeleop.java',
]
```

Paths are relative to `TeamCode/src/main/java`, and `**` wildcards work for whole
folders. Excluding a file only keeps it out of the **simulator** build; it still builds
and deploys to the robot exactly as before. Say why in a comment, so the entry can be
dropped once the gap is filled.

## How the build works, and how it differs from the robot's

Both targets are Gradle subprojects of the same project, listed in `settings.gradle`,
but they are built in completely different ways.

| | `:FtcRobotController` + `:TeamCode` | `:Controller` |
|---|---|---|
| Plugin | `com.android.application` | `application` + `org.openjfx.javafxplugin` |
| Output | an Android APK, installed on the Control Hub | a desktop JavaFX app, run on your PC |
| FTC SDK | real AARs, `org.firstinspires.ftc:*` | the source approximation in `src/` |
| Java level | 8 (`build.common.gradle`) | whatever JVM runs Gradle (JBR 21 in Android Studio) |
| Shared config | `build.common.gradle`, `build.dependencies.gradle` | none - this module stands alone |

The two never share a classpath. `:Controller` deliberately does **not** depend on
`:FtcRobotController` and never sees the real FTC artifacts, so its `com.qualcomm.*`
symbols resolve only to the approximation in `src/`. That is what keeps two definitions
of the same class from colliding: each target compiles the same OpMode source against a
different definition of the SDK. It is also why the FTC artifacts could not simply be
reused here - they are Android AARs, which a plain-Java module cannot consume at all.

### The shared OpModes are copied, not referenced

`:TeamCode` compiles `TeamCode/src/main/java` directly. `:Controller` cannot also point a
source root at that folder: Android Studio requires every source root to belong to
exactly one module, and sharing it makes the IDE report *"Duplicate content roots
detected"* and pull the folder out of `:TeamCode`.

Instead a `Sync` task copies the OpModes into `build/shared-opmodes`, a generated folder
the IDE ignores, and that is the simulator's source root. Keep editing OpModes in
`TeamCode/src/main/java` as normal - the copy refreshes on every `:Controller` build. If
you go looking, compile errors from the simulator name files under
`build/shared-opmodes/...`; the file to fix is the original in `TeamCode`.

### Building it

`./gradlew :Controller:run` runs, in order:

```
extractPedroPathingFtc        ->  syncSharedOpModes  ->  compileJava  ->  processResources  ->  run
extractPedroPathingTelemetry
extractPedroPathingIvy
```

`:TeamCode` just declares `com.pedropathing:ftc` and Android handles the AARs.
`:Controller` cannot, so those three tasks pull `classes.jar` out of the `ftc`, `telemetry`
and `ivy` AARs and put them on the classpath; `com.pedropathing:core` is a plain jar and is
used as-is.

`processResources` matters more than it looks: the FXML layouts, the field `.bmp` images
and the robot geometry live next to the source under `src/`, and are packaged from
there.

Building or deploying the robot app is untouched by any of this - `:TeamCode` has no
dependency on `:Controller`, so `assembleDebug` behaves exactly as it did before the
simulator existed.

## Robot configurations

Chosen from the **Configuration** dropdown before pressing INIT. Position the robot by
left-clicking the field (position) and right-clicking (heading).

Mecanum Bot, MecDynamic Bot, Arm Bot, Freight Bot, QQ Bot and Ulti Bot are mecanum
drives. They carry `frontLeft` / `frontRight` / `backLeft` / `backRight`, an `imu`, a
Pinpoint odometry computer, `sensor_otos`, `octoquad`, a `color_sensor` and four
distance sensors.

Also available: Claw Bot, Two Wheel Bot, XDrive Bot, Kiwi Bot, Swerve Bot, Differential
Swerve Bot, Square Omni Bot, Turret Bot, and a Programming Board that does not drive but
carries a motor, servo, potentiometer, touch sensor and colour-distance sensor.

## Settings

`src/virtual_robot/config/Config.java` holds the run-time options — field size and image,
whether the gamepad controls hold their position when released
(`HOLD_CONTROLS_BY_DEFAULT`), and the game (currently Decode; `new NoGame()` removes the
field obstacles).

The sliders beside the field inject random and systematic motor error and motor inertia,
which is useful for checking that an autonomous routine is not relying on perfectly
accurate motors.

## Layout

| Path | Contents |
|---|---|
| `src/virtual_robot/` | the simulator: JavaFX UI, physics, robot configurations, game elements |
| `src/com/qualcomm/`, `src/org/firstinspires/` | the FTC SDK approximation |
| `src/com/qualcomm/hardware/limelightvision/`, `src/com/bylazar/` | Limelight and Panels stand-ins |
| `src/android/`, `src/androidx/`, `src/org/json/` | just enough Android to compile SDK-style code off-device |
| `libs/` | dyn4j, Reflections, Jamepad, Guava, Javassist |
| `LEGAL/` | third-party licences — see `LEGAL/README.md` |

OpModes are discovered by scanning the classpath at run time for `@TeleOp` and
`@Autonomous`, the same annotations the Robot Controller uses. There is no registration
file.
