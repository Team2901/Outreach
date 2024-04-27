package org.firstinspires.ftc.teamcode.Outreach.TeleOp;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Outreach.Hardware.DDRDance;
import org.firstinspires.ftc.teamcode.Outreach.Hardware.DanceObserver;
import org.firstinspires.ftc.teamcode.Outreach.Hardware.OutreachBotHardware;
import org.firstinspires.ftc.teamcode.Shared.Gamepad.DDRGamepad;
import org.firstinspires.ftc.teamcode.Shared.Gamepad.ImprovedGamepad;

import java.util.ArrayList;

//Created by jerdenn25556
@TeleOp(name = "Outreach Bot DDR Dance V3", group = "Outreach")
public class OutreachBotDDRTeleop extends OpMode {
    OutreachBotHardware robot = new OutreachBotHardware();
    ElapsedTime timer = new ElapsedTime();
    static DDRGamepad participantGamepad;
    ImprovedGamepad masterGamepad;
    double speedBoostFactor = 1;
    boolean spinning = false;
    static ArrayList<DDRDance> dances = new ArrayList<DDRDance>();
    static DDRDance speedDance;
    static DDRDance konamiCode;
    static DDRDance spinDance;
    boolean overide = false;
    private double leftSpeed = 0;
    private double rightSpeed = 0;
    private boolean reversedTurns = false;
    private boolean reversed = false;
    @Override
    public void init() {
        robot.init(hardwareMap);
        telemetry.addData("Master override", "X");
        telemetry.addData("Reverse Forward", "A");
        telemetry.addData("Reverse Turns", "B");
        telemetry.update();
        participantGamepad = new DDRGamepad(this.gamepad1, this.timer, "GP1");
        masterGamepad = new ImprovedGamepad(this.gamepad2, this.timer, "GP2");
        initDances();
    }

    @Override
    public void loop() {
        participantGamepad.update();
        masterGamepad.update();
        if (masterGamepad.areButtonsActive()) {
            robot.rightDrive.setPower(masterGamepad.left_stick_y.getValue());
            robot.leftDrive.setPower(masterGamepad.right_stick_y.getValue());
        }

        if (masterGamepad.x.isInitialPress()) {
            overide = !overide;
        }

        telemetry();

        if (overide) {
            return;
        }

        //updateDances();

        leftSpeed = 0;
        rightSpeed = 0;

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

        if (masterGamepad.b.isInitialPress()) {
            reversedTurns = !reversedTurns;
        } else if (masterGamepad.a.isInitialPress()) {
            reversed = !reversed;
        }
        switch (robot.currentClawState) {
            case OPEN:
                if (participantGamepad.topRightArrow.isInitialPress() || participantGamepad.topLeftArrow.isInitialPress() || masterGamepad.b.isInitialPress()) {
                    robot.currentClawState = OutreachBotHardware.ClawState.CLOSED;
                }
                robot.claw.setPosition(robot.OPEN_POSITION);
                break;
            case CLOSED:
                if (participantGamepad.topRightArrow.isInitialPress() || participantGamepad.topLeftArrow.isInitialPress() || masterGamepad.b.isInitialPress()) {
                    robot.currentClawState = OutreachBotHardware.ClawState.OPEN;
                }
                robot.claw.setPosition(robot.CLOSED_POSITION);
                break;
        }
        telemetry.addData("left speed", leftSpeed);
        telemetry.addData("right speed", rightSpeed);
        robot.rightDrive.setPower(rightSpeed);
        robot.leftDrive.setPower(leftSpeed);


    }

    public void initDances() {
        ArrayList testDanceMoves = new ArrayList<DDRDance.DanceMoves>();
        testDanceMoves.add(DDRDance.DanceMoves.LEFT);
        testDanceMoves.add(DDRDance.DanceMoves.RIGHT);
        testDanceMoves.add(DDRDance.DanceMoves.UP);
        speedDance = new DDRDance(testDanceMoves, participantGamepad, new SpeedDanceObserver());
        dances.add(speedDance);
        ArrayList konamiCodeMoves = new ArrayList<DDRDance.DanceMoves>();
        testDanceMoves.add(DDRDance.DanceMoves.UP);
        testDanceMoves.add(DDRDance.DanceMoves.UP);
        testDanceMoves.add(DDRDance.DanceMoves.DOWN);
        testDanceMoves.add(DDRDance.DanceMoves.DOWN);
        testDanceMoves.add(DDRDance.DanceMoves.LEFT);
        testDanceMoves.add(DDRDance.DanceMoves.RIGHT);
        testDanceMoves.add(DDRDance.DanceMoves.LEFT);
        testDanceMoves.add(DDRDance.DanceMoves.RIGHT);
        konamiCode = new DDRDance(konamiCodeMoves, participantGamepad, new SuperSpeedDanceObserver());
        dances.add(konamiCode);
        ArrayList spinDanceMoves = new ArrayList<DDRDance.DanceMoves>();
        testDanceMoves.add(DDRDance.DanceMoves.X);
        testDanceMoves.add(DDRDance.DanceMoves.UP);
        testDanceMoves.add(DDRDance.DanceMoves.O);
        testDanceMoves.add(DDRDance.DanceMoves.RIGHT);
        testDanceMoves.add(DDRDance.DanceMoves.DOWN);
        testDanceMoves.add(DDRDance.DanceMoves.LEFT);
        spinDance = new DDRDance(spinDanceMoves, participantGamepad, new SpinDanceObserver());
        dances.add(spinDance);
    }

    public void updateDances() {
        for (int i = 0; i < dances.size(); i++) {
            dances.get(i).update();
        }
    }

    private class SpeedDanceObserver implements DanceObserver
    {
        @Override
        public void onCompleted() {
            speedBoostFactor = 1.5;
        }

        @Override
        public void onSuccess() {
            //none
        }
    }

    private class SuperSpeedDanceObserver implements DanceObserver
    {
        @Override
        public void onCompleted() {
            speedBoostFactor = 2;
        }

        @Override
        public void onSuccess() {
            //none
        }
    }

    private class SpinDanceObserver implements DanceObserver
    {
        @Override
        public void onCompleted() {
            spinning = true;
        }

        @Override
        public void onSuccess() {
            //none
        }
    }

    public void telemetry(){
//        telemetry.addData("Speed Dance Progress", speedDance.progress);
//        telemetry.addData("Konami Dance Progress", konamiCode.progress);
//        telemetry.addData("Spin Dance Progress", spinDance.progress);
//        telemetry.update();
    }
}
