// FILE: src/main/java/com/treeapp/App.java
package com.treeapp;

import com.treeapp.controller.MainController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();

        Scene scene = new Scene(mainController.getRootPane(), 1100, 700);

        // Global Catppuccin Mocha-inspired dark theme stylesheet
        String css = """
            .root {
                -fx-base: #1e1e2e;
                -fx-background: #1e1e2e;
                -fx-font-family: system;
                -fx-font-size: 13px;
            }
            .label {
                -fx-text-fill: #cdd6f4;
            }
            .text-field {
                -fx-background-color: #1e1e2e;
                -fx-text-fill: #cdd6f4;
                -fx-highlight-fill: #45475a;
                -fx-highlight-text-fill: #cdd6f4;
                -fx-prompt-text-fill: #6c7086;
                -fx-border-color: #45475a;
                -fx-border-radius: 4;
            }
            .button {
                -fx-background-color: #89b4fa;
                -fx-text-fill: #1e1e2e;
                -fx-font-weight: bold;
                -fx-background-radius: 4;
                -fx-cursor: hand;
            }
            .button:hover {
                -fx-background-color: #b4befe;
            }
            .table-view {
                -fx-background-color: #1e1e2e;
                -fx-base: #1e1e2e;
                -fx-control-inner-background: #1e1e2e;
            }
            .table-view .column-header-background {
                -fx-background-color: #313244;
            }
            .table-view .column-header, .table-view .filler {
                -fx-background-color: #313244;
                -fx-size: 32px;
            }
            .table-view .column-header .label {
                -fx-text-fill: #89b4fa;
                -fx-font-weight: bold;
            }
            .table-row-cell {
                -fx-background-color: #1e1e2e;
                -fx-text-fill: #cdd6f4;
            }
            .table-row-cell:odd {
                -fx-background-color: #181825;
            }
            .table-row-cell:selected {
                -fx-background-color: #45475a;
            }
            .table-cell {
                -fx-text-fill: #cdd6f4;
            }
            .tree-view {
                -fx-background-color: #1e1e2e;
                -fx-control-inner-background: #1e1e2e;
            }
            .tree-cell {
                -fx-background-color: #1e1e2e;
                -fx-text-fill: #cdd6f4;
            }
            .tree-cell:selected {
                -fx-background-color: #45475a;
                -fx-text-fill: #89b4fa;
            }
            .split-pane-divider {
                -fx-background-color: #313244;
                -fx-padding: 0 2 0 2;
            }
            .context-menu {
                -fx-background-color: #313244;
                -fx-border-color: #45475a;
            }
            .menu-item .label {
                -fx-text-fill: #cdd6f4;
            }
            .menu-item:focused {
                -fx-background-color: #45475a;
            }
        """;

        scene.getRoot().setStyle("-fx-base: #1e1e2e;");
        scene.getStylesheets().add("data:text/css," + css.replaceAll("\n", " "));

        primaryStage.setTitle("JavaFX Tree Object Editor with Persistence");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Ensure window pops to front on macOS desktop
        Platform.runLater(() -> {
            primaryStage.setAlwaysOnTop(true);
            primaryStage.toFront();
            primaryStage.requestFocus();
            primaryStage.setAlwaysOnTop(false);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
