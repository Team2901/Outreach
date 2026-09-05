package virtual_robot.controller;

import com.qualcomm.hardware.CommonOdometry;
import com.qualcomm.robotcore.eventloop.opmode.*;
import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.util.Range;
import com.studiohartman.jamepad.ControllerIndex;
import com.studiohartman.jamepad.ControllerManager;
import com.studiohartman.jamepad.ControllerState;
import com.studiohartman.jamepad.ControllerUnpluggedException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.ContinuousDetectionMode;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl;
import org.reflections.Reflections;
import virtual_robot.config.Config;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import virtual_robot.robots.ControlsElements;
import virtual_robot.robots.classes.MecanumBot;
import virtual_robot.keyboard.KeyState;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * For internal use only. Controller class for the JavaFX application.
 */
public class VirtualRobotController {

    //User Interface
    @FXML private Pane fieldPane;
    @FXML ImageView imgViewBackground;
    @FXML private ComboBox<Class<?>> cbxConfig;
    @FXML private Button driverButton;
    @FXML private ComboBox<Class<?>> cbxOpModes;
    @FXML private Slider sldRandomMotorError;
    @FXML private Slider sldSystematicMotorError;
    @FXML private Slider sldMotorInertia;
    @FXML private TextArea txtTelemetry;
    @FXML private ComboBox<GamePadSource> cbxGamePad1Source;
    @FXML private ComboBox<GamePadSource> cbxGamePad2Source;
    @FXML private BorderPane borderPane;
    @FXML private CheckBox cbxShowPath;
    @FXML private CheckBox checkBoxAutoHuman;
    @FXML private Label lblRunTime;
    @FXML private HBox hbxGamePadPanels;

    // dyn4j world
    World<Body> world = new World<>();

    // Virtual Hardware
    private HardwareMap hardwareMap = null;
    private VirtualBot bot = null;
    Gamepad gamePad1 = new Gamepad();
    Gamepad gamePad2 = new Gamepad();
    GamePadHelper gamePadHelper = null;
    /**
     * What drives each virtual gamepad. Written on the FX thread, read by the gamepad polling
     * thread.
     */
    private volatile GamePadSource gamePad1Source = GamePadSource.VIRTUAL;
    private volatile GamePadSource gamePad2Source = GamePadSource.HIDDEN;
    ScheduledExecutorService gamePadExecutorService = Executors.newSingleThreadScheduledExecutor();

    VirtualGamePadController virtualGamePadController = null;
    VirtualGamePadController virtualGamePad2Controller = null;
    // The gamepad 2 panel; kept out of the layout until the user asks for it.
    HBox virtualGamePad2Box = null;
    // Divides the two panels, which are otherwise identical clusters butted together.
    Separator gamePadSeparator = null;

    //Background Image and Field
    private final Image backgroundImage = Config.BACKGROUND;
    private final PixelReader pixelReader = backgroundImage.getPixelReader();
    private double halfFieldWidth;
    private double fieldWidth;

    //Path Drawing
    Polyline pathLine;



    //OpMode Control
    private OpMode opMode = null;
    private volatile boolean opModeInitialized = false;
    private volatile boolean opModeStarted = false;
    private Thread opModeThread = null;

    //Virtual Robot Control Engine
    ScheduledExecutorService executorService = null;
    public static final double TIME_INTERVAL_MILLISECONDS = 20;

    //Random Number Generator
    private final Random random = new Random();

    //KeyState
    private final KeyState keyState = new KeyState();

