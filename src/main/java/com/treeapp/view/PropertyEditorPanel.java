package com.treeapp.view;

import com.treeapp.model.PropertyEntry;

import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Centre panel: edit the selected node's name and its key/value properties.
 * Edits are reported through {@link #setOnEdited(Runnable)} so the controller can apply them
 * to the node straight away (no "forgot to click Save" data loss).
 */
public class PropertyEditorPanel extends VBox {

    private final TextField nameField = new TextField();
    private final TableView<PropertyEntry> table = new TableView<>();
    private final ObservableList<PropertyEntry> entries = FXCollections.observableArrayList();
    private final TextField newKeyField = new TextField();
    private final TextField newValueField = new TextField();
    private final Button addBtn = new Button("Add");
    private final Button deleteBtn = new Button("Delete");
    private final Button saveBtn = new Button("Save to Disk");
    private final Label statusLabel = new Label();
    private final Label hintLabel = new Label();

    private Runnable onEdited = () -> { };
    private Runnable onSave = () -> { };
    /** True while the controller is filling the fields, so that doesn't count as a user edit. */
    private boolean loading;

    public PropertyEditorPanel() {
        super(14);
        setPadding(new Insets(12));
        getStyleClass().add("panel");

        Label title = new Label("PROPERTY EDITOR");
        title.getStyleClass().add("panel-title");

        // Name row
        Label nameLabel = new Label("Node Name:");
        nameLabel.getStyleClass().add("field-label");
        nameField.setPromptText("Enter node name");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        HBox nameRow = new HBox(10, nameLabel, nameField);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Table
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(entries);
        table.setPlaceholder(new Label("This node has no properties yet."));

        TableColumn<PropertyEntry, String> keyCol = new TableColumn<>("Property Key");
        keyCol.setCellValueFactory(d -> d.getValue().keyProperty());
        keyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        keyCol.setOnEditCommit(e -> {
            String newKey = e.getNewValue() == null ? "" : e.getNewValue().trim();
            if (newKey.isEmpty() || keyExists(newKey, e.getRowValue())) {
                setStatus("That key is empty or already used.", true);
                table.refresh();
                return;
            }
            e.getRowValue().setKey(newKey);
            fireEdited();
        });

        TableColumn<PropertyEntry, String> valueCol = new TableColumn<>("Property Value");
        valueCol.setCellValueFactory(d -> d.getValue().valueProperty());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(e -> {
            e.getRowValue().setValue(e.getNewValue() == null ? "" : e.getNewValue());
            fireEdited();
        });

        table.getColumns().add(keyCol);
        table.getColumns().add(valueCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        hintLabel.getStyleClass().add("hint-label");
        hintLabel.setWrapText(true);
        hintLabel.setManaged(false);
        hintLabel.setVisible(false);

        // Add-property row: the two fields share the space; buttons keep their full width
        newKeyField.setPromptText("New Key");
        newValueField.setPromptText("New Value");
        HBox.setHgrow(newKeyField, Priority.ALWAYS);
        HBox.setHgrow(newValueField, Priority.ALWAYS);
        newKeyField.setMinWidth(60);
        newValueField.setMinWidth(60);
        addBtn.setMinWidth(Button.USE_PREF_SIZE);
        deleteBtn.setMinWidth(Button.USE_PREF_SIZE);
        addBtn.setTooltip(new javafx.scene.control.Tooltip("Add this key/value property"));
        deleteBtn.setTooltip(new javafx.scene.control.Tooltip("Delete the selected property row"));
        deleteBtn.getStyleClass().add("button-red");
        addBtn.setOnAction(e -> addProperty());
        deleteBtn.setOnAction(e -> deleteSelectedProperty());
        newValueField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addProperty();
            }
        });
        newKeyField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                newValueField.requestFocus();
            }
        });
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                deleteSelectedProperty();
            }
        });

        HBox addRow = new HBox(8, newKeyField, newValueField, addBtn, deleteBtn);
        addRow.setAlignment(Pos.CENTER_LEFT);

        // Save row
        saveBtn.getStyleClass().addAll("button-green", "button-large");
        saveBtn.setOnAction(e -> onSave.run());
        statusLabel.getStyleClass().add("saved-label");
        statusLabel.setOpacity(0);
        HBox saveRow = new HBox(12, saveBtn, statusLabel);
        saveRow.setAlignment(Pos.CENTER_LEFT);

        // Live editing of the name
        nameField.textProperty().addListener((obs, old, now) -> {
            if (!loading) {
                fireEdited();
            }
        });

        getChildren().addAll(title, nameRow, hintLabel, table, addRow, saveRow);
    }

    // ── API for the controller ──────────────────────────────────────

    public void setOnEdited(Runnable r) { this.onEdited = r; }
    public void setOnSave(Runnable r) { this.onSave = r; }

    public String getNodeName() { return nameField.getText(); }

    public ObservableList<PropertyEntry> getEntries() { return entries; }

    /** Fill the panel for a node. {@code hint} may be null. */
    public void show(String name, java.util.Map<String, String> props, String hint) {
        loading = true;
        try {
            nameField.setText(name);
            entries.clear();
            props.forEach((k, v) -> entries.add(new PropertyEntry(k, v)));
        } finally {
            loading = false;
        }
        boolean hasHint = hint != null && !hint.isEmpty();
        hintLabel.setText(hint == null ? "" : hint);
        hintLabel.setManaged(hasHint);
        hintLabel.setVisible(hasHint);
        setEnabled(true);
    }

    public void clear() {
        loading = true;
        try {
            nameField.clear();
            entries.clear();
        } finally {
            loading = false;
        }
        hintLabel.setManaged(false);
        hintLabel.setVisible(false);
        setEnabled(false);
    }

    public void setEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        table.setDisable(!enabled);
        newKeyField.setDisable(!enabled);
        newValueField.setDisable(!enabled);
        addBtn.setDisable(!enabled);
        deleteBtn.setDisable(!enabled);
        saveBtn.setDisable(!enabled);
    }

    /** Show a short message next to the Save button; fades out after two seconds. */
    public void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("saved-label", "error-label");
        statusLabel.getStyleClass().add(error ? "error-label" : "saved-label");
        statusLabel.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(2.5), statusLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setDelay(Duration.seconds(0.6));
        fade.play();
    }

    // ── internals ───────────────────────────────────────────────────

    private void fireEdited() {
        onEdited.run();
    }

    private boolean keyExists(String key, PropertyEntry ignore) {
        for (PropertyEntry e : entries) {
            if (e != ignore && key.equals(e.getKey())) {
                return true;
            }
        }
        return false;
    }

    private void addProperty() {
        String key = newKeyField.getText().trim();
        String val = newValueField.getText().trim();
        if (key.isEmpty()) {
            setStatus("Enter a key first.", true);
            newKeyField.requestFocus();
            return;
        }
        if (keyExists(key, null)) {
            setStatus("Key \"" + key + "\" already exists.", true);
            newKeyField.requestFocus();
            newKeyField.selectAll();
            return;
        }
        entries.add(new PropertyEntry(key, val));
        newKeyField.clear();
        newValueField.clear();
        newKeyField.requestFocus();
        fireEdited();
    }

    private void deleteSelectedProperty() {
        PropertyEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            entries.remove(selected);
            fireEdited();
        }
    }
}
