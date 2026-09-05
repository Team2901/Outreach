package com.bylazar.field;

/**
 * Virtual Robot's approximation of Panels' PanelsField singleton.
 *
 * Mirrors the Kotlin-object access pattern the real library uses
 * ({@code PanelsField.INSTANCE.getField()}, {@code ...getPresets().getPEDRO_PATHING()}).
 */
public class PanelsField {

    public static final PanelsField INSTANCE = new PanelsField();

    /**
     * Coordinate-system offset presets. On a real robot these tell Panels how to map
     * a library's coordinates onto the field view; in the simulator they are opaque
     * markers passed to {@link FieldManager#setOffsets(Object)} and never used.
     */
    public static class Presets {
        public Object getPEDRO_PATHING() { return "PEDRO_PATHING"; }
        public Object getROAD_RUNNER() { return "ROAD_RUNNER"; }
        public Object getDEFAULT() { return "DEFAULT"; }
    }

    private final FieldManager field = new FieldManager();
    private final Presets presets = new Presets();

    private PanelsField() { }

    public FieldManager getField() { return field; }

    public Presets getPresets() { return presets; }
}
