package com.treeapp.controller;

import com.treeapp.model.PropertyEntry;
import com.treeapp.model.TreeNode;
import com.treeapp.util.JsonFormatter;
import com.treeapp.util.PersistenceUtil;
import com.treeapp.view.ExplorerPanel;
import com.treeapp.view.ExplorerPanel.DropPosition;
import com.treeapp.view.JsonPreviewPanel;
import com.treeapp.view.PropertyEditorPanel;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Connects the three panels to the data model.
 * The panels only draw things and report user actions; every change to the
 * TreeNode data happens in this class.
 */
public class MainController {

    private final SplitPane rootPane = new SplitPane();
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

    public SplitPane getRootPane() {
        return rootPane;
    }

    // ── setup ───────────────────────────────────────────────────────

    private void buildUI() {
        treeView.setRoot(explorer.buildTreeItem(rootNode));
        rootPane.getItems().addAll(explorer, editor, preview);
        rootPane.setDividerPositions(0.30, 0.70);
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
        if (!PersistenceUtil.save(rootNode)) {
            editor.setStatus("Could not save!", true);
        }
    }

    private void saveToDisk() {
        if (PersistenceUtil.save(rootNode)) {
            editor.setStatus("Saved \u2713", false);
        } else {
            editor.setStatus("Save failed", true);
            new Alert(Alert.AlertType.ERROR,
                    "Could not write to:\n" + PersistenceUtil.getFilePath()).showAndWait();
        }
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
}
