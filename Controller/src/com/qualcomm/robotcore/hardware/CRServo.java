package com.qualcomm.robotcore.hardware;

/**
 * CRServo interface included for compatibility with FTC SDK. Its motion functionality is found in
 * the DcMotorSimple interface; the controller/port accessors below mirror the real SDK so that
 * team code implementing CRServo compiles unchanged.
 */
public interface CRServo extends DcMotorSimple {

    /**
     * Returns the underlying servo controller on which this servo is situated.
     * Simulated servos are not attached to a controller, so this defaults to null.
     * @return the underlying servo controller on which this servo is situated
     * @see #getPortNumber()
     */
    default ServoController getController() {
        return null;
    }

    /**
     * Returns the port number on the underlying servo controller where this servo is situated.
     * @return the port number on the underlying servo controller
     * @see #getController()
     */
    default int getPortNumber() {
        return 0;
    }
}
