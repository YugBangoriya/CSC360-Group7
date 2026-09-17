// FILE: src/main/java/com/treeapp/controller/MainController.java
package com.treeapp.controller;

import com.treeapp.model.TreeNode;
import com.treeapp.util.PersistenceUtil;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class MainController {

    private final SplitPane rootPane;

    private TreeNode rootTreeNode;

    // Tree panel controls
    private TreeView<TreeNode> treeView;

    // Property editor controls
    private TextField nameField;
    private TableView<PropertyEntry> propertyTable;
    private ObservableList<PropertyEntry> propertyEntries;
    private TextField newKeyField;
    private TextField newValueField;
    private Button addPropertyBtn;
    private Button deletePropertyBtn;
    private Button saveBtn;
    private Label savedLabel;

    // JSON preview control
    private TextArea jsonPreviewArea;

    public MainController() {
        this.rootPane = new SplitPane();
        this.propertyEntries = FXCollections.observableArrayList();

        // Load data tree
        this.rootTreeNode = PersistenceUtil.load();

        initUI();
    }

    public SplitPane getRootPane() {
        return rootPane;
    }

    private void initUI() {
        rootPane.setStyle("-fx-background-color: #1e1e2e; -fx-base: #1e1e2e;");

        VBox leftPanel = createLeftPanel();
        VBox centerPanel = createCenterPanel();
        VBox rightPanel = createRightPanel();

        rootPane.getItems().addAll(leftPanel, centerPanel, rightPanel);
        rootPane.setDividerPositions(0.30, 0.70);

        // Select initial item if available
        if (!treeView.getRoot().getChildren().isEmpty()) {
            treeView.getSelectionModel().select(treeView.getRoot().getChildren().get(0));
        } else {
            treeView.getSelectionModel().select(treeView.getRoot());
        }
    }

    // ── LEFT PANEL (Tree View) ──────────────────────────────
    private VBox createLeftPanel() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: #2a2a3e;");

        Label title = new Label("EXPLORER");
        title.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-font-size: 14px;");

        treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.setStyle(
                "-fx-background-color: #1e1e2e; -fx-control-inner-background: #1e1e2e; -fx-text-fill: #cdd6f4;");

        TreeItem<TreeNode> rootItem = buildTreeItem(rootTreeNode);
        treeView.setRoot(rootItem);

        // Cell Factory with Icons and Formatting
        treeView.setCellFactory(tv -> {
            TreeCell<TreeNode> cell = new TreeCell<>() {
                @Override
                protected void updateItem(TreeNode item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setContextMenu(null);
                    } else {
                        setText(item.getName());
                        if (item.isFolder()) {
                            Label icon = new Label(getTreeItem() != null && getTreeItem().isExpanded() ? "📂" : "📁");
                            icon.setStyle("-fx-font-size: 14px;");
                            setGraphic(icon);
                        } else {
                            Label icon = new Label("📄");
                            icon.setStyle("-fx-font-size: 14px;");
                            setGraphic(icon);
                        }
                        setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 13px;");
                    }
                }
            };

            // Double click toggle expand/collapse for folders
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && cell.getItem() != null && cell.getItem().isFolder()
                        && cell.getTreeItem() != null) {
                    cell.getTreeItem().setExpanded(!cell.getTreeItem().isExpanded());
                }
            });

            return cell;
        });

        // Context Menu for right-click on nodes
        ContextMenu contextMenu = new ContextMenu();
        MenuItem addFolderItem = new MenuItem("Add Child Folder");
        MenuItem addItemItem = new MenuItem("Add Child Item");
        MenuItem renameItem = new MenuItem("Rename");
        MenuItem deleteItem = new MenuItem("Delete");

        contextMenu.getItems().addAll(addFolderItem, addItemItem, renameItem, deleteItem);

        treeView.setContextMenu(contextMenu);

        // Dynamically configure menu actions based on selected node
        contextMenu.setOnShowing(e -> {
            TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
            if (selected == null || selected.getValue() == null) {
                addFolderItem.setDisable(true);
                addItemItem.setDisable(true);
                renameItem.setDisable(true);
                deleteItem.setDisable(true);
            } else {
                boolean isFolder = selected.getValue().isFolder();
                addFolderItem.setDisable(!isFolder);
                addItemItem.setDisable(!isFolder);
                renameItem.setDisable(false);
                deleteItem.setDisable(selected == treeView.getRoot());
            }
        });

        addFolderItem.setOnAction(e -> handleAddChild(true));
        addItemItem.setOnAction(e -> handleAddChild(false));
        renameItem.setOnAction(e -> handleRenameNode());
        deleteItem.setOnAction(e -> handleDeleteNode());

        // Tree selection listener
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                loadNodeProperties(newVal.getValue());
            } else {
                clearNodeProperties();
            }
        });

        // Toolbar buttons for quick actions
        HBox toolbar = new HBox(6);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addFolderBtn = new Button("📁 + Folder");
        addFolderBtn.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 8; -fx-cursor: hand;");
        addFolderBtn.setOnAction(e -> handleAddFolderFromToolbar());

        Button addItemBtn = new Button("📄 + Item");
        addItemBtn.setStyle(
                "-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 8; -fx-cursor: hand;");
        addItemBtn.setOnAction(e -> handleAddItemFromToolbar());

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle(
                "-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 8; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDeleteNode());

        toolbar.getChildren().addAll(addFolderBtn, addItemBtn, deleteBtn);

        VBox.setVgrow(treeView, Priority.ALWAYS);
        container.getChildren().addAll(title, toolbar, treeView);
        return container;
    }

    private TreeItem<TreeNode> buildTreeItem(TreeNode node) {
        TreeItem<TreeNode> item = new TreeItem<>(node);
        if (node.isFolder()) {
            item.setExpanded(true);
            for (TreeNode child : node.getChildren()) {
                item.getChildren().add(buildTreeItem(child));
            }
        }
        return item;
    }

    // ── CENTER PANEL (Property Editor) ──────────────────────
    private VBox createCenterPanel() {
        VBox container = new VBox(14);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: #2a2a3e;");

        Label title = new Label("PROPERTY EDITOR");
        title.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Node Name Row
        HBox nameBox = new HBox(10);
        nameBox.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Node Name:");
        nameLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-weight: bold;");

        nameField = new TextField();
        nameField.setPromptText("Enter node name");
        nameField.setStyle(
                "-fx-background-color: #1e1e2e; -fx-text-fill: #cdd6f4; -fx-border-color: #45475a; -fx-border-radius: 4;");
        HBox.setHgrow(nameField, Priority.ALWAYS);
        nameBox.getChildren().addAll(nameLabel, nameField);

        // TableView for Properties
        propertyTable = new TableView<>();
        propertyTable.setEditable(true);
        propertyTable.setStyle(
                "-fx-background-color: #1e1e2e; -fx-control-inner-background: #1e1e2e; -fx-table-cell-border-color: #313244;");

        TableColumn<PropertyEntry, String> keyCol = new TableColumn<>("Property Key");
        keyCol.setCellValueFactory(data -> data.getValue().keyProperty());
        keyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        keyCol.setOnEditCommit(e -> e.getRowValue().setKey(e.getNewValue()));
        keyCol.setPrefWidth(180);

        TableColumn<PropertyEntry, String> valueCol = new TableColumn<>("Property Value");
        valueCol.setCellValueFactory(data -> data.getValue().valueProperty());
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(e -> e.getRowValue().setValue(e.getNewValue()));
        valueCol.setPrefWidth(220);

        propertyTable.getColumns().add(keyCol);
        propertyTable.getColumns().add(valueCol);
        propertyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        propertyTable.setItems(propertyEntries);

        VBox.setVgrow(propertyTable, Priority.ALWAYS);

        // Add Property controls
        HBox addBox = new HBox(8);
        addBox.setAlignment(Pos.CENTER_LEFT);

        newKeyField = new TextField();
        newKeyField.setPromptText("New Key");
        newKeyField.setStyle(
                "-fx-background-color: #1e1e2e; -fx-text-fill: #cdd6f4; -fx-border-color: #45475a; -fx-border-radius: 4;");
        HBox.setHgrow(newKeyField, Priority.ALWAYS);

        newValueField = new TextField();
        newValueField.setPromptText("New Value");
        newValueField.setStyle(
                "-fx-background-color: #1e1e2e; -fx-text-fill: #cdd6f4; -fx-border-color: #45475a; -fx-border-radius: 4;");
        HBox.setHgrow(newValueField, Priority.ALWAYS);

        addPropertyBtn = new Button("Add Property");
        addPropertyBtn.setStyle(
                "-fx-background-color: #89b4fa; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-cursor: hand;");
        addPropertyBtn.setOnAction(e -> handleAddProperty());

        deletePropertyBtn = new Button("Delete Selected");
        deletePropertyBtn.setStyle(
                "-fx-background-color: #f38ba8; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-cursor: hand;");
        deletePropertyBtn.setOnAction(e -> handleDeleteProperty());

        addBox.getChildren().addAll(newKeyField, newValueField, addPropertyBtn, deletePropertyBtn);

        // Save Controls
        HBox saveBox = new HBox(12);
        saveBox.setAlignment(Pos.CENTER_LEFT);

        saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #a6e3a1; -fx-text-fill: #1e1e2e; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 20; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> handleSaveNode());

        savedLabel = new Label("Saved ✓");
        savedLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold; -fx-font-size: 14px;");
        savedLabel.setOpacity(0.0);

        saveBox.getChildren().addAll(saveBtn, savedLabel);

        container.getChildren().addAll(title, nameBox, propertyTable, addBox, saveBox);
        return container;
    }

    // ── RIGHT PANEL (JSON Preview) ──────────────────────────
    private VBox createRightPanel() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(12));
        container.setStyle("-fx-background-color: #2a2a3e;");

        Label title = new Label("JSON PREVIEW");
        title.setStyle("-fx-text-fill: #89b4fa; -fx-font-weight: bold; -fx-font-size: 14px;");

        jsonPreviewArea = new TextArea();
        jsonPreviewArea.setEditable(false);
        jsonPreviewArea.setWrapText(true);
        jsonPreviewArea.setStyle(
                "-fx-control-inner-background: #1e1e2e; -fx-text-fill: #a6e3a1; -fx-font-family: 'Monospaced', 'Consolas', monospace; -fx-font-size: 12px;");

        VBox.setVgrow(jsonPreviewArea, Priority.ALWAYS);
        container.getChildren().addAll(title, jsonPreviewArea);
        return container;
    }

    // ── LOGIC & EVENT HANDLERS ──────────────────────────────
    private void loadNodeProperties(TreeNode node) {
        nameField.setText(node.getName());
        propertyEntries.clear();
        for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
            propertyEntries.add(new PropertyEntry(entry.getKey(), entry.getValue()));
        }
        updateJsonPreview(node);
    }

    private void clearNodeProperties() {
        nameField.clear();
        propertyEntries.clear();
        jsonPreviewArea.clear();
    }

    private void handleAddProperty() {
        String key = newKeyField.getText().trim();
        String val = newValueField.getText().trim();
        if (!key.isEmpty()) {
            propertyEntries.add(new PropertyEntry(key, val));
            newKeyField.clear();
            newValueField.clear();
        }
    }

    private void handleDeleteProperty() {
        PropertyEntry selected = propertyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            propertyEntries.remove(selected);
        }
    }

    private void handleSaveNode() {
        TreeItem<TreeNode> selectedTreeItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedTreeItem == null || selectedTreeItem.getValue() == null) {
            return;
        }

        TreeNode node = selectedTreeItem.getValue();
        node.setName(nameField.getText().trim());

        Map<String, String> newProps = new LinkedHashMap<>();
        for (PropertyEntry entry : propertyEntries) {
            if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                newProps.put(entry.getKey().trim(), entry.getValue() != null ? entry.getValue() : "");
            }
        }
        node.setProperties(newProps);

        // Refresh tree view cell label
        treeView.refresh();

        // Save tree data to disk
        PersistenceUtil.save(rootTreeNode);

        // Update JSON preview
        updateJsonPreview(node);

        // Fade transition for green "Saved ✓" label
        savedLabel.setOpacity(1.0);
        FadeTransition fade = new FadeTransition(Duration.seconds(2), savedLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }

    private void handleAddFolderFromToolbar() {
        createNodeInCurrentContext(true);
    }

    private void handleAddItemFromToolbar() {
        createNodeInCurrentContext(false);
    }

    private void createNodeInCurrentContext(boolean isFolder) {
        TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
        TreeItem<TreeNode> parentItem;

        if (selected == null) {
            parentItem = treeView.getRoot();
        } else if (selected.getValue() != null && selected.getValue().isFolder()) {
            parentItem = selected;
        } else if (selected.getParent() != null) {
            parentItem = selected.getParent();
        } else {
            parentItem = treeView.getRoot();
        }

        String typeStr = isFolder ? "Folder" : "Item";
        TextInputDialog dialog = new TextInputDialog("New " + typeStr);
        dialog.setTitle("Add " + typeStr);
        dialog.setHeaderText("Enter name for the new " + typeStr.toLowerCase() + ":");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String name = result.get().trim();
            TreeNode childNode = new TreeNode(name, isFolder);
            parentItem.getValue().addChild(childNode);

            TreeItem<TreeNode> childItem = buildTreeItem(childNode);
            parentItem.getChildren().add(childItem);
            parentItem.setExpanded(true);
            treeView.getSelectionModel().select(childItem);

            PersistenceUtil.save(rootTreeNode);
        }
    }

    private void handleAddChild(boolean isFolder) {
        createNodeInCurrentContext(isFolder);
    }

    private void handleRenameNode() {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null) {
            return;
        }

        TreeNode node = selectedItem.getValue();
        TextInputDialog dialog = new TextInputDialog(node.getName());
        dialog.setTitle("Rename Node");
        dialog.setHeaderText("Enter new name for node:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            node.setName(result.get().trim());
            nameField.setText(node.getName());
            treeView.refresh();
            updateJsonPreview(node);
            PersistenceUtil.save(rootTreeNode);
        }
    }

    private void handleDeleteNode() {
        TreeItem<TreeNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null || selectedItem.getValue() == null || selectedItem == treeView.getRoot()) {
            return;
        }

        TreeNode node = selectedItem.getValue();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete '" + node.getName() + "'?");
        alert.setContentText("Are you sure you want to delete this node and all of its contents?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TreeItem<TreeNode> parentItem = selectedItem.getParent();
            if (parentItem != null && parentItem.getValue() != null) {
                parentItem.getValue().removeChild(node);
                parentItem.getChildren().remove(selectedItem);
                treeView.getSelectionModel().select(parentItem);
                PersistenceUtil.save(rootTreeNode);
            }
        }
    }

    private void updateJsonPreview(TreeNode node) {
        if (node == null) {
            jsonPreviewArea.clear();
        } else {
            jsonPreviewArea.setText(formatJson(node, 0));
        }
    }

    private String formatJson(TreeNode node, int indentLevel) {
        if (node == null)
            return "";
        StringBuilder sb = new StringBuilder();
        String indent = "  ".repeat(indentLevel);
        String childIndent = "  ".repeat(indentLevel + 1);

        sb.append("{\n");
        sb.append(childIndent).append("\"id\": \"").append(escapeJson(node.getId())).append("\",\n");
        sb.append(childIndent).append("\"name\": \"").append(escapeJson(node.getName())).append("\",\n");
        sb.append(childIndent).append("\"isFolder\": ").append(node.isFolder()).append(",\n");

        // properties
        sb.append(childIndent).append("\"properties\": {\n");
        int propCount = 0;
        int totalProps = node.getProperties().size();
        for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
            propCount++;
            sb.append(childIndent).append("  \"").append(escapeJson(entry.getKey())).append("\": \"")
                    .append(escapeJson(entry.getValue())).append("\"");
            if (propCount < totalProps)
                sb.append(",");
            sb.append("\n");
        }
        sb.append(childIndent).append("},\n");

        // children
        sb.append(childIndent).append("\"children\": [\n");
        int childCount = 0;
        int totalChildren = node.getChildren().size();
        for (TreeNode child : node.getChildren()) {
            childCount++;
            sb.append(formatJson(child, indentLevel + 2));
            if (childCount < totalChildren)
                sb.append(",");
            sb.append("\n");
        }
        sb.append(childIndent).append("]\n");

        sb.append(indent).append("}");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null)
            return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Helper model class for Property Table rows
    public static class PropertyEntry {
        private final StringProperty key;
        private final StringProperty value;

        public PropertyEntry(String key, String value) {
            this.key = new SimpleStringProperty(key);
            this.value = new SimpleStringProperty(value);
        }

        public String getKey() {
            return key.get();
        }

        public void setKey(String k) {
            key.set(k);
        }

        public StringProperty keyProperty() {
            return key;
        }

        public String getValue() {
            return value.get();
        }

        public void setValue(String v) {
            value.set(v);
        }

        public StringProperty valueProperty() {
            return value;
        }
    }
}
