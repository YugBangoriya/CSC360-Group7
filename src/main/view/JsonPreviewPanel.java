package view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Right panel: read-only JSON of the selected node's subtree, with copy, import, and export actions. */
public class JsonPreviewPanel extends VBox {

    private final TextArea area = new TextArea();
    private Runnable onExport = () -> { };
    private Runnable onImport = () -> { };

    public JsonPreviewPanel() {
        super(10);
        setPadding(new Insets(12));
        getStyleClass().add("panel");

        Label title = new Label("JSON PREVIEW");
        title.getStyleClass().add("panel-title");

        Button copyBtn = new Button("Copy");
        copyBtn.getStyleClass().add("button-small");
        copyBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(area.getText());
            Clipboard.getSystemClipboard().setContent(content);

            // Visual confirmation feedback
            copyBtn.setText("Copied! \u2713");
            copyBtn.getStyleClass().add("button-green");
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> {
                copyBtn.setText("Copy");
                copyBtn.getStyleClass().remove("button-green");
            });
            pause.play();
        });

        Button exportBtn = new Button("Export");
        exportBtn.getStyleClass().add("button-small");
        exportBtn.setOnAction(e -> onExport.run());

        Button importBtn = new Button("Import");
        importBtn.getStyleClass().add("button-small");
        importBtn.setOnAction(e -> onImport.run());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox actions = new HBox(6, importBtn, exportBtn, copyBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(title, spacer, actions);
        header.setAlignment(Pos.CENTER_LEFT);

        area.setEditable(false);
        area.setWrapText(false); // wrapping broke the indentation of long "id" lines
        area.getStyleClass().add("json-area");
        VBox.setVgrow(area, Priority.ALWAYS);

        getChildren().addAll(header, area);
    }

    public void setJson(String json) {
        area.setText(json);
    }

    public void setOnExport(Runnable handler) {
        this.onExport = handler;
    }

    public void setOnImport(Runnable handler) {
        this.onImport = handler;
    }
}

