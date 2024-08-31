package org.firstinspires.ftc.teamcode.Utilities;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.ElapsedTime;

public class DDRGamepad {
    private final Gamepad hardwareGamepad;
    private final ElapsedTime timer;
    private final String name;

    // Dpad left
    public Button leftArrow;

    // b button
    public Button downArrow;

    // Left bumper
    public Button topLeftArrow;

    // Right bumper
    public Button topRightArrow;

    // Left stick button
    public Button upArrow;

    // Right stick button
    public Button rightArrow;

    // Start button
    public Button startButton;

    // Using a to start robot
    public Button a;

    public DDRGamepad(final Gamepad hardwareGamepad, final ElapsedTime timer, final String name) {

        this.hardwareGamepad = hardwareGamepad;
        this.timer = timer;
        this.name = (null != name) ? name : "";

        this.leftArrow = new Button(String.format("%s_left_arrow", this.name));
        this.downArrow = new Button(String.format("%s_down_arrow", this.name));
        this.topLeftArrow = new Button(String.format("%s_top_left_arrow", this.name));
        this.topRightArrow = new Button(String.format("%s_top_right_arrow", this.name));
        this.upArrow = new Button(String.format("%s_up_arrow", this.name));
        this.rightArrow = new Button(String.format("%s_right_arrow", this.name));
        this.startButton = new Button(String.format("%s_start_button", this.name));
        this.a = new Button(String.format("%s_a_button", this.name));
    }

    public void update() {

        double time = timer.time();

        leftArrow.update(hardwareGamepad.dpad_left, time);
        downArrow.update(hardwareGamepad.b, time);
        topLeftArrow.update(hardwareGamepad.left_bumper, time);
        topRightArrow.update(hardwareGamepad.right_bumper, time);
        upArrow.update(hardwareGamepad.left_stick_button, time);
        rightArrow.update(hardwareGamepad.right_stick_button, time);
        startButton.update(hardwareGamepad.start, time);
        a.update(hardwareGamepad.a, time);
    }

    public boolean areButtonsActive(){
        return leftArrow.pressed || (downArrow.pressed && !startButton.pressed) || topLeftArrow.pressed || topRightArrow.pressed || upArrow.pressed || rightArrow.pressed;
    }
    public boolean areButtonsInitialPress(){
        return leftArrow.isInitialPress() || (downArrow.isInitialPress() && !startButton.pressed) || topLeftArrow.isInitialPress() || topRightArrow.isInitialPress() || upArrow.isInitialPress() || rightArrow.isInitialPress();
    }
}
