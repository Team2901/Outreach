package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.hardware.ClawbotHardware;

@SuppressWarnings("unused")
@TeleOp(name="Clawbot Voltage tester", group="Test")
public class ClawbotVoltageTester extends OpMode {

    ClawbotHardware robot = new ClawbotHardware();
    @Override
    public void init() {
        robot.init(hardwareMap);
    }

    @Override
    public void loop() {
        telemetry();
    }

    public void telemetry() {
        telemetry.addData("Voltage", robot.potentiometer.getVoltage());
        telemetry.update();
    }
}
