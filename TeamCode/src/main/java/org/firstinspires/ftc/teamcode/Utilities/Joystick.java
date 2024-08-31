package org.firstinspires.ftc.teamcode.Utilities;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class Joystick {

    public final AnalogInput x;
    public final AnalogInput y;
    public final Button click;

    public final AnalogInput radius;
    public final AnalogInput angle;
    public final AngleUnit angleUnit = AngleUnit.DEGREES;
    /*double rawAngle;
    double angle = 0;
    double rawRadius;
    double radius = 0;*/

    public Joystick(final String name) {
        x = new AnalogInput(String.format("%s_x", name), false);
        y = new AnalogInput(String.format("%s_y", name), true);
        click = new Button(String.format("%s_click", name));

        // These are derived/virtual "inputs", but work through the power of abstraction...
        radius = new AnalogInput(String.format("%s_radius", name), false);
        angle = new AnalogInput(String.format("%s_angle", name), false);
    }

    public void update(final Double xUpdateValue, final Double yUpdateValue, final Boolean clickUpdateValue, final Double updateTime) {
        x.update(xUpdateValue, updateTime);
        y.update(yUpdateValue, updateTime);
        click.update(clickUpdateValue, updateTime);

        // offset by 90 degrees (pi/2 rad) so that up = forward = 0 angle
        // Note: The AngleUnit class will also normalize, even if radians->radians
        double raw_x = this.x.getRawValue();
        double raw_y = this.y.getRawValue();
        Double rawAngle = angleUnit.fromRadians(Math.atan2(raw_y, raw_x) - Math.PI/2);
        Double rawRadius = Math.sqrt(raw_x*raw_x + raw_y*raw_y);
        radius.update(rawRadius, updateTime);
        // Special handling for angle update. Only update the raw angle if the raw radius is "pressed"
        if (radius.isPressed()) {
            angle.update(rawAngle, updateTime);
        } else {
            // We still want to update the update time...
            angle.update(angle.getRawValue(), updateTime);
        }
    }

    public void setDeadZone(double newValue) {
        x.setDeadZone(newValue);
        y.setDeadZone(newValue);
        radius.setDeadZone(newValue);
        angle.setDeadZone(newValue);
    }
}