package com.bylazar.telemetry;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Virtual Robot's approximation of Panels' TelemetryManager.
 *
 * Panels is a web dashboard served from the Robot Controller; there is no such
 * dashboard in the simulator. Rather than discard the data, this stub forwards
 * everything to the simulator's own telemetry window when an OpMode hands one over
 * (via {@link #update(Telemetry)}), and otherwise buffers lines and prints them to
 * the console on {@link #update()}. That way Panels-instrumented OpModes still show
 * their output while running in the simulator.
 */
public class TelemetryManager {

    private final StringBuilder pending = new StringBuilder();
    private Telemetry sdkTelemetry;

    /** Lets the simulator wire its own telemetry in, so addData shows in the UI. */
    public void setTelemetry(Telemetry telemetry) {
        this.sdkTelemetry = telemetry;
    }

    public void addData(String caption, Object value) {
        if (sdkTelemetry != null) {
            sdkTelemetry.addData(caption, value);
        } else {
            pending.append(caption).append(" : ").append(value).append(System.lineSeparator());
        }
    }

    public void addLine(String line) {
        if (sdkTelemetry != null) {
            sdkTelemetry.addLine(line);
        } else {
            pending.append(line).append(System.lineSeparator());
        }
    }

    /** Panels' "debug" channel; in the simulator it is just another telemetry line. */
    public void debug(String caption, Object value) {
        addData(caption, value);
    }

    public void debug(String line) {
        addLine(line);
    }

    public void update() {
        if (sdkTelemetry != null) {
            sdkTelemetry.update();
        } else if (pending.length() > 0) {
            System.out.print(pending);
            pending.setLength(0);
        }
    }

    /** Panels' overload that also pushes to the normal Driver Station telemetry. */
    public void update(Telemetry telemetry) {
        this.sdkTelemetry = telemetry;
        if (pending.length() > 0) {
            for (String line : pending.toString().split(System.lineSeparator())) {
                if (!line.isEmpty()) telemetry.addLine(line);
            }
            pending.setLength(0);
        }
        telemetry.update();
    }
}
