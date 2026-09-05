package com.qualcomm.hardware.limelightvision;

import java.util.ArrayList;
import java.util.List;

/**
 * Virtual Robot's approximation of the FTC SDK's LLFieldMap.
 *
 * Present so that Limelight OpModes that reference field maps compile; the simulator
 * never uploads one to real hardware.
 */
public class LLFieldMap {

    public static class Fiducial {
        private final int id;
        private final double size;
        private final String family;
        private final List<Double> transform;
        private final boolean unique;

        public Fiducial() {
            this(0, 0, "", new ArrayList<>(), false);
        }

        public Fiducial(int id, double size, String family, List<Double> transform, boolean isUnique) {
            this.id = id;
            this.size = size;
            this.family = family;
            this.transform = transform;
            this.unique = isUnique;
        }

        public int getId() { return id; }
        public double getSize() { return size; }
        public String getFamily() { return family; }
        public List<Double> getTransform() { return transform; }
        public boolean isUnique() { return unique; }
    }

    private final List<Fiducial> fiducials;
    private final String type;

    public LLFieldMap() {
        this(new ArrayList<>(), "");
    }

    public LLFieldMap(List<Fiducial> fiducials, String type) {
        this.fiducials = fiducials;
        this.type = type;
    }

    public List<Fiducial> getFiducials() { return fiducials; }
    public String getType() { return type; }
    public int getNumberOfTags() { return fiducials.size(); }
    public boolean isValid() { return !fiducials.isEmpty(); }
}
