package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.ClawbotHardware;

@TeleOp(name="Clawbot Voltage tester", group="Outreach")
public class ClawbotVoltageTester extends OpMode {

    ClawbotHardware robot = new ClawbotHardware();
    @Override
    public void init() {
        robot.init(hardwareMap);
    }

    @Override
    public void loop() {
        telemetryStuff();
    }

    public void telemetryStuff() {
        telemetry.addData("Voltage", robot.potentiometer.getVoltage());
        telemetry.update();
    }
}
