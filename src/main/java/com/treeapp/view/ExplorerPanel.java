package com.treeapp.view;

import com.treeapp.model.TreeNode;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

import java.util.function.Consumer;

/**
 * Left panel: the tree, the toolbar, the right-click context menu and drag and drop.
 * It only shows and edits the tree structure; saving and the property editor are handled
 * by the controller through the callbacks below.
 */
public class ExplorerPanel extends VBox {

    /** Where a dragged node lands relative to the row it is dropped on. */
    public enum DropPosition { INTO, ABOVE, BELOW }

    /** Implemented by the controller to perform the real move. */
    public interface MoveHandler {
        void move(TreeNode dragged, TreeNode target, DropPosition position);
    }

    private static final DataFormat NODE_FORMAT = new DataFormat("application/x-treeapp-node-id");

    private final TreeView<TreeNode> treeView = new TreeView<>();
    private final Button addFolderBtn = new Button("+ Folder");
    private final Button addItemBtn = new Button("+ Item");
    private final Button deleteBtn = new Button("Delete");

    // What the controller wants to happen
    private Consumer<Boolean> onAdd = isFolder -> { };
    private Runnable onRename = () -> { };
    private Runnable onDelete = () -> { };
    private Runnable onDuplicate = () -> { };
    private MoveHandler onMove = (d, t, p) -> { };

    /** The node currently being dragged (kept here because DataFormat can't carry live objects). */
    private TreeItem<TreeNode> draggedItem;

