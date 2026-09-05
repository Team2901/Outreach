package org.firstinspires.ftc.teamcode.test;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TeleOp(name = "Motor Test", group = "test")
public class MotorTest extends OpMode {
    List<Map.Entry<String, DcMotor>> dcMotorList = new ArrayList<>();
    int activeIndex = 0;

    double targetPower;

    int lastEncoderTicks = 0;

    DcMotor.RunMode runMode = DcMotor.RunMode.RUN_USING_ENCODER;

    public void help() {
        telemetry.addLine("Use Dpad up/down to choose motor");
        telemetry.addLine("a toggle run mode");
        telemetry.addLine("Left stick Y");
        telemetry.addLine("           up - forward");
        telemetry.addLine("           dn - backward");
        telemetry.addLine("");
    }

    public void telemetry(){
        DcMotor activeMotor = dcMotorList.get(activeIndex).getValue();

        telemetry.addData("current motor", dcMotorList.get(activeIndex).getKey());
        telemetry.addData("motor power", activeMotor.getPower());
        telemetry.addData("encoder value (ticks)", activeMotor.getCurrentPosition());
        telemetry.addData("velocity", ((DcMotorEx) activeMotor).getVelocity());
        telemetry.addData("y stick", targetPower);
        telemetry.addData("run mode", activeMotor.getMode());
    }

    @Override
    public void init() {
        Set<Map.Entry<String, DcMotor>> dcMotorSet = this.hardwareMap.dcMotor.entrySet();
        dcMotorList.addAll(dcMotorSet);
        for (DcMotor dcMotor : this.hardwareMap.dcMotor) {
            dcMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            dcMotor.setMode(runMode);
        }

        help();
    }

    @Override
    public void loop() {

        int maxCount = dcMotorList.size();
        int newIndex;
        if (gamepad1.dpadUpWasPressed()){
            newIndex = (activeIndex + 1) % maxCount;
        } else if (gamepad1.dpadDownWasPressed()){
            // add maxCount so that newIndex will always be positive
            newIndex = (activeIndex + maxCount - 1) % maxCount;
        } else {
            newIndex = activeIndex;
        }

        DcMotor activeMotor;
        if (newIndex != activeIndex) {
            // stop the old motor
            dcMotorList.get(activeIndex).getValue().setPower(0);

            // update to index the new motor
            activeIndex = newIndex;
            activeMotor = dcMotorList.get(activeIndex).getValue();

            // reset the new motor's encoder count
            activeMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            activeMotor.setMode(runMode);

        } else {
            activeMotor = dcMotorList.get(activeIndex).getValue();

            // only check if the motor has not changed
            int tickDelta = activeMotor.getCurrentPosition() - lastEncoderTicks;
            if (Math.signum(tickDelta) != Math.signum(targetPower)) {
                telemetry.addLine("WARNING: ticks are going backwards from motor direction. Check your wiring");
            }
        }

        if (gamepad1.aWasPressed()) {
            // toggle between running with/without encoders
            runMode = DcMotor.RunMode.RUN_USING_ENCODER == runMode ?
                    DcMotor.RunMode.RUN_WITHOUT_ENCODER :
                    DcMotor.RunMode.RUN_USING_ENCODER;

            activeMotor.setMode(runMode);
        }

        targetPower = -gamepad1.left_stick_y;
        lastEncoderTicks = activeMotor.getCurrentPosition();
        activeMotor.setPower(targetPower);

        help();
        telemetry();
    }
}
