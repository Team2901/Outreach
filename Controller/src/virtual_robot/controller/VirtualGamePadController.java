package virtual_robot.controller;

import com.qualcomm.robotcore.hardware.Gamepad;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import virtual_robot.config.Config;

import java.security.Key;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class VirtualGamePadController {

    @FXML StackPane joyStickLeftPane;
    @FXML StackPane joyStickRightPane;
    @FXML Circle joyStickLeftHandle;
    @FXML
    Circle joyStickRightHandle;
    @FXML
    HBox gamepadBackground;
    @FXML
    Button btnX;
    @FXML Button btnY;
    @FXML Button btnA;
    @FXML Button btnB;
    @FXML Button btnDU;
    @FXML Button btnDL;
    @FXML Button btnDR;
    @FXML Button btnDD;
    @FXML Button btnLB;
    @FXML Button btnRB;
    @FXML Button btnBack;
    @FXML Button btnStart;
    @FXML Slider sldLeft;
    @FXML Slider sldRight;
    @FXML Label lblLeftStick;
    @FXML Label lblRightStick;
    @FXML Label lblLeftTrigger;
    @FXML Label lblRightTrigger;


    volatile float left_stick_x = 0;
    volatile float left_stick_y = 0;
    volatile float right_stick_x = 0;
    volatile float right_stick_y = 0;
    volatile boolean xPressed = false;
    volatile boolean yPressed = false;
    volatile boolean aPressed = false;
    volatile boolean bPressed = false;
    volatile boolean dUPressed = false;
    volatile boolean dLPressed = false;
    volatile boolean dRPressed = false;
    volatile boolean dDPressed = false;
    volatile boolean lBPressed = false;
    volatile boolean rBPressed = false;
    volatile boolean backPressed = false;
    volatile boolean startPressed = false;
    volatile float leftTrigger = 0;
    volatile float rightTrigger = 0;
    /*
     * The guide button has no widget on this panel, so it is pass-through only: a mouse-driven
     * panel always reports it as false. The stick buttons are pressed by right-clicking a
     * joystick.
     */
    volatile boolean guidePressed = false;
    volatile boolean leftStickButtonPressed = false;
    volatile boolean rightStickButtonPressed = false;

    // How thick a ring to draw round a joystick handle while its stick button is held.
    private static final double STICK_PRESSED_STROKE = 3.0;

    // Amber reads clearly against the panel's grey and still leaves the black glyphs legible.
    private static final String BUTTON_PRESSED_STYLE = "-fx-base: #ffb300;";

    /*
     * True while a physical gamepad is driving this panel. The panel is then read-only to the mouse:
     * the hardware owns the control positions, and a stray click must not fight it.
     */
    volatile boolean physicalGamePadAttached = false;

    // At most one un-run visual refresh is queued on the FX thread at a time.
    private final AtomicBoolean visualRefreshPending = new AtomicBoolean(false);

    VirtualRobotController virtualRobotController = null;

    ChangeListener<Number> sliderChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            leftTrigger = (float)sldLeft.getValue();
            rightTrigger = (float)sldRight.getValue();
            updateReadouts();
        }
    };

    public void initialize(){
        System.out.println("Initializing virtual gamepad");
        sldLeft.valueProperty().addListener(sliderChangeListener);
        sldRight.valueProperty().addListener(sliderChangeListener);
        updateReadouts();
        /*
         * Nothing on this panel is worth reaching by keyboard, and the focus ring lingers on
         * whichever control was clicked last, which reads as a second kind of "pressed".
         */
        for (Button btn : new Button[]{ btnX, btnY, btnA, btnB, btnDU, btnDL, btnDR, btnDD,
                btnLB, btnRB, btnBack, btnStart }) {
            btn.setFocusTraversable(false);
        }
        sldLeft.setFocusTraversable(false);
        sldRight.setFocusTraversable(false);

        btnDU.setGraphic(dPadArrow(0));
        btnDR.setGraphic(dPadArrow(90));
        btnDD.setGraphic(dPadArrow(180));
        btnDL.setGraphic(dPadArrow(270));
    }

    /**
     * One arrow for a D-pad button, pointing where `rotation` degrees clockwise from up says.
     *
     * Drawn rather than set as text because the arrow characters are not consistent with one
     * another: in most fonts the left and right triangles come out visibly smaller than the up
     * and down ones. These are a single triangle at four rotations, so they cannot disagree.
     */
    private static Polygon dPadArrow(double rotation){
        // Very nearly equilateral, so a quarter turn does not change how big it looks.
        Polygon arrow = new Polygon(0.0, 9.5, 11.0, 9.5, 5.5, 0.0);
        arrow.setRotate(rotation);
        return arrow;
    }

    void setVirtualRobotController(VirtualRobotController vrController){
        virtualRobotController = vrController;
    }


    /**
     * Whether a control let go of right now should hold its position rather than spring back.
     * SHIFT or ALT inverts Config.HOLD_CONTROLS_BY_DEFAULT, so whichever way that is set, the
     * modifier gives you the other behaviour without reaching for the config.
     */
    private boolean holdOnRelease(){
        boolean keyDown = virtualRobotController.getKeyState(KeyCode.SHIFT)
                || virtualRobotController.getKeyState(KeyCode.ALT);
        return keyDown != Config.HOLD_CONTROLS_BY_DEFAULT;
    }

    @FXML
    private void handleJoystickMouseEvent(MouseEvent arg){
        if (physicalGamePadAttached) return;
        if (!virtualRobotController.getOpModeInitialized()) return;

        /*
         * Right-click presses the stick button - the click you get on a real gamepad by pushing
         * the stick straight down. It is held for as long as the mouse button is, like every
         * other button on the panel, so it can be combined with a left-drag of the same stick.
         */
        boolean secondary = arg.getButton() == MouseButton.SECONDARY;
        if (secondary && arg.getEventType() == MouseEvent.MOUSE_PRESSED) {
            // A held button latches, so right-clicking one that is already down releases it.
            setStickButtonPressed(arg.getSource(),
                    holdOnRelease()? !isStickButtonPressed(arg.getSource()) : true);
            return;
        }
        if (arg.getEventType() == MouseEvent.MOUSE_EXITED
                || (secondary && arg.getEventType() == MouseEvent.MOUSE_RELEASED)) {
            if (!holdOnRelease()) setStickButtonPressed(arg.getSource(), false);
            if (secondary) return;
        }

        if (arg.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (!arg.isPrimaryButtonDown()) return;
            float x = (float) Math.max(10, Math.min(110, arg.getX()));
            float y = (float) Math.max(10, Math.min(110, arg.getY()));
            if (arg.getSource() == joyStickLeftPane) {
                joyStickLeftHandle.setTranslateX(x - 10);
                joyStickLeftHandle.setTranslateY(y - 10);
                left_stick_x = (x - 60.0f) / 50.0f;
                left_stick_y = (y - 60.0f) / 50.0f;
            } else if (arg.getSource() == joyStickRightPane) {
                joyStickRightHandle.setTranslateX(x - 10);
                joyStickRightHandle.setTranslateY(y - 10);
                right_stick_x = (x - 60.0f) / 50.0f;
                right_stick_y = (y - 60.0f) / 50.0f;
            }
            updateReadouts();
        } else if (arg.getEventType() == MouseEvent.MOUSE_RELEASED){
            if (!holdOnRelease()) {
                if (arg.getSource() == joyStickLeftPane) {
                    joyStickLeftHandle.setTranslateX(50);
                    joyStickLeftHandle.setTranslateY(50);
                    left_stick_x = 0;
                    left_stick_y = 0;
                } else if (arg.getSource() == joyStickRightPane) {
                    joyStickRightHandle.setTranslateX(50);
                    joyStickRightHandle.setTranslateY(50);
                    right_stick_x = 0;
                    right_stick_y = 0;
                }
                updateReadouts();
            }
        }
    }

    @FXML
    public void handleTriggerMouseEvent(MouseEvent arg){
        if (physicalGamePadAttached) return;
        if (arg.getEventType() == MouseEvent.MOUSE_RELEASED && !holdOnRelease()){
            ((Slider)arg.getSource()).setValue(0);
        }
    }

    @FXML
    private void handleGamePadButtonMouseEvent(MouseEvent arg){
        if (physicalGamePadAttached) return;
        if (!virtualRobotController.getOpModeInitialized()) return;
        Button btn = (Button)arg.getSource();

        if (arg.getEventType() == MouseEvent.MOUSE_PRESSED) {
            // A held button latches, so pressing one that is already down releases it.
            setButtonState(btn, holdOnRelease()? !isButtonPressed(btn) : true);
        } else if (arg.getEventType() == MouseEvent.MOUSE_EXITED
                || arg.getEventType() == MouseEvent.MOUSE_RELEASED) {
            if (!holdOnRelease()) setButtonState(btn, false);
        } else {
            return;
        }

        /*
         * JavaFX arms and disarms the button itself as the mouse goes down and up, which is the
         * wrong look for one that has just latched down, so put it back afterwards.
         */
        final Button target = btn;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                setButtonPressed(target, isButtonPressed(target));
            }
        });
    }

    private boolean isButtonPressed(Button btn){
        if (btn == btnX) return xPressed;
        else if (btn == btnY) return yPressed;
        else if (btn == btnA) return aPressed;
        else if (btn == btnB) return bPressed;
        else if (btn == btnDU) return dUPressed;
        else if (btn == btnDL) return dLPressed;
        else if (btn == btnDR) return dRPressed;
        else if (btn == btnDD) return dDPressed;
        else if (btn == btnLB) return lBPressed;
        else if (btn == btnRB) return rBPressed;
        else if (btn == btnBack) return backPressed;
        else if (btn == btnStart) return startPressed;
        return false;
    }

    private void setButtonState(Button btn, boolean pressed){
        if (btn == btnX) xPressed = pressed;
        else if (btn == btnY) yPressed = pressed;
        else if (btn == btnA) aPressed = pressed;
        else if (btn == btnB) bPressed = pressed;
        else if (btn == btnDU) dUPressed = pressed;
        else if (btn == btnDL) dLPressed = pressed;
        else if (btn == btnDR) dRPressed = pressed;
        else if (btn == btnDD) dDPressed = pressed;
        else if (btn == btnLB) lBPressed = pressed;
        else if (btn == btnRB) rBPressed = pressed;
        else if (btn == btnBack) backPressed = pressed;
        else if (btn == btnStart) startPressed = pressed;
    }

    /**
     * Attach or detach a physical gamepad from this panel. While one is attached the panel is driven
     * entirely by hardware, so the mouse is locked out of it; making the whole panel mouse
     * transparent is what stops the trigger sliders being dragged, since a Slider drag never
     * reaches handleTriggerMouseEvent.
     */
    void setPhysicalGamePadAttached(boolean attached){
        if (attached == physicalGamePadAttached) return;
        physicalGamePadAttached = attached;
        if (!attached) resetGamePad();
        final boolean mouseTransparent = attached;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gamepadBackground.setMouseTransparent(mouseTransparent);
            }
        });
    }

    /**
     * Copy the state of a physical gamepad into this panel. The input fields are written immediately,
     * on the polling thread, so getState() is current within the same tick; the controls are moved
     * to match on the FX thread. The panel, not the physical gamepad, is what the op mode then reads.
     */
    void applyPhysicalState(com.studiohartman.jamepad.ControllerState s){
        // Jamepad reports stick Y positive up; the panel (like the FTC SDK) is positive down.
        left_stick_x = s.leftStickX;
        left_stick_y = -s.leftStickY;
        right_stick_x = s.rightStickX;
        right_stick_y = -s.rightStickY;
        // A resting trigger can read a hair above zero; don't park the slider off zero for that.
        leftTrigger = s.leftTrigger < 0.05f? 0 : s.leftTrigger;
        rightTrigger = s.rightTrigger < 0.05f? 0 : s.rightTrigger;
        xPressed = s.x;
        yPressed = s.y;
        aPressed = s.a;
        bPressed = s.b;
        dUPressed = s.dpadUp;
        dLPressed = s.dpadLeft;
        dRPressed = s.dpadRight;
        dDPressed = s.dpadDown;
        lBPressed = s.lb;
        rBPressed = s.rb;
        backPressed = s.back;
        startPressed = s.start;
        guidePressed = s.guide;
        leftStickButtonPressed = s.leftStickClick;
        rightStickButtonPressed = s.rightStickClick;
        scheduleVisualRefresh();
    }

    /**
     * Queue a repaint of the controls from the current input fields, at most one at a time. The
     * refresh reads the fields when it runs rather than a snapshot from when it was queued, so a
     * skipped tick costs nothing: the pending refresh paints the newest state.
     */
    private void scheduleVisualRefresh(){
        if (!visualRefreshPending.compareAndSet(false, true)) return;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                visualRefreshPending.set(false);
                refreshVisuals();
            }
        });
    }

    private void refreshVisuals(){
        joyStickLeftHandle.setTranslateX(handleTranslate(left_stick_x));
        joyStickLeftHandle.setTranslateY(handleTranslate(left_stick_y));
        joyStickRightHandle.setTranslateX(handleTranslate(right_stick_x));
        joyStickRightHandle.setTranslateY(handleTranslate(right_stick_y));
        // The listener on each slider feeds the value back into leftTrigger/rightTrigger.
        sldLeft.setValue(leftTrigger);
        sldRight.setValue(rightTrigger);
        setButtonPressed(btnX, xPressed);
        setButtonPressed(btnY, yPressed);
        setButtonPressed(btnA, aPressed);
        setButtonPressed(btnB, bPressed);
        setButtonPressed(btnDU, dUPressed);
        setButtonPressed(btnDL, dLPressed);
        setButtonPressed(btnDR, dRPressed);
        setButtonPressed(btnDD, dDPressed);
        setButtonPressed(btnLB, lBPressed);
        setButtonPressed(btnRB, rBPressed);
        setButtonPressed(btnBack, backPressed);
        setButtonPressed(btnStart, startPressed);
        setStickRing(joyStickLeftHandle, leftStickButtonPressed);
        setStickRing(joyStickRightHandle, rightStickButtonPressed);
        updateReadouts();
    }

    private boolean isStickButtonPressed(Object joyStickPane){
        if (joyStickPane == joyStickLeftPane) return leftStickButtonPressed;
        return joyStickPane == joyStickRightPane && rightStickButtonPressed;
    }

    private void setStickButtonPressed(Object joyStickPane, boolean pressed){
        if (joyStickPane == joyStickLeftPane) {
            leftStickButtonPressed = pressed;
            setStickRing(joyStickLeftHandle, pressed);
        } else if (joyStickPane == joyStickRightPane) {
            rightStickButtonPressed = pressed;
            setStickRing(joyStickRightHandle, pressed);
        }
    }

    /**
     * Ring the handle in black while its stick button is held. The stroke is drawn INSIDE the
     * circle, so the handle does not change size as it comes and goes.
     */
    private static void setStickRing(Circle handle, boolean pressed){
        handle.setStrokeWidth(pressed? STICK_PRESSED_STROKE : 0);
    }

    /**
     * Inverse of the (x - 60) / 50 mapping in handleJoystickMouseEvent: turn a -1..1 axis value
     * back into a handle offset within the 120 x 120 joystick pane.
     */
    private static double handleTranslate(float axisValue){
        return 50.0 + 50.0 * Math.max(-1.0, Math.min(1.0, axisValue));
    }

    /**
     * Colour a button while it is down. Arming it instead would be the more native look, but the
     * shading is too slight to pick out at a glance - and a latched button can stay down for a
     * whole match, so it has to be obvious.
     */
    private static void setButtonPressed(Button btn, boolean pressed){
        String style = pressed? BUTTON_PRESSED_STYLE : "";
        if (!style.equals(btn.getStyle())) btn.setStyle(style);
    }

    /**
     * Show what the sticks and triggers are currently reporting, which is what an op mode reading
     * this panel sees.
     */
    private void updateReadouts(){
        lblLeftStick.setText(stickReadout(left_stick_x, left_stick_y));
        lblRightStick.setText(stickReadout(right_stick_x, right_stick_y));
        lblLeftTrigger.setText(String.format("%.2f", leftTrigger));
        lblRightTrigger.setText(String.format("%.2f", rightTrigger));
    }

    /**
     * Stick position, magnitude and angle. Stick y is positive downwards, so it is negated for
     * the angle, which then reads the usual way round: 0 to the right and rising anticlockwise,
     * putting a stick pushed forward at +90 degrees.
     */
    private static String stickReadout(float x, float y){
        double angle = Math.toDegrees(Math.atan2(-y, x));
        // A centred stick comes back as -0.0, which would read "-0.00".
        if (angle == 0.0) angle = 0.0;
        return String.format("x %.2f  y %.2f\nmag %.2f  %.2f\u00B0",
                x, y, Math.hypot(x, y), angle);
    }

    private void clearStickRings(){
        setStickRing(joyStickLeftHandle, false);
        setStickRing(joyStickRightHandle, false);
    }

    private void releaseAllButtons(){
        setButtonPressed(btnX, false);
        setButtonPressed(btnY, false);
        setButtonPressed(btnA, false);
        setButtonPressed(btnB, false);
        setButtonPressed(btnDU, false);
        setButtonPressed(btnDL, false);
        setButtonPressed(btnDR, false);
        setButtonPressed(btnDD, false);
        setButtonPressed(btnLB, false);
        setButtonPressed(btnRB, false);
        setButtonPressed(btnBack, false);
        setButtonPressed(btnStart, false);
    }

    void resetGamePad(){
        left_stick_y = 0;
        left_stick_x = 0;
        right_stick_x = 0;
        right_stick_y = 0;
        aPressed = false;
        bPressed = false;
        xPressed = false;
        yPressed = false;
        dUPressed = false;
        dLPressed = false;
        dRPressed = false;
        dDPressed = false;
        lBPressed = false;
        rBPressed = false;
        backPressed = false;
        startPressed = false;
        guidePressed = false;
        leftStickButtonPressed = false;
        rightStickButtonPressed = false;
        sldLeft.setValue(0);
        sldRight.setValue(0);
        leftTrigger = 0;
        rightTrigger = 0;
        joyStickLeftHandle.setTranslateX(50);
        joyStickLeftHandle.setTranslateY(50);
        joyStickRightHandle.setTranslateX(50);
        joyStickRightHandle.setTranslateY(50);

        interruptLEDandRumbleThreads();

        final String normalStyle = "-fx-background-color: #FFFFFF";
        if (Platform.isFxApplicationThread()){
            sldLeft.setStyle(normalStyle);
            sldRight.setStyle(normalStyle);
            gamepadBackground.setStyle(normalStyle);
            releaseAllButtons();
            clearStickRings();
            updateReadouts();
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sldLeft.setStyle(normalStyle);
                    sldRight.setStyle(normalStyle);
                    gamepadBackground.setStyle(normalStyle);
                    releaseAllButtons();
                    clearStickRings();
                    updateReadouts();
                }
            });
        }

    }


    public class ControllerState {

        public final float leftStickX;
        public final float leftStickY;
        public final float rightStickX;
        public final float rightStickY;
        public final boolean a;
        public final boolean b;
        public final boolean x;
        public final boolean y;
        public final boolean dpad_up;
        public final boolean dpad_left;
        public final boolean dpad_right;
        public final boolean dpad_down;
        public final boolean left_bumper;
        public final boolean right_bumper;
        public final boolean back;
        public final boolean start;
        public final boolean guide;
        public final boolean left_stick_button;
        public final boolean right_stick_button;
        public final float left_trigger;
        public final float right_trigger;

        public ControllerState(){
            leftStickX = VirtualGamePadController.this.left_stick_x;
            leftStickY = VirtualGamePadController.this.left_stick_y;
            rightStickX = VirtualGamePadController.this.right_stick_x;
            rightStickY = VirtualGamePadController.this.right_stick_y;
            a = VirtualGamePadController.this.aPressed;
            b = VirtualGamePadController.this.bPressed;
            x = VirtualGamePadController.this.xPressed;
            y = VirtualGamePadController.this.yPressed;
            dpad_up = VirtualGamePadController.this.dUPressed;
            dpad_left = VirtualGamePadController.this.dLPressed;
            dpad_right = VirtualGamePadController.this.dRPressed;
            dpad_down = VirtualGamePadController.this.dDPressed;
            left_bumper = VirtualGamePadController.this.lBPressed;
            right_bumper = VirtualGamePadController.this.rBPressed;
            back = VirtualGamePadController.this.backPressed;
            start = VirtualGamePadController.this.startPressed;
            guide = VirtualGamePadController.this.guidePressed;
            left_stick_button = VirtualGamePadController.this.leftStickButtonPressed;
            right_stick_button = VirtualGamePadController.this.rightStickButtonPressed;
            left_trigger = VirtualGamePadController.this.leftTrigger;
            right_trigger = VirtualGamePadController.this.rightTrigger;
        }
    }

    ControllerState getState() {
        return new ControllerState();
    }

    public class LEDPattern implements Runnable {
        Gamepad.LedEffect leds;
        HBox background;

        LEDPattern(Gamepad.LedEffect leds, HBox background) {
            this.leds = leds;
            this.background = background;
        }

        @Override
        public void run() {
            do {
                ListIterator<Gamepad.LedEffect.Step> stepIterator = leds.steps.listIterator();
                while (stepIterator.hasNext()) {
                    Gamepad.LedEffect.Step step = stepIterator.next();
                    final String styleString = String.format("-fx-background-color: #%02X%02X%02X", step.r, step.g, step.b);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            gamepadBackground.setStyle(styleString);
                        }
                    });

                    if (step.duration == -1) {
                        return;
                    }
                    try {
                        Thread.sleep(step.duration);
                    } catch (InterruptedException e) {
                        return;  // don't know why it was interrupted, but lets just bail on this sequence
                    }
                }
            } while (leds.repeating);
        }
    }

    public class RumblePattern implements Runnable {
        Gamepad.RumbleEffect rumbles;
        Slider sldLeft;
        Slider sldRight;
        HBox background;

        RumblePattern(Gamepad.RumbleEffect rumbles, Slider sldLeft, Slider sldRight) {
            this.rumbles = rumbles;
            this.sldLeft = sldLeft;
            this.sldRight = sldRight;
        }

        @Override
        public void run() {
            ListIterator<Gamepad.RumbleEffect.Step> stepIterator = rumbles.steps.listIterator();
            while (stepIterator.hasNext()) {
                Gamepad.RumbleEffect.Step step = stepIterator.next();
                final String leftStyle = String.format("-fx-background-color: #%02X%02X%02X", 255 - step.large, 255 - step.large, 255 - step.large);
                final String rightStyle = String.format("-fx-background-color: #%02X%02X%02X", 255 - step.small, 255 - step.small, 255 - step.small);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sldLeft.setStyle(leftStyle);
                        sldRight.setStyle(rightStyle);
                    }
                });

                if (step.duration == -1) {
                    return;
                }
                try {
                    Thread.sleep(step.duration);
                } catch (InterruptedException e) {
                    return;  // don't know why it was interrupted, but lets just bail on this sequence
                }
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    sldLeft.setStyle("-fx-background-color: #FFFFFF");
                    sldRight.setStyle("-fx-background-color: #FFFFFF");
                }
            });
        }
    }

    Thread ledThread;
    Thread rumbleThread;


    /**
     * Show an LED effect as the background color of the panel. The effect is passed in rather than
     * polled from the Gamepad here, because a physical gamepad driving this panel needs the same
     * effect, and polling drains the queue.
     */
    void applyLedEffect(Gamepad.LedEffect leds) {
        if (leds == null) return;
        if (this.ledThread != null) {
            this.ledThread.interrupt();
        }
        this.ledThread = new Thread(new LEDPattern(leds, gamepadBackground));
        this.ledThread.start();
    }

    /**
     * Show a rumble effect as the background color of the trigger sliders.
     */
    void applyRumbleEffect(Gamepad.RumbleEffect rumbles) {
        if (rumbles == null) return;
        if (this.rumbleThread != null) {
            this.rumbleThread.interrupt();
        }
        this.rumbleThread = new Thread(new RumblePattern(rumbles, sldLeft, sldRight));
        this.rumbleThread.start();
    }

    void interruptLEDandRumbleThreads(){
        if (ledThread != null){
            ledThread.interrupt();
        }
        if (rumbleThread != null){
            rumbleThread.interrupt();
        }
    }
}
