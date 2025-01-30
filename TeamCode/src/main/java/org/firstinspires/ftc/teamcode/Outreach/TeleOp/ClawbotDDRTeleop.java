package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.ClawbotHardware;
import org.firstinspires.ftc.teamcode.Utilities.DDRGamepad;
import org.firstinspires.ftc.teamcode.Utilities.ImprovedGamepad;

@SuppressWarnings("unused")
@TeleOp(name="Clawbot DDR", group="Clawbot")
public class ClawbotDDRTeleop extends OpMode {
    public ImprovedGamepad gamepad;
    public double voltage;
    public boolean reversed = false;
    public boolean reversedTurns = false;

    public int speedBoostFactor = 2;
    public ImprovedGamepad masterGamepad;
    static DDRGamepad participantGamepad;
    public ElapsedTime timer = new ElapsedTime();
    public double rightPower = 0;
    public double leftPower = 0;


    public boolean gamepadOverride = false;

    ClawbotHardware robot = new ClawbotHardware();
    @Override
    public void init() {
        robot.init(hardwareMap);

        gamepad = new ImprovedGamepad(this.gamepad1, this.timer, "GP1");
        masterGamepad = new ImprovedGamepad(this.gamepad2, this.timer, "GP2");
        robot.currentArmState = ClawbotHardware.ArmState.HIGH;
    }

    @Override
    public void loop() {
        armPositionUpdate();
        double rightSpeed = 0;
        double leftSpeed = 0;

        if (participantGamepad.upArrow.getValue()) {
            if (!reversed) {
                telemetry.addData("upArrow", participantGamepad.upArrow);
                leftSpeed = .5 * speedBoostFactor;
                rightSpeed = .5 * speedBoostFactor;
            } else {
                leftSpeed = -.5 * speedBoostFactor;
                rightSpeed = -.5 * speedBoostFactor;
            }

        } else if (participantGamepad.downArrow.getValue()) {
            if (!reversed) {
                leftSpeed = -.5 * speedBoostFactor;
                rightSpeed = -.5 * speedBoostFactor;
            } else {
                leftSpeed = .5 * speedBoostFactor;
                rightSpeed = .5 * speedBoostFactor;
            }

        }

        if (participantGamepad.leftArrow.getValue()) {
            if (!reversedTurns) {
                if (rightSpeed == 0 && leftSpeed == 0) {
                    leftSpeed = -.4;
                    rightSpeed = .4;
                } else if (leftSpeed > 0){
                    rightSpeed = .65;
                } else {
                    rightSpeed = -.65;
                }
            } else {
                if (rightSpeed == 0 && leftSpeed == 0) {
                    leftSpeed = .4;
                    rightSpeed = -.4;
                } else if (leftSpeed > 0){
                    rightSpeed = -.65;
                } else {
                    rightSpeed = .65;
                }
            }

        } else if (participantGamepad.rightArrow.getValue()) {
            if (!reversedTurns) {
                if (rightSpeed == 0 && leftSpeed == 0) {
                    rightSpeed = -.4;
                    leftSpeed = .4;
                } else if (rightSpeed > 0) {
                    leftSpeed = .65;
                } else {
                    leftSpeed = -.65;
                }
            } else {
                if (rightSpeed == 0 && leftSpeed == 0) {
                    rightSpeed = .4;
                    leftSpeed = -.4;
                } else if (rightSpeed > 0) {
                    leftSpeed = -.65;
                } else {
                    leftSpeed = .65;
                }
            }

        }
    }

    public void armPositionUpdate() {

//        if (robot.currentArmState == ClawbotHardware.ArmState.MEDIUM && voltage > ClawbotHardware.MAX) {
//            robot.arm.setPower(0.5);
//        } else if (robot.currentArmState == ClawbotHardware.ArmState.LOW && voltage < ClawbotHardware.MAXIMUM_MEDIUM_ARM_VOLTAGE) {
//            robot.arm.setPower(-0.2);
//        } else {
//            robot.arm.setPower(0);
//        }
//
//        if ((rightPower != 0 || leftPower != 0) && voltage > ClawbotHardware.MAXIMUM_LOW_ARM_VOLTAGE) {
//            robot.arm.setPower(0.3);
//        }
    }
}
