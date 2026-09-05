package com.qualcomm.hardware.limelightvision;

import com.qualcomm.robotcore.hardware.HardwareDevice;

/**
 * Virtual Robot's approximation of the FTC SDK's Limelight3A driver.
 *
 * The simulator has no camera hardware, so this is a no-op device: it accepts every
 * call an OpMode would make (start/stop, pipelineSwitch, updateRobotOrientation) and
 * reports "not connected, no result". {@link #getLatestResult()} returns null, which
 * is exactly what the real driver returns before the first frame arrives - so OpModes
 * written defensively (`if (result != null && result.isValid())`) behave correctly.
 *
 * To actually see targets in the simulator, a Limelight would have to be modelled
 * against the field's AprilTags; that is not implemented.
 */
public class Limelight3A implements HardwareDevice {

    private boolean running = false;
    private int pipelineIndex = 0;
    private double robotYawDegrees = 0;

    public synchronized void start() { running = true; }
    public synchronized void pause() { running = false; }
    public synchronized void stop() { running = false; }
    public boolean isRunning() { return running; }

    public synchronized void setPollRateHz(int rateHz) { }
    public long getTimeSinceLastUpdate() { return Long.MAX_VALUE; }

    /** Always false: there is no Limelight attached to a simulated robot. */
    public boolean isConnected() { return false; }

    /** Always null, mirroring the real driver before any frame has been received. */
    public LLResult getLatestResult() { return null; }

    public LLStatus getStatus() { return new LLStatus(); }

    public boolean reloadPipeline() { return false; }

    public boolean pipelineSwitch(int index) {
        this.pipelineIndex = index;
        return true;
    }

    public int getPipelineIndex() { return pipelineIndex; }

    public boolean captureSnapshot(String snapname) { return false; }
    public boolean deleteSnapshots() { return false; }
    public boolean deleteSnapshot(String snapname) { return false; }

    public boolean updatePythonInputs(double input1, double input2, double input3, double input4,
                                      double input5, double input6, double input7, double input8) {
        return false;
    }

    public boolean updatePythonInputs(double[] inputs) { return false; }

    public boolean updateRobotOrientation(double yaw) {
        this.robotYawDegrees = yaw;
        return true;
    }

    public double getRobotOrientation() { return robotYawDegrees; }

    public boolean uploadPipeline(String jsonString, Integer index) { return false; }
    public boolean uploadFieldmap(LLFieldMap fieldmap, Integer index) { return false; }
    public boolean uploadPython(String pythonString, Integer index) { return false; }

    public void shutdown() { running = false; }

    @Override
    public Manufacturer getManufacturer() { return Manufacturer.Other; }

    @Override
    public String getDeviceName() { return "Limelight 3A (simulated)"; }

    @Override
    public String getConnectionInfo() { return "Virtual Robot: no Limelight hardware"; }

    @Override
    public int getVersion() { return 1; }

    @Override
    public void resetDeviceConfigurationForOpMode() {
        running = false;
        pipelineIndex = 0;
    }

    @Override
    public void close() { running = false; }
}
