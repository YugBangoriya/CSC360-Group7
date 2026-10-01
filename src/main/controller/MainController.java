package controller;

import model.PropertyEntry;
import model.TreeNode;
import util.JsonFormatter;
import util.JsonParser;
import util.PersistenceUtil;
import view.ExplorerPanel;
import view.ExplorerPanel.DropPosition;
import view.JsonPreviewPanel;
import view.PropertyEditorPanel;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Connects the three panels to the data model.
 * The panels only draw things and report user actions; every change to the
 * TreeNode data happens in this class.
 */
public class MainController {

    private final BorderPane mainContainer = new BorderPane();
    private final SplitPane rootPane = new SplitPane();
    private final Label statsLabel = new Label();
    private final Label saveStatusLabel = new Label();
    private final TreeNode rootNode;

    private final ExplorerPanel explorer = new ExplorerPanel();
    private final PropertyEditorPanel editor = new PropertyEditorPanel();
    private final JsonPreviewPanel preview = new JsonPreviewPanel();
    private final TreeView<TreeNode> treeView = explorer.getTreeView();

    /** The node the editor is currently showing. */
    private TreeNode current;

    public MainController() {
        this.rootNode = PersistenceUtil.load();
        buildUI();
        wireEvents();
        selectInitialNode();
    }

    public Pane getRootPane() {
        return mainContainer;
    }

    // ── setup ───────────────────────────────────────────────────────

    private void buildUI() {
        explorer.setRootNode(rootNode);
        rootPane.getItems().addAll(explorer, editor, preview);
        rootPane.setDividerPositions(0.30, 0.70);

        statsLabel.getStyleClass().add("status-text");
        saveStatusLabel.getStyleClass().add("status-saved");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusBar = new HBox(12, statsLabel, spacer, saveStatusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar");

        mainContainer.setCenter(rootPane);
        mainContainer.setBottom(statusBar);

        updateStats();
        updateSaveStatus("Saved \u2713");
    }

    private void wireEvents() {
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, old, now) -> {
            if (now == null || now.getValue() == null) {
                current = null;
                editor.clear();
                preview.setJson("");
                explorer.updateToolbar(false, false);
            } else {
                showNode(now.getValue());
                explorer.updateToolbar(true, now.getParent() == null);
            }
        });

        explorer.setOnAdd(this::addNode);
        explorer.setOnRename(this::renameNode);
        explorer.setOnDelete(this::deleteNode);
        explorer.setOnDuplicate(this::duplicateNode);
        explorer.setOnMove(this::moveNode);

        editor.setOnEdited(this::applyEditsToNode);
        editor.setOnSave(this::saveToDisk);

