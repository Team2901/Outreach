package virtual_robot.controller;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * For internal use only. Main class for the JavaFX application.
 */
public class VirtualRobotApplication extends Application {

    private static VirtualRobotController controllerHandle;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("virtual_robot.fxml"));
        BorderPane root = (BorderPane)loader.load();
        controllerHandle = loader.getController();
        primaryStage.setTitle("Virtual Robot");
        /*
         * The UI cannot reflow: the field is a fixed number of pixels, and the virtual
         * gamepad's joysticks are pinned at 120x120 because their drag maths depends on it.
         * So rather than fight the layout, scale the whole thing to fit whatever size the
         * window is dragged to. A Group does not resize its child, so the BorderPane keeps
         * its natural size and the Scale below does the work; the StackPane centres it.
         *
         * Mouse events are transformed back into unscaled local coordinates by JavaFX, so
         * clicking the field and dragging the joysticks keep working at any scale.
         */
        Group scaleTarget = new Group(root);
        StackPane container = new StackPane(scaleTarget);
        Scene scene = new Scene(container);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                controllerHandle.setConfig(null);
            }
        });
        primaryStage.show();

        // Natural size = what the layout asked for at 100%, measured once the window exists.
        final double naturalWidth = root.getWidth();
        final double naturalHeight = root.getHeight();

        Scale scale = new Scale(1.0, 1.0);
        scale.setPivotX(0);
        scale.setPivotY(0);
        root.getTransforms().add(scale);

        Runnable rescale = () -> {
            // Uniform, so the field stays square and the robots stay round.
            double factor = Math.min(container.getWidth() / naturalWidth,
                                     container.getHeight() / naturalHeight);
            scale.setX(factor);
            scale.setY(factor);
        };
        container.widthProperty().addListener((obs, was, now) -> rescale.run());
        container.heightProperty().addListener((obs, was, now) -> rescale.run());

        // Don't let it be dragged so small that the controls stop being usable.
        double chromeWidth = primaryStage.getWidth() - scene.getWidth();
        double chromeHeight = primaryStage.getHeight() - scene.getHeight();
        primaryStage.setMinWidth(naturalWidth * 0.5 + chromeWidth);
        primaryStage.setMinHeight(naturalHeight * 0.5 + chromeHeight);
    }

    @Override
    public void stop() {
        if (controllerHandle.executorService != null && !controllerHandle.executorService.isShutdown()) {
            controllerHandle.executorService.shutdownNow();
        }
        if (controllerHandle.gamePadExecutorService != null && !controllerHandle.gamePadExecutorService.isShutdown()) {
            controllerHandle.gamePadExecutorService.shutdownNow();
        }
        controllerHandle.gamePadHelper.quit();
    }

    public static VirtualRobotController getControllerHandle(){return controllerHandle;}


    public static void main(String[] args) {
        launch(args);
    }
}
