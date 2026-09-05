package com.qualcomm.hardware.limelightvision;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual Robot's approximation of the FTC SDK's LLResult.
 *
 * The simulator has no Limelight camera. {@link Limelight3A#getLatestResult()} always
 * returns null, so in practice OpModes take their "no target" branch and never see an
 * instance of this class. It exists so Limelight OpModes compile and run unchanged;
 * should one ever be constructed, {@link #isValid()} reports false.
 */
public class LLResult {

    public boolean isValid() { return false; }

    public long getControlHubTimeStamp() { return 0; }
    public long getControlHubTimeStampNanos() { return 0; }
    public long getStaleness() { return Long.MAX_VALUE; }

    public List<LLResultTypes.BarcodeResult> getBarcodeResults() { return new ArrayList<>(); }
    public List<LLResultTypes.ClassifierResult> getClassifierResults() { return new ArrayList<>(); }
    public List<LLResultTypes.DetectorResult> getDetectorResults() { return new ArrayList<>(); }
    public List<LLResultTypes.FiducialResult> getFiducialResults() { return new ArrayList<>(); }
    public List<LLResultTypes.ColorResult> getColorResults() { return new ArrayList<>(); }

    public double getFocusMetric() { return 0; }

    public Pose3D getBotpose() { return LLResultTypes.emptyPose(); }
    public Pose3D getBotpose_MT2() { return LLResultTypes.emptyPose(); }

    public double[] getStddevMt1() { return new double[0]; }
    public double[] getStddevMt2() { return new double[0]; }

    public int getBotposeTagCount() { return 0; }
    public double getBotposeSpan() { return 0; }
    public double getBotposeAvgDist() { return 0; }
    public double getBotposeAvgArea() { return 0; }

    public double[] getPythonOutput() { return new double[0]; }

    public double getCaptureLatency() { return 0; }
    public double getTargetingLatency() { return 0; }
    public double getParseLatency() { return 0; }

    public String getPipelineType() { return "none"; }
    public int getPipelineIndex() { return 0; }

    public double getTx() { return 0; }
    public double getTy() { return 0; }
    public double getTxNC() { return 0; }
    public double getTyNC() { return 0; }
    public double getTa() { return 0; }

    public double getTimestamp() { return 0; }

    @Override
    public String toString() {
        return "LLResult(simulated: no Limelight in the Virtual Robot)";
    }
}
