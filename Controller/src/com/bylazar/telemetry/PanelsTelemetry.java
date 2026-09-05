package com.bylazar.telemetry;

/**
 * Virtual Robot's approximation of Panels' PanelsTelemetry singleton.
 *
 * Mirrors the Kotlin-object access pattern the real library uses
 * ({@code PanelsTelemetry.INSTANCE.getTelemetry()}).
 */
public class PanelsTelemetry {

    public static final PanelsTelemetry INSTANCE = new PanelsTelemetry();

    private final TelemetryManager telemetry = new TelemetryManager();

    private PanelsTelemetry() { }

    public TelemetryManager getTelemetry() {
        return telemetry;
    }
}
