package com.qualcomm.hardware.limelightvision;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual Robot's approximation of the FTC SDK's Limelight result types.
 *
 * The simulator has no Limelight camera, so these carry no real detections -
 * they exist so that OpModes using the Limelight compile and run unchanged.
 * Every accessor returns a neutral "nothing detected" value.
 */
public class LLResultTypes {

    /** An empty pose, used wherever the real SDK would report a detected pose. */
    static Pose3D emptyPose() {
        return new Pose3D(
                new Position(DistanceUnit.INCH, 0, 0, 0, 0),
                new YawPitchRollAngles(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES,
                        0, 0, 0, 0));
    }

    public static class FiducialResult {
        public int getFiducialId() { return -1; }
        public String getFamily() { return ""; }
        public List<List<Double>> getTargetCorners() { return new ArrayList<>(); }
        public double getSkew() { return 0; }
        public Pose3D getCameraPoseTargetSpace() { return emptyPose(); }
        public Pose3D getRobotPoseFieldSpace() { return emptyPose(); }
        public Pose3D getRobotPoseTargetSpace() { return emptyPose(); }
        public Pose3D getTargetPoseCameraSpace() { return emptyPose(); }
        public Pose3D getTargetPoseRobotSpace() { return emptyPose(); }
        public double getTargetArea() { return 0; }
        public double getTargetXPixels() { return 0; }
        public double getTargetYPixels() { return 0; }
        public double getTargetXDegrees() { return 0; }
        public double getTargetYDegrees() { return 0; }
        public double getTargetXDegreesNoCrosshair() { return 0; }
        public double getTargetYDegreesNoCrosshair() { return 0; }
    }

    public static class BarcodeResult {
        public String getFamily() { return ""; }
        public String getData() { return ""; }
        public List<List<Double>> getTargetCorners() { return new ArrayList<>(); }
        public double getTargetArea() { return 0; }
        public double getTargetXPixels() { return 0; }
        public double getTargetYPixels() { return 0; }
        public double getTargetXDegrees() { return 0; }
        public double getTargetYDegrees() { return 0; }
    }

    public static class ClassifierResult {
        public int getClassId() { return -1; }
        public String getClassName() { return ""; }
        public double getConfidence() { return 0; }
    }

    public static class DetectorResult {
        public int getClassId() { return -1; }
        public String getClassName() { return ""; }
        public double getConfidence() { return 0; }
        public List<List<Double>> getTargetCorners() { return new ArrayList<>(); }
        public double getTargetArea() { return 0; }
        public double getTargetXPixels() { return 0; }
        public double getTargetYPixels() { return 0; }
        public double getTargetXDegrees() { return 0; }
        public double getTargetYDegrees() { return 0; }
    }

    public static class ColorResult {
        public List<List<Double>> getTargetCorners() { return new ArrayList<>(); }
        public double getTargetArea() { return 0; }
        public double getTargetXPixels() { return 0; }
        public double getTargetYPixels() { return 0; }
        public double getTargetXDegrees() { return 0; }
        public double getTargetYDegrees() { return 0; }
    }
}
