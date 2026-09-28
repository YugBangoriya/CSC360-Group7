package com.treeapp.util;

import com.treeapp.model.TreeNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lightweight JSON parser to convert JSON text into TreeNode structures.
 * Supports both exported TreeApp JSON structures and generic JSON files.
 */
public final class JsonParser {

    private JsonParser() {
    }

    /**
     * Parses a JSON string into a root TreeNode.
     *
     * @param json the raw JSON string
     * @return the parsed root TreeNode
     * @throws IllegalArgumentException if the JSON is invalid or doesn't start with '{'
     */
    public static TreeNode parse(String json) throws IllegalArgumentException {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON content is empty.");
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{")) {
            throw new IllegalArgumentException("JSON content must be an object starting with '{'.");
        }
        Map<String, Object> map = parseObject(trimmed, new int[]{0});
        return mapToTreeNode("Root", map);
    }

    private static TreeNode mapToTreeNode(String defaultName, Map<String, Object> map) {
        String name = defaultName;
        if (map.containsKey("name") && map.get("name") instanceof String str && !str.isBlank()) {
            name = str;
        }

        boolean isFolder = true;
        if (map.containsKey("isFolder") && map.get("isFolder") instanceof Boolean b) {
            isFolder = b;
        }

        TreeNode node = new TreeNode(name, isFolder);
        if (map.containsKey("id") && map.get("id") instanceof String idStr && !idStr.isBlank()) {
            node.setId(idStr);
        }

        // If explicitly formatted properties
        if (map.containsKey("properties") && map.get("properties") instanceof Map<?, ?> rawProps) {
            for (Map.Entry<?, ?> e : rawProps.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    node.getProperties().put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
            }
        }

        // If explicitly formatted children
        if (map.containsKey("children") && map.get("children") instanceof List<?> childrenList) {
            for (Object item : childrenList) {
                if (item instanceof Map<?, ?> childMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedMap = (Map<String, Object>) childMap;
                    node.addChild(mapToTreeNode("Item", typedMap));
                }
            }
        } else {
            // Fallback for generic JSON structures
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.equals("name") || key.equals("id") || key.equals("isFolder")
                        || key.equals("properties") || key.equals("children")) {
                    continue;
                }
                Object val = entry.getValue();
                if (val instanceof Map<?, ?> childMap) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> typedMap = (Map<String, Object>) childMap;
                    node.addChild(mapToTreeNode(key, typedMap));
                } else if (val instanceof List<?> list) {
                    TreeNode listFolder = new TreeNode(key, true);
                    int idx = 0;
                    for (Object elem : list) {
                        if (elem instanceof Map<?, ?> elemMap) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typedElem = (Map<String, Object>) elemMap;
                            listFolder.addChild(mapToTreeNode(key + "[" + (idx++) + "]", typedElem));
                        } else if (elem != null) {
                            TreeNode itemNode = new TreeNode(key + "[" + (idx++) + "]", false);
                            itemNode.getProperties().put("value", String.valueOf(elem));
                            listFolder.addChild(itemNode);
                        }
                    }
                    node.addChild(listFolder);
                } else if (val != null) {
                    node.getProperties().put(key, String.valueOf(val));
                }
            }
        }

        return node;
    }

    // ── Recursive Descent Token Parser ──────────────────────────────

    private static Map<String, Object> parseObject(String s, int[] pos) {
        Map<String, Object> map = new LinkedHashMap<>();
        consume(s, pos, '{');
        skipWhitespace(s, pos);
        if (pos[0] < s.length() && s.charAt(pos[0]) == '}') {
            pos[0]++;
            return map;
        }
        while (pos[0] < s.length()) {
            skipWhitespace(s, pos);
            String key = parseString(s, pos);
            skipWhitespace(s, pos);
            consume(s, pos, ':');
            skipWhitespace(s, pos);
            Object value = parseValue(s, pos);
            map.put(key, value);
            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++;
            } else if (pos[0] < s.length() && s.charAt(pos[0]) == '}') {
                pos[0]++;
                break;
            } else {
                break;
            }
        }
        return map;
    }

    private static List<Object> parseArray(String s, int[] pos) {
        List<Object> list = new ArrayList<>();
        consume(s, pos, '[');
        skipWhitespace(s, pos);
        if (pos[0] < s.length() && s.charAt(pos[0]) == ']') {
            pos[0]++;
            return list;
        }
        while (pos[0] < s.length()) {
            skipWhitespace(s, pos);
            Object val = parseValue(s, pos);
            list.add(val);
            skipWhitespace(s, pos);
            if (pos[0] < s.length() && s.charAt(pos[0]) == ',') {
                pos[0]++;
            } else if (pos[0] < s.length() && s.charAt(pos[0]) == ']') {
                pos[0]++;
                break;
            } else {
                break;
            }
        }
        return list;
    }

    private static Object parseValue(String s, int[] pos) {
        skipWhitespace(s, pos);
        if (pos[0] >= s.length()) return null;
        char c = s.charAt(pos[0]);
        if (c == '{') {
            return parseObject(s, pos);
        } else if (c == '[') {
            return parseArray(s, pos);
        } else if (c == '"') {
            return parseString(s, pos);
        } else if (c == 't' || c == 'f') {
            return parseBoolean(s, pos);
        } else if (c == 'n') {
            consumeLiteral(s, pos, "null");
            return null;
        } else {
            return parseNumberOrRaw(s, pos);
        }
    }

    private static String parseString(String s, int[] pos) {
        consume(s, pos, '"');
        StringBuilder sb = new StringBuilder();
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]++);
            if (c == '"') {
                return sb.toString();
            } else if (c == '\\' && pos[0] < s.length()) {
                char next = s.charAt(pos[0]++);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static Boolean parseBoolean(String s, int[] pos) {
        if (s.startsWith("true", pos[0])) {
            pos[0] += 4;
            return true;
        } else if (s.startsWith("false", pos[0])) {
            pos[0] += 5;
            return false;
        }
        return false;
    }

    private static void consumeLiteral(String s, int[] pos, String lit) {
        if (s.startsWith(lit, pos[0])) {
            pos[0] += lit.length();
        }
    }

    private static Object parseNumberOrRaw(String s, int[] pos) {
        int start = pos[0];
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                break;
            }
            pos[0]++;
        }
        String token = s.substring(start, pos[0]).trim();
        try {
            if (token.contains(".")) {
                return Double.parseDouble(token);
            } else {
                return Long.parseLong(token);
            }
        } catch (NumberFormatException e) {
            return token;
        }
    }

    private static void consume(String s, int[] pos, char expected) {
        skipWhitespace(s, pos);
        if (pos[0] < s.length() && s.charAt(pos[0]) == expected) {
            pos[0]++;
        }
    }

    private static void skipWhitespace(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) {
            pos[0]++;
        }
    }
}
