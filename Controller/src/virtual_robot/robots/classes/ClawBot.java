package virtual_robot.robots.classes;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorExImpl;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import virtual_robot.controller.BotConfig;

/**
 * For internal use only. Represents a robot with two drive wheels (differential drive),
 * a motor-driven arm, and a servo-driven claw on the end of the arm.
 *
 * ClawBot extends TwoWheelPhysicsBase, which manages the two-wheel drive train and its
 * physics. In addition to the drive motors, ClawBot therefore also inherits a color
 * sensor, four distance sensors, and a BNO055 IMU from that base class.
 *
 * Hardware map names:
 *   leftDrive, rightDrive - drive motors (provided by TwoWheelPhysicsBase)
 *   arm               - raises / lowers the arm
 *   claw              - 0.0 = claw fully open, 1.0 = claw fully closed
 *
 * The arm and claw are visual only: they are JavaFX transforms driven by the arm
 * encoder position and the servo position, and do not physically interact with game
 * elements. See ArmBot for an example that adds dyn4j Bodys so that accessories can
 * grab game elements.
 *
 * The @BotConfig annotation is required. "name" is what the user sees in the
 * Configuration combo box; "filename" is the fxml file (in virtual_robot/robots/fxml)
 * that holds the graphical representation, with ClawBot as its fx:controller.
 */
@BotConfig(name = "Claw Bot", filename = "claw_bot")
public class ClawBot extends BasicTwoWheelPhysicsBase {

    // Arm encoder travel, in ticks, that corresponds to a full arm sweep (Neverest40 = 1120 ticks/rev).
    private static final double ARM_TICKS_FULL_TRAVEL = 1120.0;
    // Angle, in degrees, that the arm sweeps through over ARM_TICKS_FULL_TRAVEL.
    private static final double ARM_SWEEP_DEGREES = 135.0;
    // Maximum travel of each claw finger, in fxml pixels, from fully open to fully closed.
    private static final double CLAW_FINGER_TRAVEL = 6.0;

    private DcMotorExImpl armMotor = null;
    private ServoImpl clawServo = null;

    // Instantiated automatically during loading of claw_bot.fxml via their fx:id attributes.
    @FXML private Group armGroup;
    @FXML private Rectangle leftClaw;
    @FXML private Rectangle rightClaw;

    // Transforms created in initialize() and manipulated in updateDisplay().
    private Rotate armRotate;
    private Translate leftClawTranslate;
    private Translate rightClawTranslate;

    // Updated in updateStateAndSensors(), consumed in updateDisplay().
    private double armAngleDegrees = 0;
    private double clawClosedFraction = 0;

    public ClawBot() {
        super();
    }

    public void initialize() {
        // Handles createHardwareMap(), the chassis body, and all two-wheel drive-base setup.
        super.initialize();

        // Temporarily activate the hardware map so we can "get" the accessory hardware.
        hardwareMap.setActive(true);

        armMotor = (DcMotorExImpl) hardwareMap.get(DcMotorEx.class, "arm");
        armMotor.setActualPositionLimits(0, ARM_TICKS_FULL_TRAVEL);
        armMotor.setPositionLimitsEnabled(true);

        clawServo = (ServoImpl) hardwareMap.servo.get("claw");

        hardwareMap.setActive(false);

        // Rotates the whole arm/claw group about the center of the robot (37.5, 37.5).
        armRotate = new Rotate(0, 37.5, 37.5);
        armGroup.getTransforms().add(armRotate);

        // Translate each claw finger along X to open / close the claw.
        leftClawTranslate = new Translate(0, 0);
        leftClaw.getTransforms().add(leftClawTranslate);
        rightClawTranslate = new Translate(0, 0);
        rightClaw.getTransforms().add(rightClawTranslate);
    }

    protected void createHardwareMap() {
        // Adds leftDrive, rightDrive, the distance sensors, the IMU, and the color sensor.
        super.createHardwareMap();

        // Drive motors occupy ports 0 and 1 of motorController0, so put the arm motor on motorController1.
        hardwareMap.put("arm", new DcMotorExImpl(MotorType.Neverest40, motorController1, 0));
        hardwareMap.put("claw", new ServoImpl());
    }

    public synchronized void updateStateAndSensors(double millis) {
        // Handles the drive train and the standard sensors.
        super.updateStateAndSensors(millis);

        armMotor.update(millis);
        armAngleDegrees = armMotor.getActualPosition() / ARM_TICKS_FULL_TRAVEL * ARM_SWEEP_DEGREES;

        // Servo position maps directly to how far the claw is closed (0 = open, 1 = closed).
        clawClosedFraction = clawServo.getInternalPosition();
    }

    public synchronized void updateDisplay() {
        // Positions and orients the robot on the field.
        super.updateDisplay();

        armRotate.setAngle(armAngleDegrees);
        leftClawTranslate.setX(CLAW_FINGER_TRAVEL * clawClosedFraction);
        rightClawTranslate.setX(-CLAW_FINGER_TRAVEL * clawClosedFraction);
    }

    public void powerDownAndReset() {
        super.powerDownAndReset();
        armMotor.stopAndReset();
    }

}
