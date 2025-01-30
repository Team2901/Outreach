package org.firstinspires.ftc.teamcode.Outreach.Test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.ClawbotHardware;
import org.firstinspires.ftc.teamcode.Utilities.ImprovedGamepad;

@SuppressWarnings("unused")
@TeleOp(name="Clawbot PID Tuner", group="Test")
public class ClawbotPidTuner extends OpMode {
    public ImprovedGamepad gamepad;
    public double voltage;
    public ImprovedGamepad masterGamepad;
    public ImprovedGamepad gamepadInControl;
    public ElapsedTime gamepadTimer = new ElapsedTime();
    public double rightPower = 0;
    public double leftPower = 0;

    double integralSum = 0;

    double lastError = 0;

    double increaseAmount = .001;

    double goalPositon = 2.5;
    boolean PIDRunning = true;

    ElapsedTime PIDtimer = new ElapsedTime();

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
        overrideControllerCheck();

        updatePIDValues();
        voltageRegulation();
        armPositionUpdate();
        telemetry();
    }

    public void telemetry() {
        telemetry.addData("Kp(x/y)", ClawbotHardware.Kp);
        telemetry.addData("Ki(a/b)", ClawbotHardware.Ki);
        telemetry.addData("Kd(left/right trigger)", ClawbotHardware.Kd);
        telemetry.addData("Toggle Loop(left dpad)", PIDRunning);
        telemetry.addData("Increments(left/right bumper)", increaseAmount);
        telemetry.addData("Goal Voltage", goalPositon);
        telemetry.addData("Actual Voltage", voltage);

        telemetry.update();
    }

    public void updatePIDValues() {
        if (gamepadInControl.left_bumper.isInitialPress()) {
            increaseAmount *= 10;
        } else if (gamepadInControl.right_bumper.isInitialPress()) {
            increaseAmount /= 10;
        }

        if (gamepadInControl.x.isInitialPress()) {
            ClawbotHardware.Kp += increaseAmount;
        } else if (gamepadInControl.y.isInitialPress()) {
            ClawbotHardware.Kp -= increaseAmount;
        } else if (gamepadInControl.a.isInitialPress()) {
            ClawbotHardware.Ki += increaseAmount;
        } else if (gamepadInControl.b.isInitialPress()) {
            ClawbotHardware.Ki -= increaseAmount;
        } else if (gamepadInControl.right_trigger.isInitialPress()) {
            ClawbotHardware.Kd += increaseAmount;
        } else if (gamepadInControl.left_trigger.isInitialPress()) {
            ClawbotHardware.Kd -= increaseAmount;
        } else if (gamepadInControl.dpad_left.isInitialPress()) {
            PIDRunning = !PIDRunning;
        }
    }


    public void voltageRegulation() {
        double result = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double voltage = sensor.getVoltage();
            if (voltage > 0) {
                result = Math.min(result, voltage);
            }
        }
        double scaleFactor = 12/result;
        voltage = scaleFactor * robot.potentiometer.getVoltage();
    }


    public void armPositionUpdate() {
        if (!PIDRunning) {
            robot.arm.setPower(0);
            return;
        }
        if (gamepadInControl.dpad_up.isPressed()) {
            goalPositon -= increaseAmount;
        } else if (gamepadInControl.dpad_down.isPressed()) {
            goalPositon += increaseAmount;
        }
// Elapsed timer class from SDK, please use it, it's epic
        ElapsedTime timer = new ElapsedTime();

            // calculate the error
            double error = voltage - goalPositon;

            // rate of change of the error
            double derivative = (error - lastError) / timer.seconds();

            // sum of all error over time
            integralSum = integralSum + (error * timer.seconds());

            double out = (ClawbotHardware.Kp * error) + (ClawbotHardware.Ki * integralSum) + (ClawbotHardware.Kd * derivative);

            robot.arm.setPower(out);

            lastError = error;

            // reset the timer for next time
            PIDtimer.reset();

    }


    public void overrideControllerCheck() {
        if (masterGamepad.areButtonsActive()) {
            gamepadOverride = true;
        } else {
            gamepadOverride = false;
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
    }
}
