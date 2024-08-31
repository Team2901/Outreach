package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.ClawbotHardware;
import org.firstinspires.ftc.teamcode.Outreach.Hardware.OutreachBotHardware;
import org.firstinspires.ftc.teamcode.Utilities.ImprovedGamepad;

@TeleOp(name = "Outreach Bot TeleOp", group = "Outreach")
public class OutreachBotTeleOp extends OpMode {
    OutreachBotHardware robot = new OutreachBotHardware();
    ElapsedTime timer = new ElapsedTime();
    ImprovedGamepad gamepad;
    ImprovedGamepad masterGamepad;
    ImprovedGamepad gamepadInControl;

    public boolean gamepadOverride = false;

    public double rightPower = 0;
    public double leftPower = 0;

    @Override
    public void init() {
        robot.init(hardwareMap);

        gamepad = new ImprovedGamepad(this.gamepad1, this.timer, "GP1");
        masterGamepad = new ImprovedGamepad(this.gamepad2, this.timer, "GP2");
    }

    @Override
    public void loop() {
        overrideControllerCheck();
        rightPower = (gamepadInControl.left_stick.y.getValue() / ClawbotHardware.STRAIGHT_POWER_WEIGHT) + (gamepadInControl.right_stick.y.getValue() / ClawbotHardware.STRAIGHT_POWER_WEIGHT) - (gamepadInControl.right_stick.x.getValue() / ClawbotHardware.TURN_WEIGHT) - (gamepadInControl.left_stick.x.getValue() / ClawbotHardware.TURN_WEIGHT);
        leftPower = (gamepadInControl.left_stick.y.getValue() / ClawbotHardware.STRAIGHT_POWER_WEIGHT) + (gamepadInControl.right_stick.y.getValue() / ClawbotHardware.STRAIGHT_POWER_WEIGHT) + (gamepadInControl.left_stick.x.getValue() / ClawbotHardware.TURN_WEIGHT) + (gamepadInControl.right_stick.x.getValue() / ClawbotHardware.TURN_WEIGHT);
        polarAxisValueTurning();
    }

    public void polarAxisValueTurning() {
        /*
         * With the way the turning use to work without this method, if you held one stick all the way
         * forward on the y-axis and held the other stick all the way back on the y-axis, the robot
         * would stay stationary. If you are confused by this in the future, remove the call of this
         * method and loop, see how it controls in the situation above, then add it back.
         */

        if (gamepadInControl.left_stick.y.getValue() > .9 && gamepadInControl.right_stick.y.getValue() < -.9) {
            if ((gamepadInControl.left_stick.x.getValue() < .25 && gamepadInControl.left_stick.x.getValue() > -.25) && (gamepadInControl.right_stick.x.getValue() < .25 && gamepadInControl.right_stick.x.getValue() > -.25) ) {
                leftPower = 1;
                rightPower = -1;
            }
        }

        if (gamepadInControl.right_stick.y.getValue() > .9 && gamepadInControl.left_stick.y.getValue() < -.9 ) {
            if ((gamepadInControl.left_stick.x.getValue() < .25 && gamepadInControl.left_stick.x.getValue() > -.25) && (gamepadInControl.right_stick.x.getValue() < .25 && gamepadInControl.right_stick.x.getValue() > -.25)) {
                leftPower = -1;
                rightPower = 1;
            }
        }
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