    public ExplorerPanel() {
        super(10);
        setPadding(new Insets(12));
        getStyleClass().add("panel");

        Label title = new Label("EXPLORER");
        title.getStyleClass().add("panel-title");

        addFolderBtn.getStyleClass().addAll("button-small");
        addItemBtn.getStyleClass().addAll("button-small", "button-green");
        deleteBtn.getStyleClass().addAll("button-small", "button-red");
        addFolderBtn.setOnAction(e -> onAdd.accept(true));
        addItemBtn.setOnAction(e -> onAdd.accept(false));
        deleteBtn.setOnAction(e -> onDelete.run());

        HBox toolbar = new HBox(6, addFolderBtn, addItemBtn, deleteBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        treeView.setShowRoot(true);
        treeView.setCellFactory(tv -> createCell());
        treeView.setContextMenu(createContextMenu());
        treeView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                onDelete.run();
            } else if (e.getCode() == KeyCode.F2) {
                onRename.run();
            }
        });

        VBox.setVgrow(treeView, Priority.ALWAYS);
        getChildren().addAll(title, toolbar, treeView);
    }

    // ── public API for the controller ───────────────────────────────

    public TreeView<TreeNode> getTreeView() {
        return treeView;
    }

    public void setOnAdd(Consumer<Boolean> handler) { this.onAdd = handler; }
    public void setOnRename(Runnable handler) { this.onRename = handler; }
    public void setOnDelete(Runnable handler) { this.onDelete = handler; }
    public void setOnDuplicate(Runnable handler) { this.onDuplicate = handler; }
    public void setOnMove(MoveHandler handler) { this.onMove = handler; }

    /** Builds the visible tree from the data model. Folders start expanded. */
    public TreeItem<TreeNode> buildTreeItem(TreeNode node) {
        TreeItem<TreeNode> item = new TreeItem<>(node);
        if (node.isFolder()) {
            item.setExpanded(true);
            for (TreeNode child : node.getChildren()) {
                item.getChildren().add(buildTreeItem(child));
            }
        }
        return item;
    }

    /** Finds the tree item that wraps a given node, or null. */
    public TreeItem<TreeNode> findItem(TreeItem<TreeNode> from, TreeNode node) {
        if (from == null) {
            return null;
        }
        if (from.getValue() == node) {
            return from;
        }
        for (TreeItem<TreeNode> child : from.getChildren()) {
            TreeItem<TreeNode> found = findItem(child, node);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** Enable or disable toolbar buttons depending on what is selected. */
    public void updateToolbar(boolean hasSelection, boolean isRoot) {
        deleteBtn.setDisable(!hasSelection || isRoot);
    }

    // ── cell rendering + drag and drop ──────────────────────────────

    private TreeCell<TreeNode> createCell() {
        TreeCell<TreeNode> cell = new TreeCell<>() {
            @Override
            protected void updateItem(TreeNode node, boolean empty) {
                super.updateItem(node, empty);
                if (empty || node == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(node.getName());
                    setGraphic(createIcon(node.isFolder(), getTreeItem() != null && getTreeItem().isExpanded()));
                }
            }
        };

        // Refresh the folder icon when it is expanded/collapsed with the arrow
        cell.treeItemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem != null) {
                newItem.expandedProperty().addListener((o, was, is) -> cell.updateIndex(cell.getIndex()));
            }
        });

        // Right-click should act on the row under the cursor, not the previous selection
        cell.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY && cell.getTreeItem() != null) {
                treeView.getSelectionModel().select(cell.getTreeItem());
            }
        });

        cell.setOnDragDetected(event -> {
            TreeItem<TreeNode> item = cell.getTreeItem();
            // The root cannot be moved
            if (item == null || item.getParent() == null) {
                return;
            }
            draggedItem = item;
            Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(NODE_FORMAT, item.getValue().getId());
            db.setContent(content);
            event.consume();
        });

        cell.setOnDragOver(event -> handleDragOver(cell, event));
        cell.setOnDragExited(event -> clearDropStyle(cell));
        cell.setOnDragDropped(event -> handleDrop(cell, event));
        cell.setOnDragDone(event -> {
            draggedItem = null;
            clearDropStyle(cell);
        });

        return cell;
    }

    private void handleDragOver(TreeCell<TreeNode> cell, DragEvent event) {
        clearDropStyle(cell);
        TreeItem<TreeNode> target = cell.getTreeItem();
        if (draggedItem == null || target == null || !event.getDragboard().hasContent(NODE_FORMAT)) {
            return;
        }
        DropPosition pos = positionFor(cell, event);
        if (!isValidDrop(draggedItem, target, pos)) {
            return;
        }
        event.acceptTransferModes(TransferMode.MOVE);
        cell.getStyleClass().add(switch (pos) {
            case INTO -> "drop-into";
            case ABOVE -> "drop-above";
            case BELOW -> "drop-below";
        });
        event.consume();
    }

    private void handleDrop(TreeCell<TreeNode> cell, DragEvent event) {
        TreeItem<TreeNode> target = cell.getTreeItem();
        boolean done = false;
        if (draggedItem != null && target != null) {
            DropPosition pos = positionFor(cell, event);
            if (isValidDrop(draggedItem, target, pos)) {
                onMove.move(draggedItem.getValue(), target.getValue(), pos);
                done = true;
            }
        }
        clearDropStyle(cell);
        event.setDropCompleted(done);
        event.consume();
    }


    /**
     * Draws the folder / file icon with vector shapes instead of emoji, so it looks the same on
     * every operating system and follows the theme colour (set in the stylesheet).
     */
    private static Node createIcon(boolean folder, boolean open) {
        SVGPath path = new SVGPath();
        if (folder) {
            // folder body with a tab; the open state is drawn as an outline
            path.setContent("M0 2 Q0 0 2 0 L5.5 0 L7 2 L12 2 Q14 2 14 4 L14 10 Q14 12 12 12 L2 12 Q0 12 0 10 Z");
            path.getStyleClass().add(open ? "icon-folder-open" : "icon-folder");
        } else {
            // page with a folded corner
            path.setContent("M1 0 L8 0 L12 4 L12 12 Q12 13 11 13 L1 13 Q0 13 0 12 L0 1 Q0 0 1 0 Z");
            path.getStyleClass().add("icon-file");
        }
        return path;
    }

    /** Top quarter = above, bottom quarter = below, middle = into (folders only). */
    private DropPosition positionFor(TreeCell<TreeNode> cell, DragEvent event) {
        TreeItem<TreeNode> target = cell.getTreeItem();
        double h = cell.getHeight();
        double y = event.getY();
        boolean isFolder = target != null && target.getValue().isFolder();
        boolean isRoot = target != null && target.getParent() == null;

        if (isRoot) {
            return DropPosition.INTO; // nothing can go above or below the root
        }
        if (!isFolder) {
            return y < h / 2 ? DropPosition.ABOVE : DropPosition.BELOW;
        }
        if (y < h * 0.25) {
            return DropPosition.ABOVE;
        }
        if (y > h * 0.75) {
            return DropPosition.BELOW;
        }
        return DropPosition.INTO;
    }

    /**
     * The rules that keep the tree valid:
     *  - the root can't be moved
     *  - you can't drop a node onto itself
     *  - you can't drop a folder into itself or anything inside it (that would create a loop)
     *  - "into" only works on folders
     */
    static boolean isValidDrop(TreeItem<TreeNode> dragged, TreeItem<TreeNode> target, DropPosition pos) {
        if (dragged.getParent() == null || dragged == target) {
            return false;
        }
        if (pos == DropPosition.INTO && !target.getValue().isFolder()) {
            return false;
        }
        if (dragged.getValue().containsInSubtree(target.getValue())) {
            return false;
        }
        return true;
    }

    private void clearDropStyle(TreeCell<TreeNode> cell) {
        cell.getStyleClass().removeAll("drop-into", "drop-above", "drop-below");
    }

    // ── context menu ────────────────────────────────────────────────

    private ContextMenu createContextMenu() {
        MenuItem addFolder = new MenuItem("New Folder");
        MenuItem addItem = new MenuItem("New Item");
        MenuItem rename = new MenuItem("Rename");
        MenuItem duplicate = new MenuItem("Duplicate");
        MenuItem delete = new MenuItem("Delete");
        MenuItem expandAll = new MenuItem("Expand All");
        MenuItem collapseAll = new MenuItem("Collapse All");

        rename.setAccelerator(new KeyCodeCombination(KeyCode.F2));
        delete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE));

        addFolder.setOnAction(e -> onAdd.accept(true));
        addItem.setOnAction(e -> onAdd.accept(false));
        rename.setOnAction(e -> onRename.run());
        duplicate.setOnAction(e -> onDuplicate.run());
        delete.setOnAction(e -> onDelete.run());
        expandAll.setOnAction(e -> setExpandedRecursive(treeView.getRoot(), true));
        collapseAll.setOnAction(e -> {
            setExpandedRecursive(treeView.getRoot(), false);
            treeView.getRoot().setExpanded(true);
        });

        ContextMenu menu = new ContextMenu(addFolder, addItem, new SeparatorMenuItem(),
                rename, duplicate, delete, new SeparatorMenuItem(), expandAll, collapseAll);

        menu.setOnShowing(e -> {
            TreeItem<TreeNode> sel = treeView.getSelectionModel().getSelectedItem();
            boolean has = sel != null && sel.getValue() != null;
            boolean isRoot = has && sel.getParent() == null;
            // "New ..." is always allowed: the controller drops the node into the folder,
            // or next to the item if an item is selected.
            addFolder.setDisable(!has);
            addItem.setDisable(!has);
            rename.setDisable(!has);
            duplicate.setDisable(!has || isRoot);
            delete.setDisable(!has || isRoot);
        });
        return menu;
    }

    private void setExpandedRecursive(TreeItem<TreeNode> item, boolean expanded) {
        if (item == null) {
            return;
        }
        if (!item.isLeaf()) {
            item.setExpanded(expanded);
        }
        for (TreeItem<TreeNode> child : item.getChildren()) {
            setExpandedRecursive(child, expanded);
        }
    }
}
