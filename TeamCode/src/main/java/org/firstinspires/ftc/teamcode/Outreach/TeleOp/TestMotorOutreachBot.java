package org.firstinspires.ftc.teamcode.Outreach.TeleOp;


import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@SuppressWarnings("unused")
@TeleOp(name = "TestMotorOutreachBot", group = "Outreach")
public class TestMotorOutreachBot extends OpMode {
    public DcMotor leftDrive = null;
    public DcMotor rightDrive = null;
    public void init(){
        leftDrive = hardwareMap.dcMotor.get("leftDrive");
        rightDrive = hardwareMap.dcMotor.get("rightDrive");
    }

    @Override
    public void loop() {
        rightDrive.setPower(1);
        leftDrive.setPower(1);
    }
}
