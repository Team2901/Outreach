package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Utilities.DDRGamepad;

@TeleOp(name = "DDR Pad Diagnostic", group = "Outreach")
public class DDRPadDiagnostic extends OpMode {
    static DDRGamepad participantGamepad;

    @Override
    public void init() {
        participantGamepad = new DDRGamepad(this.gamepad1, new ElapsedTime(), "GP1");
        telemetry.addLine("Do not Init program");
        telemetry.addLine("Press Up Arrow");
        telemetry.update();
        if (participantGamepad.upArrow.isPressed()) {
            telemetry.addLine("Up arrow works successfully");
            telemetry.addLine("Press Left Arrow");
            telemetry.update();
        }
        if (participantGamepad.leftArrow.isPressed()) {
            telemetry.addLine("Left arrow works successfully");
            telemetry.addLine("Press Right Arrow");
            telemetry.update();
        }
        if (participantGamepad.rightArrow.isPressed()) {
            telemetry.addLine("Right arrow works successfully");
            telemetry.addLine("Press Down Arrow");
            telemetry.update();
        }
        if (participantGamepad.downArrow.isPressed()) {
            telemetry.addLine("Up arrow works successfully");
            telemetry.addLine("Press top left Arrow");
            telemetry.update();
        }
        if (participantGamepad.topLeftArrow.isPressed()) {
            telemetry.addLine("top left Arrow successfully");
            telemetry.addLine("Press top right Arrow");
            telemetry.update();
        }
        if (participantGamepad.topRightArrow.isPressed()) {
            telemetry.addLine("top right Arrow works successfully");
            telemetry.addLine("Nothing is wrong with the hardware in the DDR Pad");
            telemetry.update();
        }
    }

    @Override
    public void loop() {
        telemetry.addLine("Do not Init program");
        telemetry.update();
    }

}
