package org.firstinspires.ftc.teamcode.Utilities;

public class AnalogInput extends InputMechanism<Double> {
    private final boolean invertAxis;
    double deadZone = 0.0;

    public AnalogInput(String name) {
        this(name, false);
    }

    public AnalogInput(String name, boolean invertAxis) {
        super(name);
        this.invertAxis = invertAxis;
        update(0.0, 0.0);
    }

    @Override
    protected boolean isPressed(Double rawValue) {
        return (null != rawValue) && (Math.abs(rawValue) > deadZone);
    }

    @Override
    public Double getRawValue() {
        if ((null != rawValue) && (rawValue != 0)) {
            // Invert the rawValue if needed
            return this.invertAxis ? -rawValue : rawValue;
        } else {
            return 0.0;
        }
    }

    @Override
    public Double getValue() {
        if (this.isPressed()) {
            // Adjust the rawValue given the size of the dead zone
            Double rawValue = this.getRawValue();
            return (rawValue - (Math.signum(rawValue) * deadZone)) / (1.0 - deadZone);
        } else {
            // Default unpressed values to 0.0
            return 0.0;
        }
    }

    public void setDeadZone(double newValue) {
        deadZone = newValue;
    }
}
