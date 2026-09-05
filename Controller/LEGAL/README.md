# Third-party licenses — Virtual Robot simulator (`:Controller`)

`LICENSE.txt` / `NOTICE.txt` cover the simulator itself: Virtual Robot,
Copyright 2019 Team Beta, Apache License 2.0.

Everything else here covers third-party code the simulator ships or depends on.

## Libraries bundled as jars (`Controller/libs/`)

| Library | Used for |
|---|---|
| dyn4j | 2D rigid-body physics engine |
| Guava | collections (Reflections dependency) |
| Jamepad | physical USB gamepad support |
| Javassist | bytecode access (Reflections dependency) |
| Reflections | classpath scanning that discovers `@TeleOp` / `@Autonomous` OpModes |

## Libraries resolved from Maven (`Controller/build.gradle`)

| Library | Used for |
|---|---|
| PedroPathing (`core`, `ftc`, `telemetry`) | path follower; the same artifacts the robot uses |

## Third-party source included under `Controller/src/`

| Project | Files |
|---|---|
| OpenFTC | `org/openftc/apriltag/` — AprilTag classes, MIT, headers retained |

## API stand-ins written for the simulator

`OpenCV` and `AcmeRobotics` are listed because `Controller/src` contains small
classes under `org.opencv.*` and `com.acmerobotics.dashboard.*` that mirror those
projects' public API so team code compiles. They contain no upstream source and
neither library is shipped; the notices are kept as attribution for the APIs.

The FTC SDK approximation (`com.qualcomm.*`, `org.firstinspires.ftc.*`), the
Limelight and Panels (`com.bylazar.*`) stand-ins, and the small `android.*` /
`androidx.*` shims are likewise original code written for the simulator, mirroring
the shape of the real APIs.
