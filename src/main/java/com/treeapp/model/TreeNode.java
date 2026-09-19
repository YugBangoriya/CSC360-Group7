package com.treeapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TreeNode implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private boolean isFolder;
    private List<TreeNode> children;
    private Map<String, String> properties;

    public TreeNode() {
        this.id = UUID.randomUUID().toString();
        this.name = "Untitled";
        this.isFolder = false;
        this.children = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
    }

    public TreeNode(String name, boolean isFolder) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isFolder = isFolder;
        this.children = new ArrayList<>();
        this.properties = new LinkedHashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public List<TreeNode> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new LinkedHashMap<>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void addChild(TreeNode child) {
        if (isFolder) {
            getChildren().add(child);
        }
    }

    public void removeChild(TreeNode child) {
        if (children != null) {
            children.remove(child);
        }
    }

    /** Inserts a child at a given position (clamped to the valid range). Folders only. */
    public void addChild(int index, TreeNode child) {
        if (isFolder) {
            List<TreeNode> list = getChildren();
            int safe = Math.max(0, Math.min(index, list.size()));
            list.add(safe, child);
        }
    }

    /** True if {@code other} is this node or lives anywhere below it. */
    public boolean containsInSubtree(TreeNode other) {
        if (other == this) {
            return true;
        }
        for (TreeNode child : getChildren()) {
            if (child.containsInSubtree(other)) {
                return true;
            }
        }
        return false;
    }

    /** Number of nodes in this subtree, including this one. */
    public int countNodes() {
        int total = 1;
        for (TreeNode child : getChildren()) {
            total += child.countNodes();
        }
        return total;
    }

    @Override
    public String toString() {
        return name;
    }
}
