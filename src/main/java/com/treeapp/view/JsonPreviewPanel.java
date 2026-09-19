package com.treeapp.view;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** Right panel: read-only JSON of the selected node's subtree, with a copy button. */
public class JsonPreviewPanel extends VBox {

    private final TextArea area = new TextArea();

    public JsonPreviewPanel() {
        super(10);
        setPadding(new Insets(12));
        getStyleClass().add("panel");

        Label title = new Label("JSON PREVIEW");
        title.getStyleClass().add("panel-title");

        Button copy = new Button("Copy");
        copy.getStyleClass().add("button-small");
        copy.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(area.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(title, spacer, copy);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        area.setEditable(false);
        area.setWrapText(false); // wrapping broke the indentation of long "id" lines
        area.getStyleClass().add("json-area");
        VBox.setVgrow(area, Priority.ALWAYS);

        getChildren().addAll(header, area);
    }

    public void setJson(String json) {
        area.setText(json);
    }
}