    /*
     * Motor slider listener
     *
     * The values set for random error fraction and systematic error fraction may look reversed, but they aren't.
     * Systematic error fraction is set randomly for each motor when the slider is changed, but then remains the
     * same for that motor until the slider is changed again. Random error fraction is set for each motor when the
     * slider is changed, but then (in the DcMotorImpl class) gets multiplied by a new random number during each motor
     * update cycle.
     */
    private final ChangeListener<Number> sliderChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            for (DcMotor motor: hardwareMap.dcMotor) {
                if (!(motor instanceof DcMotorImpl)) continue;      //Now that DeadWheelEncoder has been added, not all "DcMotor" are "DcMotorImpl"
                ((DcMotorImpl)motor).setRandomErrorFrac(sldRandomMotorError.getValue());
                ((DcMotorImpl)motor).setSystematicErrorFrac(sldSystematicMotorError.getValue() * 2.0 * (0.5 - random.nextDouble()));
                ((DcMotorImpl)motor).setInertia(1.0 - Math.pow(10.0, -sldMotorInertia.getValue()));
            }
        }
    };

    boolean getOpModeInitialized(){ return opModeInitialized; }

    public void initialize() {
        OpMode.setVirtualRobotController(this);
        VirtualBot.setController(this);
        VirtualGameElement.setController(this);
        Game.setController(this);
        setupCbxOpModes();
        setupCbxRobotConfigs();
        fieldWidth = Config.FIELD_WIDTH;
        halfFieldWidth = fieldWidth / 2.0;
        fieldPane.setPrefWidth(fieldWidth);
        fieldPane.setPrefHeight(fieldWidth);
        fieldPane.setMinWidth(fieldWidth);
        fieldPane.setMaxWidth(fieldWidth);
        fieldPane.setMinHeight(fieldWidth);
        fieldPane.setMaxHeight(fieldWidth);
        imgViewBackground.setFitWidth(fieldWidth);
        imgViewBackground.setFitHeight(fieldWidth);
        imgViewBackground.setViewport(new Rectangle2D(0, 0, fieldWidth, fieldWidth));
        imgViewBackground.setImage(backgroundImage);
        Rectangle pathRect = new Rectangle(fieldWidth, fieldWidth);
        pathRect.setFill(Color.color(1,0,0,0));
        pathLine = new Polyline();
        pathLine.setStroke(Color.LAWNGREEN);
        pathLine.setStrokeWidth(2);
        pathLine.setVisible(false);
        fieldPane.getChildren().addAll(new Group(pathRect, pathLine));

        addConstraintMasks();

        setupPhysicsWorld();

        Config.GAME.initialize();
        Config.GAME.resetGameElements();
        Config.GAME.setHumanPlayerAuto(true);

        sldRandomMotorError.valueProperty().addListener(sliderChangeListener);
        sldSystematicMotorError.valueProperty().addListener(sliderChangeListener);
        sldMotorInertia.valueProperty().addListener(sliderChangeListener);

        try{
            FXMLLoader loader1 = new FXMLLoader(getClass().getResource("virtual_gamepad.fxml"));
            HBox hbox1 = (HBox)loader1.load();
            virtualGamePadController = loader1.getController();
            virtualGamePadController.setVirtualRobotController(this);

            /*
             * A second gamepad, for op modes that use gamepad2. It is loaded up front but
             * left out of the layout: most op modes only need gamepad1, and an unused
             * second panel takes up screen space. Its source selector adds it.
             */
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("virtual_gamepad.fxml"));
            virtualGamePad2Box = (HBox)loader2.load();
            virtualGamePad2Controller = loader2.getController();
            virtualGamePad2Controller.setVirtualRobotController(this);
            virtualGamePad2Box.setVisible(false);
            virtualGamePad2Box.setManaged(false);

            gamePadSeparator = new Separator(Orientation.VERTICAL);
            gamePadSeparator.setMaxHeight(Double.MAX_VALUE);
            gamePadSeparator.setVisible(false);
            gamePadSeparator.setManaged(false);

            hbxGamePadPanels.getChildren().addAll(hbox1, gamePadSeparator, virtualGamePad2Box);
        } catch (IOException e){
            System.out.println("Virtual GamePad UI Failed to Load");
        }

        // Only the panels themselves to begin with; physical gamepads join as they are found.
        refreshGamePadSourceItems(false, false);
        setGamePadSource(cbxGamePad2Source, GamePadSource.HIDDEN);

        gamePadHelper = new GamePadHelper();
        gamePadExecutorService.scheduleAtFixedRate(gamePadHelper, 0, 20, TimeUnit.MILLISECONDS);
    }

    /**
     *  Adjust world settings (especially gravity, but other settings may need adjustment as well).
     *  Add Bodys (with rectangular BodyFixtures) on all four sides, representing the walls.
     */
    private void setupPhysicsWorld(){
        world.setGravity(0, 0);
        world.getSettings().setContinuousDetectionMode(ContinuousDetectionMode.BULLETS_ONLY);
        world.getSettings().setBaumgarte(0.2);

        // Create Rectangles for four 1 meter thick walls
        org.dyn4j.geometry.Rectangle topRect = new org.dyn4j.geometry.Rectangle(
                VirtualField.FIELD_WIDTH_METERS + 2, 1);
        org.dyn4j.geometry.Rectangle bottomRect = new org.dyn4j.geometry.Rectangle(
                VirtualField.FIELD_WIDTH_METERS + 2, 1);
        org.dyn4j.geometry.Rectangle leftRect = new org.dyn4j.geometry.Rectangle(
                1, VirtualField.FIELD_WIDTH_METERS);
        org.dyn4j.geometry.Rectangle rightRect = new org.dyn4j.geometry.Rectangle(
                1, VirtualField.FIELD_WIDTH_METERS);

        // Translate the rectangles into correct positions
        topRect.translate(0, VirtualField.Y_MAX/VirtualField.PIXELS_PER_METER + 0.5);
        bottomRect.translate(0, VirtualField.Y_MIN/VirtualField.PIXELS_PER_METER - 0.5);
        leftRect.translate(VirtualField.X_MIN/VirtualField.PIXELS_PER_METER - 0.5, 0);
        rightRect.translate(VirtualField.X_MAX/VirtualField.PIXELS_PER_METER + 0.5, 0);

        /*
         * For each wall, create a body with infinite mass. The shape (i.e., Rectangle) for each wall is placed into
         * the body via a BodyFixture. The Fixture is assigned a Category filter which assigns it to the WALL
         * category, and allows it to collide with all categories.
         */
        Body topWall = new Body();
        BodyFixture topFixture = topWall.addFixture(topRect);
        topFixture.setFilter(Filters.WALL_FILTER);
        topWall.setMass(MassType.INFINITE);
        world.addBody(topWall);
        topWall.setUserData(new Wall());

        Body bottomWall = new Body();
        BodyFixture bottomFixture = bottomWall.addFixture(bottomRect);
        bottomFixture.setFilter(Filters.WALL_FILTER);
        bottomWall.setMass(MassType.INFINITE);
        world.addBody(bottomWall);
        bottomWall.setUserData(new Wall());

        Body leftWall = new Body();
        BodyFixture leftFixture = leftWall.addFixture(leftRect);
        leftFixture.setFilter(Filters.WALL_FILTER);
        leftWall.setMass(MassType.INFINITE);
        world.addBody(leftWall);
        leftWall.setUserData(new Wall());

        Body rightWall = new Body();
        BodyFixture rightFixture = rightWall.addFixture(rightRect);
        rightFixture.setFilter(Filters.WALL_FILTER);
        rightWall.setMass(MassType.INFINITE);
        world.addBody(rightWall);
        rightWall.setUserData(new Wall());
    }

    /**
     *  "Gray out" part of the field based on the field constraints (X_MIN_FRACTION, X_MAX_FRACTION,
     *  Y_MIN_FRACTION, Y_MAX_FRACTION values)
     */
    private void addConstraintMasks(){
        if (Config.X_MIN_FRACTION > 0){
            Rectangle rect = new Rectangle(fieldWidth*Config.X_MIN_FRACTION, fieldWidth);
            rect.setFill(Color.color(0.2, 0.2, 0.2, 0.75));
            fieldPane.getChildren().add(rect);
        }
        if (Config.X_MAX_FRACTION < 1){
            Rectangle rect = new Rectangle(fieldWidth*(1-Config.X_MAX_FRACTION), fieldWidth);
            rect.setTranslateX(fieldWidth*Config.X_MAX_FRACTION);
            rect.setFill(Color.color(0.2, 0.2, 0.2, 0.75));
            fieldPane.getChildren().add(rect);
        }
        if (Config.Y_MIN_FRACTION > 0){
            Rectangle rect = new Rectangle(fieldWidth*(Config.X_MAX_FRACTION-Config.X_MIN_FRACTION), fieldWidth*Config.Y_MIN_FRACTION);
            rect.setTranslateX(fieldWidth*Config.X_MIN_FRACTION);
            rect.setTranslateY(fieldWidth*(1-Config.Y_MIN_FRACTION));
            rect.setFill(Color.color(0.2, 0.2, 0.2, 0.75));
            fieldPane.getChildren().add(rect);
        }
        if (Config.Y_MAX_FRACTION < 1){
            Rectangle rect = new Rectangle(fieldWidth*(Config.X_MAX_FRACTION-Config.X_MIN_FRACTION), fieldWidth*(1-Config.Y_MAX_FRACTION));
            rect.setTranslateX(fieldWidth*Config.X_MIN_FRACTION);
            rect.setFill(Color.color(0.2, 0.2, 0.2, 0.75));
            fieldPane.getChildren().add(rect);
        }
    }

    private void setupCbxRobotConfigs(){
        //Reflections reflections = new Reflections(VirtualRobotApplication.class.getClassLoader());
        Reflections reflections = new Reflections("virtual_robot.robots.classes");
        Set<Class<?>> configClasses = new HashSet<>();
        configClasses.addAll(reflections.getTypesAnnotatedWith(BotConfig.class));
        ObservableList<Class<?>> validConfigClasses = FXCollections.observableArrayList();
        for (Class<?> c: configClasses){
            if (!c.getAnnotation(BotConfig.class).disabled() && VirtualBot.class.isAssignableFrom(c))
                validConfigClasses.add(c);
        }
        cbxConfig.setItems(validConfigClasses);
//        cbxConfig.setValue(MecanumBot.class);
        cbxConfig.setValue(Config.DEFAULT_BOT);

        cbxConfig.setCellFactory(new Callback<ListView<Class<?>>, ListCell<Class<?>>>() {
            @Override
            public ListCell<Class<?>> call(ListView<Class<?>> param) {
                final ListCell<Class<?>> cell = new ListCell<Class<?>>(){
                    @Override
                    protected void updateItem(Class<?> cl, boolean bln){
                        super.updateItem(cl, bln);
                        if (cl == null){
                            setText(null);
                            return;
                        }
                        Annotation a = cl.getAnnotation(BotConfig.class);
                        setText(((BotConfig)a).name());
                    }
                };
                return cell;
            }
        });

        cbxConfig.setButtonCell(new ListCell<Class<?>>(){
            @Override
            protected void updateItem(Class<?> cl, boolean bln) {
                super.updateItem(cl, bln);
                if (cl == null) {
                    setText(null);
                    return;
                }
                Annotation a = cl.getAnnotation(BotConfig.class);
                setText(((BotConfig) a).name());
            }
        });
    }


    public VirtualBot getVirtualBotInstance(Class<?> c){
        try {
            Annotation a = c.getAnnotation(BotConfig.class);
            /*
             * Publish the selected configuration's name (e.g. "Mecanum Bot") so that the
             * emulated Robot Controller settings can report it. This is what lets team code
             * calling ConfigUtilities.getRobotConfigurationName() get a real answer here,
             * exactly as it would on a robot. See android.preference.PreferenceManager.
             */
            System.setProperty(android.preference.PreferenceManager.CONFIGURATION_NAME_PROPERTY,
                    ((BotConfig) a).name());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/virtual_robot/robots/fxml/" + ((BotConfig) a).filename() + ".fxml"));
            Group group = (Group) loader.load();
            VirtualBot bot = (VirtualBot) loader.getController();
            bot.setUpDisplayGroup(group);
            return bot;
        } catch (Exception e){
            System.out.println("Unable to load robot configuration.");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    private String getNameFromAnnotationOrOpmode(Class<?> c){
        String name = "";
        Annotation a1 = c.getAnnotation(TeleOp.class);
        if(a1 != null){
            name = ((TeleOp)a1).name();
        }else{
            a1 = c.getAnnotation(Autonomous.class);
            if(a1 != null){
                name = ((Autonomous)a1).name();
            }
        }
        if(name.isEmpty()){
            name = c.getSimpleName();
        }
        return name;
    }

    private String getGroupFromAnnotationOrOpmode(Class<?> c){
        String group = null;
        Annotation a1 = c.getAnnotation(TeleOp.class);
        if(a1 != null){
            group = ((TeleOp)a1).group();
        }else{
            a1 = c.getAnnotation(Autonomous.class);
            if(a1 != null){
                group = ((Autonomous)a1).group();
            }
        }
        return group;
    }

    private void setupCbxOpModes(){
        Reflections reflections = new Reflections("");
//        Reflections reflections = new Reflections("org.firstinspires.ftc.teamcode");
        Set<Class<?>> opModes = new HashSet<>();
        opModes.addAll(reflections.getTypesAnnotatedWith(TeleOp.class));
        opModes.addAll(reflections.getTypesAnnotatedWith(Autonomous.class));//Lists of OpMode classes and OpMode Names
        ObservableList<Class<?>> nonDisabledOpModeClasses = FXCollections.observableArrayList();
        for (Class<?> c : opModes){
            if (c.getAnnotation(Disabled.class) == null && OpMode.class.isAssignableFrom(c)){
                nonDisabledOpModeClasses.add(c);
            }
        }

        nonDisabledOpModeClasses.sort(new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                String group1 = getGroupFromAnnotationOrOpmode(o1);
                String group2 = getGroupFromAnnotationOrOpmode(o2);

                if (group1 == null) return -1;
                else if (group2 == null) return 1;
                else return group1.compareToIgnoreCase(group2);
            }
        });

        cbxOpModes.setItems(nonDisabledOpModeClasses);

        cbxOpModes.setCellFactory(new Callback<ListView<Class<?>>, ListCell<Class<?>>>() {
            @Override
            public ListCell<Class<?>> call(ListView<Class<?>> param) {
                final ListCell<Class<?>> cell = new ListCell<Class<?>>(){
                    @Override
                    protected void updateItem(Class<?> cl, boolean bln){
                        super.updateItem(cl, bln);
                        if (cl == null){
                            setText(null);
                            return;
                        }
                        String group = getGroupFromAnnotationOrOpmode(cl);
                        String name = getNameFromAnnotationOrOpmode(cl);

                        if(group.isEmpty()) {
                            setText(name);
                        }else{
                            setText(group + ": " + name);
                        }
                    }
                };
                return cell;
            }
        });

        cbxOpModes.setButtonCell(new ListCell<Class<?>>(){
            @Override
            protected void updateItem(Class<?> cl, boolean bln) {
                super.updateItem(cl, bln);
                if (cl == null) {
                    setText(null);
                    return;
                }
                setText(getNameFromAnnotationOrOpmode(cl));
            }
        });

        /*
         * A project with no OpModes yet is a normal starting state, not an error - so
         * open with an empty selection rather than crashing on the first entry.
         */
        if (!cbxOpModes.getItems().isEmpty()) {
            cbxOpModes.setValue(cbxOpModes.getItems().get(0));
        } else {
            cbxOpModes.setPromptText("No OpModes found");
        }
    }


    @FXML
    public void setConfig(ActionEvent event){
        if (opModeInitialized || opModeStarted) return;
        if (bot != null) {
            bot.removeFromWorld();
            bot.removeFromDisplay(fieldPane);
        }
        CommonOdometry.internalReset();
        bot = getVirtualBotInstance(cbxConfig.getValue());
        if (bot == null) System.out.println("Unable to get VirtualBot Object");
        hardwareMap = bot.getHardwareMap();
        initializeTelemetryTextArea();
        sldRandomMotorError.setValue(0.0);
        sldSystematicMotorError.setValue(0.0);
        sldMotorInertia.setValue(0.0);
    }


    public Pane getFieldPane(){ return fieldPane; }

    public World<Body> getWorld(){ return world; }

    @FXML
    private void handleDriverButtonAction(ActionEvent event){
        if (!opModeInitialized){
            /*
             * INIT has been pressed.
             */
            if (!initOpMode()) return;
            pathLine.getPoints().clear();
            txtTelemetry.setText("");
            driverButton.setText("START");
            opModeInitialized = true;
            cbxConfig.setDisable(true);
            Runnable runOpMode = new Runnable() {
                @Override
                public void run() {
                    runOpModeAndCleanUp();
                }
            };

            opModeThread = new Thread(runOpMode);
            opModeThread.setDaemon(true);

            Runnable updateDisplay = new Runnable() {
                @Override
                public void run() {
                    Config.GAME.updateDisplay();
                    bot.updateDisplay();
                    pathLine.getPoints().addAll(halfFieldWidth + bot.getX(), halfFieldWidth - bot.getY());
                }
            };

            Runnable singleCycle = new Runnable() {
                @Override
                public void run() {
                    singlePhysicsCycle();
                    Platform.runLater(updateDisplay);
                }
            };
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(singleCycle, 0, (long) TIME_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);

            opModeThread.start();
        } else if (!opModeStarted){
            /*
             * START has been pressed.
             */
            driverButton.setText("STOP");
            opModeStarted = true;
        } else{
            /*
             * STOP has been pressed. Note that it is not possible for this to happen before START is pressed.
             */
            driverButton.setText("INIT");
            opModeInitialized = false;
            /*
             * Setting opModeStarted to false will:
             *   -Cause the final loop in runOpModeAndCleanUp to exit;
             *   -Cause opmode.Stop() to run
             *   -In a linear opmode, the above will cause stopRequested to become true, and interrupt runOpMode thread
             */
            opModeStarted = false;
            if (!executorService.isShutdown()) executorService.shutdown();
            /*
             * This should not be necessary, but...
             */
            try{
                opModeThread.join(500);
            } catch(InterruptedException exc) {
                opModeThread.interrupt();
            }
            if (opModeThread.isAlive()) System.out.println("OpMode Thread Failed to Terminate.");

            bot.getHardwareMap().setActive(false);
            bot.powerDownAndReset();
            Config.GAME.stopGameElements();
            gamePadHelper.onOpModeFinished();
            initializeTelemetryTextArea();
            cbxConfig.setDisable(false);
        }
    }

    private void runOpModeAndCleanUp(){
        Platform.runLater(()->lblRunTime.setText("0.00"));

        try {

            //Activate the hardware map, so that calls to "get" on the hardware map itself, and on dcMotor, etc,
            //will return hardware objects
            bot.getHardwareMap().setActive(true);

            //For regular opMode, run user-defined init() method. For Linear opMode, init() starts the execution of
            //runOpMode on a helper thread.
            opMode.init();

            while (!opModeStarted && !Thread.currentThread().isInterrupted()) {
                // to keep the guarantee that this is updated
                opMode.time = opMode.getRuntime();
                //For regular opMode, run user-defined init_loop() method. For Linear opMode, init_loop checks whether
                //runOpMode has exited; if so, it interrupts the opModeThread.
                opMode.init_loop();
                //For regular op mode, update telemetry after each iteration of init_loop()
                //For linear op mode, do-nothing
                opMode.internalPostInitLoop();

                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

            //For regular opMode, run user-defined start() method, if any. For Linear opMode, the start() method
            //will allow waitForStart() to finish executing.
            if (!Thread.currentThread().isInterrupted()) opMode.start();

            long opModeStartNanos = System.nanoTime();

            while (opModeStarted && !Thread.currentThread().isInterrupted()) {

                try {
                    Thread.sleep(20, 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Update the run time display
                final long opModeRunNanos = System.nanoTime() - opModeStartNanos;
                Platform.runLater(()->lblRunTime.setText(String.format("%.2f", opModeRunNanos/1000000000.0)));

                // to keep the guarantee that this is updated
                opMode.time = opMode.getRuntime();

                //For regular opMode, run user-defined loop() method. For Linear opMode, loop() checks whether
                //runOpMode has exited; if so, it interrupts the opModeThread.
                opMode.loop();

                //For regular op mode only, update telemetry after each execution of loop()
                //For linear op mode, do-nothing
                opMode.internalPostLoop();

                try {
                    Thread.sleep(0, 1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }

            //For regular opMode, run user-defined stop() method, if any. For Linear opMode, shut down the
            //helper thread that runs runOpMode.
            opMode.stop();
        } catch(Exception e){
            System.out.println("Exception thrown by opModeThread.");
            System.out.println(e.getClass().getName());
            System.out.println(e.getLocalizedMessage());
            System.out.println("Stack Trace:");
            for (StackTraceElement stackTraceElement: e.getStackTrace()){
                System.out.println("  " + stackTraceElement.toString());
            }
            System.out.println();
        }

        bot.getHardwareMap().setActive(false);
        bot.powerDownAndReset();
        Config.GAME.stopGameElements();
        if (!executorService.isShutdown()) executorService.shutdown();
        opModeInitialized = false;
        opModeStarted = false;
        gamePadHelper.onOpModeFinished();
        Platform.runLater(new Runnable() {
            public void run() {
                driverButton.setText("INIT");
                //resetGamePad();
                initializeTelemetryTextArea();
                cbxConfig.setDisable(false);
            }
        });

        System.out.println("Finished executing runOpModeAndCleanUp() on opModeThread.");
    }

    private void singlePhysicsCycle(){
        // Update the physics engine. This will also call any collision/contact listeners that have been set.
        // These listeners will generally be in the bot class. They should record events within fields in the bot's
        // class, to be handled later in the bot.updateStateAndSensors call.
        world.updatev(TIME_INTERVAL_MILLISECONDS / 1000.0);

        // Update game element pose, and any other relevant state, of all game elements
        Config.GAME.updateGameElementState(TIME_INTERVAL_MILLISECONDS);

        // Update robot's pose by obtaining it from the physics engine, accumulate forces on Body based on
        // drive motor status, and update robot sensors. Depending on collision/contact events that occurred
        // during the world.updatev() call, it is also possible that this method will directly affect game elements.
        bot.updateStateAndSensors(TIME_INTERVAL_MILLISECONDS);


        if (Config.GAME.hasHumanPlayer() && Config.GAME.isHumanPlayerAuto() && opModeStarted
                || Config.GAME.isHumanPlayerActionRequested() && opModeInitialized) {
            Config.GAME.updateHumanPlayerState(TIME_INTERVAL_MILLISECONDS);
        }
    }

    @FXML
    private void handleFieldMouseClick(MouseEvent arg){
        if (opModeInitialized || opModeStarted) return;
        bot.positionWithMouseClick(arg);
    }


    private boolean initOpMode() {
        Class<?> selected = cbxOpModes.getValue();
        if (selected == null) {
            System.out.println("No OpMode selected. Write one in org.firstinspires.ftc.teamcode "
                    + "and annotate it @TeleOp or @Autonomous.");
            return false;
        }
        try {
            Class<?> opModeClass = selected;
            opMode = (OpMode) opModeClass.getDeclaredConstructor().newInstance();
        } catch (Exception exc){
            /*
             * An OpMode whose constructor throws arrives here wrapped in InvocationTargetException.
             * Report the cause rather than the wrapper: that is the OpMode's own error, and it is
             * what the user needs to see. Otherwise the simulator just refuses to INIT in silence.
             */
            Throwable cause = exc;
            if (exc instanceof InvocationTargetException && exc.getCause() != null) {
                cause = exc.getCause();
            }
            System.out.println("Unable to initialize OpMode " + selected.getName() + ".");
            System.out.println(cause.getMessage());
            cause.printStackTrace();
            return false;
        }
        return true;
    }

    @FXML
    private void handleCbxShowPathAction(ActionEvent event){
        if (pathLine == null) return;
        pathLine.setVisible(cbxShowPath.isSelected());
    }

    /**
     * Show or hide the second virtual gamepad, following its source selector. While it is
     * hidden, gamepad2 reports nothing pressed, so hiding it cannot leave a stale button held
     * down.
     */
    private void setGamePad2PanelVisible(boolean show){
        if (virtualGamePad2Box == null) return;
        if (virtualGamePad2Box.isVisible() == show) return;
        virtualGamePad2Box.setVisible(show);
        virtualGamePad2Box.setManaged(show);
        if (gamePadSeparator != null) {
            gamePadSeparator.setVisible(show);
            gamePadSeparator.setManaged(show);
        }
        if (!show && virtualGamePad2Controller != null) virtualGamePad2Controller.resetGamePad();
        /*
         * The window is not resizable and was sized to its contents when it opened, so it
         * will not grow or shrink on its own. Without this the panel would be revealed in
         * the layout but left outside the window.
         */
        Window window = borderPane.getScene() == null ? null : borderPane.getScene().getWindow();
        if (window instanceof Stage) ((Stage)window).sizeToScene();
    }

    /**
     * What drives a virtual gamepad: its own on-screen panel under the mouse, or one of the
     * physical gamepads. Either way the op mode reads the panel - a physical gamepad works by
     * moving that panel's controls.
     *
     * HIDDEN is offered for gamepad2 only. Most op modes use gamepad1 alone, and a second panel
     * always on screen costs space; while hidden, gamepad2 reports nothing pressed.
     */
    public enum GamePadSource {
        HIDDEN("Hide", -1),
        VIRTUAL("Virtual gamepad", -1),
        PHYSICAL_A("Physical gamepad A", 0),
        PHYSICAL_B("Physical gamepad B", 1);

        private final String label;
        /** The physical gamepad this names, as an index into the jamepad slots, or -1 for none. */
        final int slot;

        GamePadSource(String label, int slot){ this.label = label; this.slot = slot; }

        static GamePadSource forSlot(int slot){ return slot == 0? PHYSICAL_A : PHYSICAL_B; }

        @Override
        public String toString(){ return label; }
    }

    // Guards against the selection changes made below re-entering the handler.
    private boolean updatingGamePadSource = false;

    @FXML
    private void handleGamePadSourceSelection(ActionEvent event){
        if (updatingGamePadSource) return;
        ComboBox<GamePadSource> cbx = event.getSource() == cbxGamePad1Source? cbxGamePad1Source : cbxGamePad2Source;
        setGamePadSource(cbx, cbx.getValue());
    }

    /**
     * Choose what drives one of the virtual gamepads. A physical gamepad can only drive one of
     * them, so claiming it hands the other back to its own panel.
     *
     * Must run on the FX thread. Both ways of choosing - the dropdown and the Start+A / Start+B
     * gesture on the gamepad itself - come through here, so they cannot disagree.
     */
    private void setGamePadSource(ComboBox<GamePadSource> cbx, GamePadSource source){
        if (source == null) return;
        ComboBox<GamePadSource> other = cbx == cbxGamePad1Source? cbxGamePad2Source : cbxGamePad1Source;

        updatingGamePadSource = true;
        cbx.getSelectionModel().select(source);
        // Only a physical gamepad is exclusive; both panels can be virtual, and Hide is gamepad2's alone.
        if (source.slot >= 0 && other.getValue() == source) {
            other.getSelectionModel().select(GamePadSource.VIRTUAL);
        }
        updatingGamePadSource = false;

        gamePad1Source = cbxGamePad1Source.getValue();
        gamePad2Source = cbxGamePad2Source.getValue();

        // Gamepad 2's panel is on screen unless its selector says to hide it.
        if (cbx == cbxGamePad2Source) setGamePad2PanelVisible(source != GamePadSource.HIDDEN);
    }

    /**
     * Offer only the sources that exist: a gamepad's own panel always, Hide for gamepad2, and
     * each physical gamepad while it is plugged in. A selection whose gamepad has just been
     * unplugged falls back to the panel, which leaves it on screen under the mouse rather than
     * yanking it out of the window. Must run on the FX thread.
     */
    private void refreshGamePadSourceItems(boolean aConnected, boolean bConnected){
        updatingGamePadSource = true;
        selectFrom(cbxGamePad1Source, availableGamePadSources(false, aConnected, bConnected));
        selectFrom(cbxGamePad2Source, availableGamePadSources(true, aConnected, bConnected));
        updatingGamePadSource = false;

        gamePad1Source = cbxGamePad1Source.getValue();
        gamePad2Source = cbxGamePad2Source.getValue();
    }

    private static ObservableList<GamePadSource> availableGamePadSources(
            boolean canHide, boolean aConnected, boolean bConnected){
        ObservableList<GamePadSource> available = FXCollections.observableArrayList();
        if (canHide) available.add(GamePadSource.HIDDEN);
        available.add(GamePadSource.VIRTUAL);
        if (aConnected) available.add(GamePadSource.PHYSICAL_A);
        if (bConnected) available.add(GamePadSource.PHYSICAL_B);
        return available;
    }

    private static void selectFrom(ComboBox<GamePadSource> cbx, ObservableList<GamePadSource> available){
        GamePadSource current = cbx.getValue();
        cbx.setItems(available);
        cbx.getSelectionModel().select(available.contains(current)? current : GamePadSource.VIRTUAL);
    }

    public void updateTelemetryDisplay(String telemetryText) {
        txtTelemetry.setText(telemetryText);
    }
		
    @FXML
    private void handleCheckBoxAutoHumanAction(ActionEvent event){
        Config.GAME.setHumanPlayerAuto(checkBoxAutoHuman.isSelected());
    }

    @FXML
    private void handleBtnHumanAction(ActionEvent event){
        if (opModeInitialized) Config.GAME.requestHumanPlayerAction();
    }

    @FXML
    private void handleBtnResetGameElements(ActionEvent event){
        if (opModeInitialized) return;
        if (bot instanceof ControlsElements) ((ControlsElements) bot).clearLoadedElements(Config.GAME);
        Config.GAME.resetGameElements();
    }

    @FXML
    private void handleBtnPreloadElementsOnBot(ActionEvent event){
        if (!opModeInitialized && bot instanceof ControlsElements){
            ((ControlsElements) bot).preloadElements(Config.GAME);
        }
    }


    private void initializeTelemetryTextArea(){
        StringBuilder sb = new StringBuilder();
        sb.append("Left-click to position bot.");
        sb.append("\nRight-click to orient bot.");
        sb.append("\n\nCONFIG");
        Set<String> motors = hardwareMap.dcMotor.keySet();
        if (!motors.isEmpty()) {
            sb.append("\n Motors:");
            for (String motor : motors) sb.append("\n   " + motor);
        }
        Set<String> servos = hardwareMap.servo.keySet();
        if (!servos.isEmpty()) {
            sb.append("\n Servos:");
            for (String servo : servos) sb.append("\n   " + servo);
        }
        Set<String> crservos = hardwareMap.crservo.keySet();
        if (!crservos.isEmpty()){
            sb.append("\n CR Servos:");
            for (String crservo : crservos) sb.append("\n   " + crservo);
        }
        Set<String> colorSensors = hardwareMap.colorSensor.keySet();
        if (!colorSensors.isEmpty()) {
            sb.append("\n Color Sensors:");
            for (String colorSensor : colorSensors) sb.append("\n   " + colorSensor);
        }
        Set<String> gyroSensors = hardwareMap.gyroSensor.keySet();
        if (!gyroSensors.isEmpty()) {
            sb.append("\n Gyro Sensors:");
            for (String gyroSensor : gyroSensors) sb.append("\n   " + gyroSensor);
        }
        Set<String> bno055IMUs = hardwareMap.keySet(BNO055IMU.class);
        if (!bno055IMUs.isEmpty()){
            sb.append("\n BNO055IMU Sensors:");
            for (String imuSensor : bno055IMUs) sb.append("\n   " + imuSensor);
        }
        Set<String> distanceSensors = hardwareMap.keySet(DistanceSensor.class);
        if (!distanceSensors.isEmpty()) {
            sb.append("\n Distance Sensors:");
            for (String distance : distanceSensors) sb.append("\n   " + distance);
        }
        Set<String> digitalChannels = hardwareMap.keySet(DigitalChannel.class);
        if (!digitalChannels.isEmpty()) {
            sb.append("\n Digital Sensors:");
            for (String digitalChannel : digitalChannels) sb.append("\n   " + digitalChannel);
        }
        Set<String> analogInputs = hardwareMap.keySet(AnalogInput.class);
        if (!analogInputs.isEmpty()) {
            sb.append("\n Analog Sensors:");
            for (String analogInput : analogInputs) sb.append("\n   " + analogInput);
        }
        txtTelemetry.setText(sb.toString());
    }

    @FXML
    private void handleKeyEvents(KeyEvent e){
        if (e.getEventType() == KeyEvent.KEY_PRESSED){
            keyState.set(e.getCode(), true);
        } else if (e.getEventType() == KeyEvent.KEY_RELEASED){
            keyState.set(e.getCode(), false);
        }
    }

    public boolean getKeyState(KeyCode code){
        return keyState.get(code);
    }

    public class ColorSensorImpl implements ColorSensor, ColorRangeSensor {
        private int red = 0;
        private int green = 0;
        private int blue = 0;
        private int alpha = 0;
        private double gain = 1.0;
        private double distanceCM = 0.0;

        public synchronized int red() {
            return red;
        }

        public synchronized int green() {
            return green;
        }

        public synchronized int blue() {
            return blue;
        }

        public synchronized int alpha() {
            return alpha;
        }

        public synchronized void updateColor(double x, double y) {
            int colorX = (int) (x + halfFieldWidth);
            int colorY = (int) (halfFieldWidth - y);
            double tempRed = 0.0;
            double tempGreen = 0.0;
            double tempBlue = 0.0;
            for (int row = colorY-4; row < colorY+5; row++)
                for (int col = colorX - 4; col < colorX+5; col++){
                    Color c = pixelReader.getColor(col, row);
                    tempRed += c.getRed();
                    tempGreen += c.getGreen();
                    tempBlue += c.getBlue();
                }
            tempRed = Math.floor( tempRed * 256.0 / 81.0 );
            if (tempRed == 256) tempRed = 255;
            tempGreen = Math.floor(tempGreen * 256.0 / 81.0);
            if (tempGreen == 256) tempGreen = 255;
            tempBlue = Math.floor(tempBlue * 256.0 / 81.0);
            if (tempBlue == 256) tempBlue = 255;
            red = (int) tempRed;
            green = (int) tempGreen;
            blue = (int) tempBlue;
            alpha = Math.max(red, Math.max(green, blue));
        }

        public synchronized void setDistance(double distance, DistanceUnit distanceUnit) {
            distanceCM = distanceUnit.toCm(distance);
        }

        @Override
        public synchronized double getDistance(DistanceUnit distanceUnit) {
            return distanceUnit.fromCm(distanceCM);
        }

        @Override
        public double getLightDetected() {
            return 0;
        }

        @Override
        public double getRawLightDetected() {
            return 0;
        }

        @Override
        public double getRawLightDetectedMax() {
            return 1.0;
        }

        @Override
        public void enableLed(boolean enable) {

        }

        @Override
        public String status() {
            return String.format(Locale.getDefault(), "%s on %s", getDeviceName(), getConnectionInfo());
        }

        @Override
        public synchronized NormalizedRGBA getNormalizedColors() {
            NormalizedRGBA nRGBA = new NormalizedRGBA();
            nRGBA.red = red / 256.0f;
            nRGBA.green = green / 256.0f;
            nRGBA.blue = blue / 256.0f;
            nRGBA.alpha = alpha / 256.0f;
            return nRGBA;
        }

        @Override
        public synchronized float getGain() {
            return (float) gain;
        }

        @Override
        public synchronized void setGain(float newGain) {
            gain = newGain;
        }
    }

    public class DistanceSensorImpl implements DistanceSensor {

        private final double readingWhenOutOfRangeMM = 8200;
        private double distanceMM = readingWhenOutOfRangeMM;
        private static final double MIN_DISTANCE = 50; //mm
        private static final double MAX_DISTANCE = 1000; //mm
        private static final double MAX_OFFSET = 7.0 * Math.PI / 180.0;

        private final double X_MIN, X_MAX, Y_MIN, Y_MAX;    //Need these to constrain field

        public DistanceSensorImpl(){
            X_MIN = 2.0 * (Config.X_MIN_FRACTION - 0.5) * halfFieldWidth;
            X_MAX = 2.0 * (Config.X_MAX_FRACTION - 0.5) * halfFieldWidth;
            Y_MIN = 2.0 * (Config.Y_MIN_FRACTION - 0.5) * halfFieldWidth;
            Y_MAX = 2.0 * (Config.Y_MAX_FRACTION - 0.5) * halfFieldWidth;
        }

        public synchronized double getDistance(DistanceUnit distanceUnit){
            double result;
            if (distanceMM < MIN_DISTANCE) result = MIN_DISTANCE - 1.0;
            else if (distanceMM > MAX_DISTANCE) result = readingWhenOutOfRangeMM;
            else result = distanceMM;
            return distanceUnit.fromMm(result);
        }

        public synchronized void updateDistance(double x, double y, double headingRadians){
            final double mmPerPixel = 144.0 * 25.4 / fieldWidth;
            final double piOver2 = Math.PI / 2.0;
            double temp = headingRadians / piOver2;
            int side = (int)Math.round(temp); //-2, -1 ,0, 1, or 2 (2 and -2 both refer to the bottom)
            double offset = Math.abs(headingRadians - (side * Math.PI / 2.0));
            if (offset > MAX_OFFSET) distanceMM = readingWhenOutOfRangeMM;
            else switch (side){
                case 2:
                case -2:
                    distanceMM = (y - Y_MIN) * mmPerPixel;                  //BOTTOM
                    break;
                case -1:
                    distanceMM = (X_MAX - x) * mmPerPixel;         //RIGHT
                    break;
                case 0:
                    distanceMM = (Y_MAX - y) * mmPerPixel;         //TOP
                    break;
                case 1:
                    distanceMM = (x - X_MIN) * mmPerPixel;         //LEFT
                    break;
            }
        }

    }


    /**
     * Base class for OpMode.
     */
    public class OpModeBase {
        /*
         * Deliberately NOT final, matching the real FTC SDK, where these are plain mutable
         * fields on OpMode. Library code assigns them to hand an OpMode its context - e.g.
         * PedroPathing's SelectableOpMode injects them into the OpMode the user picks from
         * its menu. Making them final produces an IllegalAccessError when it does so.
         */
        public HardwareMap hardwareMap;
        public Gamepad gamepad1;
        public Gamepad gamepad2;
        public Telemetry telemetry;

        public OpModeBase() {
            hardwareMap = VirtualRobotController.this.hardwareMap;
            gamepad1 = gamePad1;
            this.gamepad2 = gamePad2;
            telemetry = new TelemetryImpl(VirtualRobotController.this);
        }
    }


    /**
     * Polls the physical gamepads, if any, into the virtual gamepad panels, then reads the panels into
     * gamepad1 and gamepad2.
     *
     * The panels are the only thing an op mode ever sees. A physical gamepad driving a panel moves
     * that panel's controls, and what the op mode reads is the panel - so real input is not just
     * feedback on screen, it is the input.
     */
    public class GamePadHelper implements Runnable {

        // Null if SDL could not be initialized; the panels are then mouse-only.
        private ControllerManager controllerManager = null;

        private final boolean[] isConnected = { false, false };

        private final Thread[] rumbleThreads = new Thread[2];

        // True while an assignment gesture is still being held, so it assigns once per press.
        private boolean assigning = false;

        public GamePadHelper(){
            try {
                ControllerManager manager = new ControllerManager(2);
                manager.initSDLGamepad();
                controllerManager = manager;
            } catch (Throwable t) {
                /*
                 * initSDLGamepad throws IllegalStateException, and loading the native library can
                 * fail outright. Neither is fatal: without physical gamepads the panels still work
                 * under the mouse, so just say so and carry on.
                 */
                System.out.println("Physical gamepads unavailable (" + t + "). Virtual gamepads only.");
            }
        }

        public void run(){
            if (virtualGamePadController == null) return;

            ControllerState[] states = null;
            if (controllerManager != null) {
                states = new ControllerState[]{ controllerManager.getState(0), controllerManager.getState(1) };
                updateConnectionState(states);
                checkAssignmentGesture(states);
            }

            int slot1 = slotDriving(gamePad1Source, states);
            int slot2 = slotDriving(gamePad2Source, states);

            applyToPanel(virtualGamePadController, slot1, states);
            applyToPanel(virtualGamePad2Controller, slot2, states);

            gamePad1.update(virtualGamePadController.getState());

            // gamepad2 only reports input while its panel is showing.
            if (virtualGamePad2Controller != null && gamePad2Source != GamePadSource.HIDDEN) {
                gamePad2.update(virtualGamePad2Controller.getState());
            } else {
                gamePad2.resetValues();
            }

            setOutputs(gamePad1, virtualGamePadController, slot1);
            setOutputs(gamePad2, virtualGamePad2Controller, slot2);
        }

        /**
         * The jamepad slot behind the given source, or -1 when the panel is its own source (or the
         * physical gamepad it names has gone away).
         */
        private int slotDriving(GamePadSource source, ControllerState[] states){
            if (states == null || source == null || source.slot < 0) return -1;
            return states[source.slot].isConnected? source.slot : -1;
        }

        private void applyToPanel(VirtualGamePadController panel, int slot, ControllerState[] states){
            if (panel == null) return;
            panel.setPhysicalGamePadAttached(slot >= 0);
            if (slot >= 0) panel.applyPhysicalState(states[slot]);
        }

        /**
         * Start+A on a physical gamepad hands it gamepad1 and Start+B hands it gamepad2, the
         * same gesture the Driver Station uses, so a driver can claim a gamepad without reaching
         * for the mouse. Edge triggered: holding the combination assigns once.
         */
        private void checkAssignmentGesture(ControllerState[] states){
            boolean held = false;
            for (int i = 0; i < 2; i++) {
                if (states[i].isConnected && states[i].start && (states[i].a || states[i].b)) held = true;
            }
            if (!held) {
                assigning = false;
                return;
            }
            if (assigning) return;
            assigning = true;

            for (int i = 0; i < 2; i++) {
                if (!states[i].isConnected || !states[i].start) continue;
                if (states[i].a) assignLater(cbxGamePad1Source, GamePadSource.forSlot(i));
                else if (states[i].b) assignLater(cbxGamePad2Source, GamePadSource.forSlot(i));
            }
        }

        private void assignLater(final ComboBox<GamePadSource> cbx, final GamePadSource source){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setGamePadSource(cbx, source);
                }
            });
        }

        /**
         * A physical gamepad can only be chosen while it is plugged in, so the selectors follow
         * the hardware: a gamepad is offered when it appears and withdrawn when it goes away.
         */
        private void updateConnectionState(ControllerState[] states){
            boolean changed = false;
            for (int i = 0; i < 2; i++) {
                if (states[i].isConnected != isConnected[i]) {
                    isConnected[i] = states[i].isConnected;
                    changed = true;
                }
            }
            if (!changed) return;

            System.out.println("Physical gamepads connected: A = " + isConnected[0] + ", B = " + isConnected[1]);

            final boolean aConnected = isConnected[0];
            final boolean bConnected = isConnected[1];
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    refreshGamePadSourceItems(aConnected, bConnected);
                }
            });
        }

        /**
         * Deliver an op mode's LED and rumble effects. The queues are drained here, once, so that
         * both the panel and a physical gamepad driving it get the same effect.
         */
        private void setOutputs(Gamepad gamepad, VirtualGamePadController panel, int slot){
            Gamepad.LedEffect leds = gamepad.ledQueue.poll();
            Gamepad.RumbleEffect rumbles = gamepad.rumbleQueue.poll();
            if (panel != null) {
                panel.applyLedEffect(leds);
                panel.applyRumbleEffect(rumbles);
            }
            // A physical gamepad has no LEDs to speak of here, but it can rumble for real.
            if (rumbles != null && slot >= 0 && controllerManager != null) startRumble(slot, rumbles);
        }

        private void startRumble(final int slot, Gamepad.RumbleEffect rumbles){
            final ControllerIndex controllerIndex = controllerManager.getControllerIndex(slot);
            if (rumbleThreads[slot] != null){
                rumbleThreads[slot].interrupt();
            }

            // Make this final so accessable from rumble thread
            final ListIterator<Gamepad.RumbleEffect.Step> stepIterator = rumbles.steps.listIterator();

            rumbleThreads[slot] = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (stepIterator.hasNext()) {
                        Gamepad.RumbleEffect.Step step = stepIterator.next();
                        float leftMagnitude = (float)Range.scale((double)step.large, 0, 255, 0, 1);
                        float rightMagnitude = (float)Range.scale((double)step.small, 0, 255, 0, 1);

                        try {
                            controllerIndex.doVibration(leftMagnitude, rightMagnitude, 300000);
                        } catch (ControllerUnpluggedException ex) {
                            return;
                        }
                        if (step.duration == -1) {
                            return;
                        }
                        try {
                            Thread.sleep(step.duration);
                        } catch (InterruptedException e) {
                            return;  // don't know why it was interrupted, but lets just bail on this sequence
                        }
                    }

                    try{
                        controllerIndex.doVibration(0, 0, 300000);
                    } catch (ControllerUnpluggedException ex) {
                        return;
                    }

                }
            });

            rumbleThreads[slot].start();
        }

        /**
         * Make sure that LED and Rumble threads are interrupted if the user closes the application
         * while an op mode is running.
         */
        public void quit(){
            if (virtualGamePadController != null) virtualGamePadController.interruptLEDandRumbleThreads();
            if (virtualGamePad2Controller != null) virtualGamePad2Controller.interruptLEDandRumbleThreads();
            interruptRumbleThreads();
            if (controllerManager != null) controllerManager.quitSDLGamepad();
        }

        public void onOpModeFinished(){
            if (virtualGamePadController != null) virtualGamePadController.resetGamePad();
            if (virtualGamePad2Controller != null) virtualGamePad2Controller.resetGamePad();
            interruptRumbleThreads();
        }

        private void interruptRumbleThreads(){
            for (int i = 0; i < rumbleThreads.length; i++) {
                if (rumbleThreads[i] != null) rumbleThreads[i].interrupt();
            }
        }

    }

}
