package org.firstinspires.ftc.teamcode.Utilities;


public class Button extends InputMechanism<Boolean> {
    public Button(final String name) {
        super(name);
        update(false, 0.0);
    }

    @Override
    protected boolean isPressed(final Boolean rawValue) {
        return (null != rawValue) ? rawValue : false;
    }

    @Override
    public Boolean getValue() {
        // Nothing fancy for Button
        // Raw value is the same as value
        // If no raw value, assume false
        return (null != rawValue) ? rawValue : false;
    }
}
