package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.ClawbotHardware;
import org.firstinspires.ftc.teamcode.Utilities.ImprovedGamepad;

@SuppressWarnings("unused")
@TeleOp(name="Clawbot Tank Drive", group="Clawbot")
public class ClawbotTeleOpTankDrive extends OpMode {
    public enum Controller{PARTICIPANT, MASTER}
    public ImprovedGamepad gamepad;
    public double voltage;
    public ImprovedGamepad masterGamepad;
    public ImprovedGamepad gamepadInControl;
    public ElapsedTime gamepadTimer = new ElapsedTime();
    public boolean override = false;

    public boolean gamepadOverride = false;

    ClawbotHardware robot = new ClawbotHardware();
    @Override
    public void init() {
        gamepad = new ImprovedGamepad(gamepad1, gamepadTimer, "gamepad");
        masterGamepad = new ImprovedGamepad(gamepad2, gamepadTimer, "masterGamepad");
        gamepadInControl = null;
        robot.init(hardwareMap);
        telemetry();
    }

    @Override
    public void loop() {
        gamepad.update();
        masterGamepad.update();

        if (masterGamepad.areButtonsActive()) {
            override = true;
        } else {
            override = false;
        }

        if(masterGamepad.x.isInitialPress() && gamepadOverride){
            gamepadOverride = false;
        }else if(masterGamepad.x.isInitialPress() && !gamepadOverride){
            gamepadOverride = true;
        }

        if(gamepadOverride || masterGamepad.areButtonsActive()){
            gamepadInControl = masterGamepad;
        }else{
            gamepadInControl = gamepad;
        }

        robot.leftDrive.setPower(-gamepadInControl.left_stick.y.getValue());
        robot.rightDrive.setPower(-gamepadInControl.right_stick.y.getValue());

        switch(robot.currentClawState){
            case OPEN:
                robot.claw.setPosition(ClawbotHardware.CLAW_OPEN_POSITION);
                if(gamepadInControl.b.isInitialPress()){
                    robot.currentClawState = ClawbotHardware.ClawState.CLOSED;
                }
                break;
            case CLOSED:
                robot.claw.setPosition(ClawbotHardware.CLAW_CLOSED_POSITION);
                if(gamepadInControl.b.isInitialPress()){
                    robot.currentClawState = ClawbotHardware.ClawState.OPEN;
                }
                break;
        }

        switch (robot.currentArmState) {
            case GROUND:
            case LOW:
                if (gamepadInControl.dpad_up.isInitialPress()) {
                    robot.currentArmState = ClawbotHardware.ArmState.MEDIUM;
                }
                break;
            case MEDIUM:
                if (gamepadInControl.dpad_up.isInitialPress()) {
                    robot.currentArmState = ClawbotHardware.ArmState.HIGH;
                } else if (gamepadInControl.dpad_down.isInitialPress()) {
                    robot.currentArmState = ClawbotHardware.ArmState.LOW;
                }
                break;
            case HIGH:
                if (gamepadInControl.dpad_down.isInitialPress()) {
                    robot.currentArmState = ClawbotHardware.ArmState.MEDIUM;
                }
        }

        double result = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double voltage = sensor.getVoltage();
            if (voltage > 0) {
                result = Math.min(result, voltage);
            }
        }
        double scaleFactor = 12/result;
        voltage = scaleFactor * robot.potentiometer.getVoltage();

        if ((gamepadInControl.right_stick.y.getValue() != 0 || gamepadInControl.left_stick.y.getValue() != 0) && (robot.currentArmState == ClawbotHardware.ArmState.GROUND || robot.currentArmState == ClawbotHardware.ArmState.LOW) ) {
            robot.currentArmState = ClawbotHardware.ArmState.LOW;
        }

        armPositionUpdate();

        telemetry();

    }

    public void armPositionUpdate() {
//        if ((gamepadInControl.right_stick.y.getValue() != 0 || gamepadInControl.left_stick.y.getValue() != 0 || gamepadInControl.left_stick.x.getValue() != 0 || gamepadInControl.right_stick.x.getValue() != 0) && (robot.currentArmState == ClawbotHardware.ArmState.GROUND || robot.currentArmState == ClawbotHardware.ArmState.LOW) ) {
//            robot.currentArmState = ClawbotHardware.ArmState.LOW;
//        }
//
//        if (robot.currentArmState == ClawbotHardware.ArmState.MEDIUM && voltage > ClawbotHardware.MINIMUM_LOW_ARM_VOLTAGE) {
//            robot.arm.setPower(0.5);
//        } else if (robot.currentArmState == ClawbotHardware.ArmState.LOW && voltage < ClawbotHardware.MAXIMUM_MEDIUM_ARM_VOLTAGE) {
//            robot.arm.setPower(-0.2);
//        } else {
//            robot.arm.setPower(0);
//        }
//
//        if ((gamepadInControl.right_stick.y.getValue() != 0 || gamepadInControl.left_stick.y.getValue() != 0) && voltage > ClawbotHardware.MAXIMUM_LOW_ARM_VOLTAGE) {
//            robot.arm.setPower(0.3);
//        }
    }

    public void telemetry() {
        telemetry.addData("Master Control Start", "Start + B");
        telemetry.addData("Player Control Start", "Start + A");
        telemetry.addData("Forward/Backward", "Left and Right game stick");
        telemetry.addData("Open/Close claw", "B");
        telemetry.addData("Move Arm Up", "D-pad up");
        telemetry.addData("Move Arm Down", "D-pad down");
        telemetry.addData("Master Gamepad Override", "X");
        telemetry.addData("Override", override);
        telemetry.update();
    }
}
