package com.treeapp.util;

import com.treeapp.model.TreeNode;

import java.util.Map;

/** Turns a TreeNode (and its subtree) into pretty-printed JSON text. */
public final class JsonFormatter {

    private JsonFormatter() {
    }

    public static String format(TreeNode node) {
        return node == null ? "" : format(node, 0);
    }

    private static String format(TreeNode node, int level) {
        String indent = "  ".repeat(level);
        String inner = "  ".repeat(level + 1);
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append("{\n");
        sb.append(inner).append("\"id\": \"").append(escape(node.getId())).append("\",\n");
        sb.append(inner).append("\"name\": \"").append(escape(node.getName())).append("\",\n");
        sb.append(inner).append("\"isFolder\": ").append(node.isFolder()).append(",\n");

        // properties
        sb.append(inner).append("\"properties\": {");
        int i = 0;
        int total = node.getProperties().size();
        if (total > 0) {
            sb.append("\n");
            for (Map.Entry<String, String> e : node.getProperties().entrySet()) {
                sb.append(inner).append("  \"").append(escape(e.getKey())).append("\": \"")
                        .append(escape(e.getValue())).append("\"");
                sb.append(++i < total ? ",\n" : "\n");
            }
            sb.append(inner);
        }
        sb.append("},\n");

        // children
        sb.append(inner).append("\"children\": [");
        int c = 0;
        int totalChildren = node.getChildren().size();
        if (totalChildren > 0) {
            sb.append("\n");
            for (TreeNode child : node.getChildren()) {
                sb.append(format(child, level + 2));
                sb.append(++c < totalChildren ? ",\n" : "\n");
            }
            sb.append(inner);
        }
        sb.append("]\n");

        sb.append(indent).append("}");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
