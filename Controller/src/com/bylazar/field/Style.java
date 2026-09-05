package com.bylazar.field;

/**
 * Virtual Robot's approximation of Panels' field-drawing Style.
 *
 * Holds the fill/outline/opacity a drawing call would use on the Panels field view.
 * The simulator does not render the Panels field, so these values are carried but
 * never drawn.
 */
public class Style {

    private String outline;
    private String fill;
    private double opacity;

    public Style() {
        this("", "", 1.0);
    }

    public Style(String outline, String fill, double opacity) {
        this.outline = outline;
        this.fill = fill;
        this.opacity = opacity;
    }

    public String getOutline() { return outline; }
    public void setOutline(String outline) { this.outline = outline; }

    public String getFill() { return fill; }
    public void setFill(String fill) { this.fill = fill; }

    public double getOpacity() { return opacity; }
    public void setOpacity(double opacity) { this.opacity = opacity; }
}
