package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.hardware.ClawbotHardware;
import org.firstinspires.ftc.teamcode.utilities.ImprovedGamepad;

@SuppressWarnings("unused")
@TeleOp(name = "Clawbot PID Tuner", group = "test")
public class ClawbotPidTuner extends OpMode {
    public ImprovedGamepad gamepad;
    public double voltage;
    public ElapsedTime gamepadTimer = new ElapsedTime();
    public double rightPower = 0;
    public double leftPower = 0;

    double integralSum = 0;

    double lastError = 0;

    double increaseAmount = .001;

    double goalPositon = 2.5;
    boolean PIDRunning = true;

    ElapsedTime PIDTimer = new ElapsedTime();

    public boolean gamepadOverride = false;

    ClawbotHardware robot = new ClawbotHardware();

    @Override
    public void init() {
        gamepad = new ImprovedGamepad(gamepad1, gamepadTimer, "gamepad");
        robot.init(hardwareMap);
        telemetry();
    }

    @Override
    public void loop() {
        gamepad.update();

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
        if (gamepad.left_bumper.isInitialPress()) {
            increaseAmount *= 10;
        } else if (gamepad.right_bumper.isInitialPress()) {
            increaseAmount /= 10;
        }

        if (gamepad.x.isInitialPress()) {
            ClawbotHardware.Kp += increaseAmount;
        } else if (gamepad.y.isInitialPress()) {
            ClawbotHardware.Kp -= increaseAmount;
        } else if (gamepad.a.isInitialPress()) {
            ClawbotHardware.Ki += increaseAmount;
        } else if (gamepad.b.isInitialPress()) {
            ClawbotHardware.Ki -= increaseAmount;
        } else if (gamepad.right_trigger.isInitialPress()) {
            ClawbotHardware.Kd += increaseAmount;
        } else if (gamepad.left_trigger.isInitialPress()) {
            ClawbotHardware.Kd -= increaseAmount;
        } else if (gamepad.dpad_left.isInitialPress()) {
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
        double scaleFactor = 12 / result;
        voltage = scaleFactor * robot.potentiometer.getVoltage();
    }


    public void armPositionUpdate() {
        if (!PIDRunning) {
            robot.arm.setPower(0);
            return;
        }
        if (gamepad.dpad_up.isPressed()) {
            goalPositon -= increaseAmount;
        } else if (gamepad.dpad_down.isPressed()) {
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
        PIDTimer.reset();

    }
}
