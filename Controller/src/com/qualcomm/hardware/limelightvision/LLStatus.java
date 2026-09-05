package com.qualcomm.hardware.limelightvision;

import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;

/**
 * Virtual Robot's approximation of the FTC SDK's LLStatus.
 *
 * Reports an idle, disconnected Limelight - the simulator has no camera hardware.
 */
public class LLStatus {

    public Quaternion getCameraQuat() { return new Quaternion(); }
    public int getCid() { return 0; }
    public double getCpu() { return 0; }
    public double getFinalYaw() { return 0; }
    public double getFps() { return 0; }
    public int getHwType() { return 0; }
    public String getName() { return "Limelight 3A (simulated)"; }
    public int getPipeImgCount() { return 0; }
    public int getPipelineIndex() { return 0; }
    public String getPipelineType() { return "none"; }
    public double getRam() { return 0; }
    public int getSnapshotMode() { return 0; }
    public double getTemp() { return 0; }

    @Override
    public String toString() {
        return "LLStatus(simulated: no Limelight in the Virtual Robot)";
    }
}