        preview.setOnExport(this::exportJson);
        preview.setOnImport(this::importJson);
    }

    private void selectInitialNode() {
        // Select the root so the JSON preview shows the whole tree
        treeView.getSelectionModel().select(treeView.getRoot());
    }

    // ── showing / editing the selected node ─────────────────────────

    private void showNode(TreeNode node) {
        current = node;
        String hint = node.isFolder()
                ? "Folder: it can hold other nodes. Drag nodes onto it to move them in."
                : null;
        editor.show(node.getName(), node.getProperties(), hint);
        preview.setJson(JsonFormatter.format(node));
    }

    /**
     * Called on every edit in the property panel. The change goes straight into the
     * node, so switching to another node can never lose an edit.
     */
    private void applyEditsToNode() {
        if (current == null) {
            return;
        }
        String name = editor.getNodeName().trim();
        // A blank name would leave an invisible row in the tree, so keep the old one
        if (!name.isEmpty()) {
            current.setName(name);
        }

        Map<String, String> props = new LinkedHashMap<>();
        for (PropertyEntry e : editor.getEntries()) {
            String key = e.getKey() == null ? "" : e.getKey().trim();
            if (!key.isEmpty()) {
                props.put(key, e.getValue() == null ? "" : e.getValue());
            }
        }
        current.setProperties(props);

        treeView.refresh();
        preview.setJson(JsonFormatter.format(current));
        autoSave();
    }

    private void autoSave() {
        if (PersistenceUtil.save(rootNode)) {
            updateSaveStatus("Auto-saved \u2713");
        } else {
            editor.setStatus("Could not save!", true);
            saveStatusLabel.setText("Save failed!");
        }
        updateStats();
    }

    private void saveToDisk() {
        if (PersistenceUtil.save(rootNode)) {
            editor.setStatus("Saved \u2713", false);
            updateSaveStatus("Saved \u2713");
        } else {
            editor.setStatus("Save failed", true);
            saveStatusLabel.setText("Save failed!");
            new Alert(Alert.AlertType.ERROR,
                    "Could not write to:\n" + PersistenceUtil.getFilePath()).showAndWait();
        }
        updateStats();
    }

    private void updateStats() {
        int totalFolders = countFolders(rootNode);
        int totalNodes = rootNode.countNodes();
        int totalItems = totalNodes - totalFolders;
        statsLabel.setText(String.format("Total Nodes: %d  |  Folders: %d  |  Items: %d", totalNodes, totalFolders, totalItems));
    }

    private int countFolders(TreeNode node) {
        if (node == null) return 0;
        int count = node.isFolder() ? 1 : 0;
        for (TreeNode child : node.getChildren()) {
            count += countFolders(child);
        }
        return count;
    }

    private void updateSaveStatus(String message) {
        String timestamp = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now());
        saveStatusLabel.setText(message + " (" + timestamp + ")");
    }

    // ── add / rename / delete / duplicate ───────────────────────────

    /** Adds into the selected folder, or next to the selected item. */
    private void addNode(boolean isFolder) {
        TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
        TreeItem<TreeNode> parentItem;
        if (selected == null) {
            parentItem = treeView.getRoot();
        } else if (selected.getValue().isFolder()) {
            parentItem = selected;
        } else {
            parentItem = selected.getParent() != null ? selected.getParent() : treeView.getRoot();
        }

        String type = isFolder ? "Folder" : "Item";
        TextInputDialog dialog = new TextInputDialog("New " + type);
        dialog.setTitle("Add " + type);
        dialog.setHeaderText("Name for the new " + type.toLowerCase() + " (inside '"
                + parentItem.getValue().getName() + "'):");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            TreeNode child = new TreeNode(result.get().trim(), isFolder);
            parentItem.getValue().addChild(child);

            TreeItem<TreeNode> childItem = explorer.buildTreeItem(child);
            parentItem.getChildren().add(childItem);
            parentItem.setExpanded(true);
            treeView.getSelectionModel().select(childItem);
            autoSave();
        }
    }

    private void renameNode() {
        TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        TreeNode node = selected.getValue();
        TextInputDialog dialog = new TextInputDialog(node.getName());
        dialog.setTitle("Rename");
        dialog.setHeaderText("New name for '" + node.getName() + "':");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            node.setName(result.get().trim());
            treeView.refresh();
            showNode(node);
            autoSave();
        }
    }

    private void deleteNode() {
        TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getParent() == null) {
            return; // nothing selected, or it's the root
        }
        TreeNode node = selected.getValue();
        int count = node.countNodes();

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete '" + node.getName() + "'?");
        alert.setContentText(count > 1
                ? "This also deletes the " + (count - 1) + " node(s) inside it."
                : "This cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            TreeItem<TreeNode> parent = selected.getParent();
            parent.getValue().removeChild(node);
            parent.getChildren().remove(selected);
            treeView.getSelectionModel().select(parent);
            autoSave();
        }
    }

    private void duplicateNode() {
        TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getParent() == null) {
            return;
        }
        TreeItem<TreeNode> parent = selected.getParent();
        TreeNode copy = deepCopy(selected.getValue());
        copy.setName(copy.getName() + " (copy)");

        int index = parent.getValue().getChildren().indexOf(selected.getValue()) + 1;
        parent.getValue().addChild(index, copy);

        TreeItem<TreeNode> copyItem = explorer.buildTreeItem(copy);
        parent.getChildren().add(index, copyItem);
        treeView.getSelectionModel().select(copyItem);
        autoSave();
    }

    /** Copies a node and everything under it, giving every copy a fresh id. */
    private TreeNode deepCopy(TreeNode source) {
        TreeNode copy = new TreeNode(source.getName(), source.isFolder());
        copy.getProperties().putAll(source.getProperties());
        for (TreeNode child : source.getChildren()) {
            copy.addChild(deepCopy(child));
        }
        return copy;
    }

    // ── drag and drop ───────────────────────────────────────────────

    /** Moves {@code dragged} into, above or below {@code target}. The panel already validated the drop. */
    private void moveNode(TreeNode dragged, TreeNode target, DropPosition position) {
        TreeItem<TreeNode> draggedItem = explorer.findItem(treeView.getRoot(), dragged);
        TreeItem<TreeNode> targetItem = explorer.findItem(treeView.getRoot(), target);
        if (draggedItem == null || targetItem == null) {
            return;
        }

        TreeItem<TreeNode> oldParentItem = draggedItem.getParent();
        TreeNode oldParent = oldParentItem.getValue();

        TreeItem<TreeNode> newParentItem;
        int newIndex;
        if (position == DropPosition.INTO) {
            newParentItem = targetItem;
            newIndex = target.getChildren().size();
        } else {
            newParentItem = targetItem.getParent();
            int targetIndex = newParentItem.getValue().getChildren().indexOf(target);
            newIndex = position == DropPosition.ABOVE ? targetIndex : targetIndex + 1;
        }
        TreeNode newParent = newParentItem.getValue();

        // Removing first shifts the indexes when both are in the same list
        int oldIndex = oldParent.getChildren().indexOf(dragged);
        if (oldParent == newParent && oldIndex < newIndex) {
            newIndex--;
        }
        if (oldParent == newParent && oldIndex == newIndex) {
            return; // dropped where it already is
        }

        // 1) change the data
        oldParent.removeChild(dragged);
        newParent.addChild(newIndex, dragged);

        // 2) mirror it in the visible tree
        oldParentItem.getChildren().remove(draggedItem);
        newParentItem.getChildren().add(Math.min(newIndex, newParentItem.getChildren().size()), draggedItem);
        newParentItem.setExpanded(true);

        treeView.getSelectionModel().select(draggedItem);
        autoSave();
    }

    // ── import / export JSON ────────────────────────────────────────

    private void exportJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export JSON");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
        fileChooser.setInitialFileName(current != null ? current.getName().replaceAll("[^a-zA-Z0-9._-]", "_") + ".json" : "tree.json");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                String json = JsonFormatter.format(current != null ? current : rootNode);
                writer.write(json);
                editor.setStatus("Exported to " + file.getName() + " \u2713", false);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export JSON:\n" + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void importJson() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import JSON File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));

        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            TreeNode importedNode = JsonParser.parse(content);

            Alert choice = new Alert(Alert.AlertType.CONFIRMATION);
            choice.setTitle("Import JSON");
            choice.setHeaderText("How would you like to import '" + file.getName() + "'?");

            ButtonType btnReplace = new ButtonType("Replace Entire Tree");
            ButtonType btnAddChild = new ButtonType("Add as Child Node");
            ButtonType btnCancel = ButtonType.CANCEL;
            choice.getButtonTypes().setAll(btnReplace, btnAddChild, btnCancel);

            Optional<ButtonType> result = choice.showAndWait();
            if (result.isEmpty() || result.get() == btnCancel) {
                return;
            }

            if (result.get() == btnReplace) {
                rootNode.setName(importedNode.getName());
                rootNode.setFolder(importedNode.isFolder());
                rootNode.getProperties().clear();
                rootNode.getProperties().putAll(importedNode.getProperties());
                rootNode.getChildren().clear();
                rootNode.getChildren().addAll(importedNode.getChildren());

                explorer.setRootNode(rootNode);
                selectInitialNode();
                autoSave();
                editor.setStatus("Imported tree from " + file.getName() + " \u2713", false);
            } else if (result.get() == btnAddChild) {
                TreeItem<TreeNode> selected = treeView.getSelectionModel().getSelectedItem();
                TreeItem<TreeNode> targetItem = (selected != null) ? selected : treeView.getRoot();
                TreeNode targetNode = targetItem.getValue();

                if (!targetNode.isFolder()) {
                    targetItem = targetItem.getParent() != null ? targetItem.getParent() : treeView.getRoot();
                    targetNode = targetItem.getValue();
                }

                targetNode.addChild(importedNode);
                explorer.refreshTree();

                TreeItem<TreeNode> importedItem = explorer.findItem(treeView.getRoot(), importedNode);
                if (importedItem != null) {
                    treeView.getSelectionModel().select(importedItem);
                }
                autoSave();
                editor.setStatus("Imported node into '" + targetNode.getName() + "' \u2713", false);
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to import JSON file:\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}

