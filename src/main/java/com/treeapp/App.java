package com.treeapp;

import com.treeapp.controller.MainController;
import com.treeapp.util.ThemeManager;

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

public class App extends Application {

    @Override
    public void start(Stage stage) {
        MainController controller = new MainController();
        ThemeManager themes = new ThemeManager();

        // Top bar with the theme toggle
        Label appTitle = new Label("Tree Object Editor");
        appTitle.getStyleClass().add("panel-title");

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

        // Ctrl+T toggles the theme too
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN),
                themeBtn::fire);

        stage.setTitle("JavaFX Tree Object Editor with Persistence");
        stage.setMinWidth(900);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
