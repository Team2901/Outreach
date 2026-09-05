package org.firstinspires.ftc.teamcode.test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TeleOp(name = "Servo Test", group = "test")
public class ServoTest extends OpMode {
    List<Map.Entry<String, Servo>> servoList = new ArrayList<>();
    // Creates list/map? of all servos on device (robor)

    Integer activeIndex = (0);
    // Holds the value of the current servo being tested

    public void help() {
        telemetry.addData("dpad up/pdown", "select servo");
        telemetry.addData("a/y", "+/- 0.1");
        telemetry.addData("b/x", "+/- 0.01");
        telemetry.addLine("");
    }

    public void telemetry(){
        // Displays current servo's assigned name
        telemetry.addData("current servo", servoList.get(activeIndex).getKey());

        //Displays current servo's position
        telemetry.addData("servo position", servoList.get(activeIndex).getValue().getPosition());
    }

    @Override
    public void init() {
        // Finds all of the servos on device (robor) using the hardwareMap
        Set<Map.Entry<String, Servo>> servoSet = this.hardwareMap.servo.entrySet();

        // Adds all of those servos to our servo list
        servoList.addAll(servoSet);

        // Calls help method (defined above)
        help();
    }

    @Override
    public void loop() {
        // Changes current servo to next servo in list
        if(gamepad1.dpadUpWasPressed()){
            activeIndex++;
        }

        // Changes current servo to previous servo in list
        if(gamepad1.dpadDownWasPressed()){
            activeIndex--;
        }

        // if current index goes above our list size (outside of list), we move it
        // so that it loops around to our first servo in the list
        if(activeIndex == servoList.size()){
            activeIndex = 0;
        }

        // if current index goes below our list size (outside of list), we move it
        // so that it loops around to our last servo in the list
        if(activeIndex == -1){
            activeIndex = servoList.size() - 1;
        }

        // Creates our servo by looking at the first servo from our list/map
        Servo s = servoList.get(activeIndex).getValue();

        // Gets the position of our servo
        double position = s.getPosition();

        // increases the position of current servo by 0.1 of a tick
        if (gamepad1.yWasPressed()){
            position += 0.1;
        }

        // decreases the position of current servo by 0.1 of a tick
        if (gamepad1.aWasPressed()){
            position -= 0.1;
        }

        // increases the position of current servo by 0.01 of a tick
        if (gamepad1.bWasPressed()){
            position += 0.01;
        }

        // decreases the position of current servo by 0.01 of a tick
        if (gamepad1.xWasPressed()){
            position -= 0.01;
        }

        // Sets the position as the minimum between
        position = Math.min(1, Math.max(0, position));
        s.setPosition(position);

        // Calls help method (defined above)
        help();

        // Calls telemetry method (defined above)
        telemetry();
    }
}
