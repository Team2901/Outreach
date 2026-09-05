package com.bylazar.field;

/**
 * Virtual Robot's approximation of Panels' FieldManager.
 *
 * The real FieldManager draws the robot, paths and pose history onto the Panels web
 * field view. The simulator draws the robot on its own JavaFX field, so every drawing
 * call here is accepted and discarded - the cursor and style are tracked so that
 * sequences of moveCursor/line calls behave sanely, but nothing is rendered.
 */
public class FieldManager {

    private double cursorX = 0;
    private double cursorY = 0;
    private Style style = new Style();
    private Object offsets;

    public void setStyle(Style style) { this.style = style; }
    public Style getStyle() { return style; }

    public void setOffsets(Object offsets) { this.offsets = offsets; }
    public Object getOffsets() { return offsets; }

    public void moveCursor(double x, double y) {
        this.cursorX = x;
        this.cursorY = y;
    }

    public double getCursorX() { return cursorX; }
    public double getCursorY() { return cursorY; }

    /** Draws a line from the cursor to (x, y); the cursor ends at (x, y). */
    public void line(double x, double y) {
        this.cursorX = x;
        this.cursorY = y;
    }

    public void circle(double radius) { }
    public void rect(double width, double height) { }

    /** No-op: there is no Panels field view to flush to. */
    public void update() { }
}
