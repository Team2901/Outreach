package org.firstinspires.ftc.teamcode.Utilities;


public abstract class InputMechanism<T> {

    protected final String name;
    protected T rawValue;

    protected Boolean pressed = null;
    protected Double lastUpdateTime = null;
    protected Double lastChangeTime = null;
    private int pressedCounts = 0;
    private int releaseCounts = 0;

    public InputMechanism(final String name) {
        this.name = name;
    }

    public void update(final T updateValue, final double updateTime) {
        // Call the abstract method to determine if this input mechanism is interacted-with (pressed)
        final boolean updatePressed = isPressed(updateValue);

        // Update the counts
        if (isInitialValueChange()) {
            if (pressed == null) {
                // do nothing
            }
            else if (pressed) {
                this.pressedCounts++;
            } else {
                this.releaseCounts++;
            }
        }

        // Update the last changed time
        if ((pressed == null) || (updatePressed != pressed)) {
            this.lastChangeTime = updateTime;
        }

        // Update the values
        rawValue = updateValue;
        pressed = updatePressed;
        lastUpdateTime = updateTime;
    }

    /**
     * Get the name
     *
     * @return the button's name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the timestamp at which the button's state was last updated
     *
     * @return the timestamp at which the button's state was last updated
     */
    public Double getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Get the timestamp at which the button last transitioned states
     *
     * @return the timestamp at which the button last transitioned states
     */
    public Double getLastChangeTime() {
        return lastChangeTime;
    }

    /**
     * Get if the button is in the pressed state
     *
     * @return true if the button is in the pressed state
     */
    public boolean isPressed() {
        return pressed;
    }

    /**
     * Get if the button is in the released state
     *
     * @return true if the button is in the released state (unpressed)
     */
    public boolean isReleased() {
        return !isPressed();
    }

    /**
     * Get the length of time that the button has been in its current pressed/released state
     *
     * @return the length of time that the button has been in its current pressed/released state
     */
    public double getValueElapseTime() {
        return (null != lastChangeTime) ? lastUpdateTime - lastChangeTime : lastUpdateTime;
    }

    /**
     * Returns if the button is transitioning from one state to another
     *
     * @return true if the button is transitioning from one state to another
     */
    public boolean isInitialValueChange() {
        return (lastUpdateTime == null) || lastUpdateTime.equals(lastChangeTime);
    }

    /**
     * Returns if the button is transitioning from the released to pressed state
     *
     * @return true if the button is transitioning from the released to pressed state
     */
    public boolean isInitialPress() {
        return isPressed() && isInitialValueChange();
    }

    /**
     * Returns if the button is transitioning from the released to pressed state
     *
     * @return true if the button is transitioning from the pressed to released state
     */
    public boolean isInitialRelease() {
        return isReleased() && isInitialValueChange();
    }

    /**
     * Get the amount of time that the button has been in the pressed state
     *
     * @return the amount of time that the button has been in the pressed state
     */
    public double getPressedElapseTime() {
        return isPressed() ? getValueElapseTime() : 0;
    }

    /**
     * Get the amount of time that the button has been in the released state
     *
     * @return the amount of time that the button has been in the released state
     */
    public double getReleaseElapseTime() {
        return isReleased() ? getValueElapseTime() : 0;
    }

    /**
     * Get the number of times the button has transitioned from the released to pressed state
     *
     * @return number of times the button has transitioned from the released to pressed state
     */
    public int getPressedCounts() {
        return pressedCounts;
    }

    /**
     * Get the number of times the button has transitioned from the pressed to released state
     *
     * @return number of times the button has transitioned from the pressed to released state
     */
    public int getReleaseCounts() {
        return releaseCounts;
    }

    /**
     * The raw value of the button. Ignores any deadzones
     *
     * @return The raw value of the button. Ignores any deadzones
     */
    public T getRawValue() {
        return rawValue;
    }

    /**
     * The value of the button, adjusted from any deadzones
     *
     * @return The value of the button, adjusted from any deadzones
     */
    public abstract T getValue();

    /**
     * Get if the given rawValue is in the pressed state
     *
     * @param rawValue the button's raw value
     * @return true if rawValue is in the pressed state
     */
    protected abstract boolean isPressed(final T rawValue);

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Button{");
        sb.append("name='").append(name).append('\'');
        sb.append(", rawValue=").append(rawValue);
        sb.append(", pressed=").append(pressed);
        sb.append('}');
        return sb.toString();
    }
}