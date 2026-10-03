import controller.MainController;
import util.ThemeManager;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class for Tree Object Editor.
 * Configures the primary stage, top header bar, theme switching, and global
 * shortcuts.
 *
 * <p>
 * Grove opens with a {@link SplashScreen} that demonstrates
 * animation and interpolation concepts from FK §2.7–2.12 before handing
 * control to the main editor window.
 * </p>
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // ── Show the splash screen first; launch the main window in its callback.
        // SplashScreen.onComplete runs on the JavaFX Application Thread,
        // so constructing MainController (which creates JavaFX nodes) is safe.
        new SplashScreen(() -> launchApp(primaryStage)).show();
    }

    /**
     * Builds and shows the main application window.
     * Called by {@link SplashScreen} after its exit animation completes.
     *
     * <p>
     * This is the original {@code start()} body extracted verbatim —
     * no functional change, only moved to a named method so the splash
     * callback stays one readable line.
     * </p>
     */
    private void launchApp(Stage stage) {
        MainController controller = new MainController();
        ThemeManager themes = new ThemeManager();

        // Header title
        Label appTitle = new Label("Grove");
        appTitle.getStyleClass().add("panel-title");

        // Theme toggle button
        Button themeBtn = new Button();
        themeBtn.getStyleClass().add("button-small");
        Runnable refreshButtonText = () -> themeBtn.setText(
                themes.getCurrent() == ThemeManager.Theme.DARK ? "Light mode" : "Dark mode");
        refreshButtonText.run();
        themeBtn.setOnAction(e -> {
            themes.toggle();
            refreshButtonText.run();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topBar = new HBox(10, appTitle, spacer, themeBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8, 12, 8, 12));
        topBar.getStyleClass().add("panel");

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(controller.getRootPane());

        Scene scene = new Scene(root, 1180, 720);
        themes.attach(scene);

        // Global shortcut: Ctrl+T toggles light/dark theme
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN),
                themeBtn::fire);

        stage.setTitle("Grove — Tree Object Editor");
        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
