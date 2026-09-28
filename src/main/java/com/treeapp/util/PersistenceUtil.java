package com.treeapp.util;

import com.treeapp.model.TreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Saves and loads the tree using Java object serialization.
 * The file lives in the user's home directory as tree_data.ser.
 */
public class PersistenceUtil {

    private static final String FILE_PATH = System.getProperty("user.home") + File.separator + "tree_data.ser";

    /**
     * Writes the tree to disk.
     *
     * @return true if the write succeeded, false otherwise (so the UI can tell the user)
     */
    public static boolean save(TreeNode root) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(root);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to save tree to " + FILE_PATH);
            e.printStackTrace();
            return false;
        }
    }

    public static TreeNode load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (TreeNode) ois.readObject();
            } catch (Exception e) {
                System.err.println("Failed to load existing tree data, using the default tree.");
                e.printStackTrace();
            }
        }
        return createDefaultTree();
    }

    public static String getFilePath() {
        return FILE_PATH;
    }

    /**
     * The tree shown the first time the app runs (or after tree_data.ser is deleted).
     *
     * It is a small company, chosen so that every feature has something realistic to work on:
     * folders nested four levels deep, an empty folder to drop things into, people and projects
     * with mostly different property keys (they share only "status"), values that repeat across
     * folders (for the search box),
     * two nodes with the same name, and values with quotes and backslashes (for JSON escaping).
     */
    public static TreeNode createDefaultTree() {
        // Engineering: two teams of people, plus an empty QA team to try drag and drop on
        TreeNode backend = add(folder("Backend Team", "lead", "Rahul Mehta", "stack", "Java, Spring"),
                item("Rahul Mehta", "role", "Team Lead", "team", "Backend", "level", "Senior",
                        "location", "Ahmedabad", "status", "Active"),
                item("Sneha Patel", "role", "Developer", "team", "Backend", "level", "Mid",
                        "location", "Pune", "status", "Active"),
                item("Karan Shah", "role", "Developer", "team", "Backend", "level", "Junior",
                        "location", "Ahmedabad", "status", "On Leave"));

        TreeNode frontend = add(folder("Frontend Team", "lead", "Ananya Iyer", "stack", "JavaFX, CSS"),
                item("Ananya Iyer", "role", "UI Developer", "team", "Frontend", "level", "Senior",
                        "location", "Remote", "status", "Active"),
                item("Dev Joshi", "role", "Developer", "team", "Frontend", "level", "Junior",
                        "location", "Ahmedabad", "status", "Active"));

        TreeNode qa = folder("QA Team");   // empty on purpose: no children and no properties

        TreeNode engineering = add(folder("Engineering", "head", "Priya Nair", "budget", "450000"),
                backend, frontend, qa);

        // Design
        TreeNode design = add(folder("Design", "head", "Meera Kapoor"),
                item("Meera Kapoor", "role", "UX Designer", "team", "Design", "level", "Senior",
                        "location", "Mumbai", "status", "Active"));

        // Projects: Sprint 1 sits four levels below the root
        TreeNode sprint1 = add(folder("Sprint 1", "goal", "Ship the new homepage", "ends", "2026-10-15"),
                item("Homepage Mockup", "status", "Done", "owner", "Meera Kapoor", "hours", "24"),
                item("API Integration", "status", "In Progress", "owner", "Rahul Mehta", "hours", "40"));

        TreeNode website = add(folder("Website Redesign", "status", "Active", "deadline", "2026-11-30",
                        "priority", "High", "budget", "120000"),
                sprint1,
                item("Release Notes", "status", "Draft", "owner", "Sneha Patel"));

        TreeNode mobile = add(folder("Mobile App", "status", "Planning", "deadline", "2027-03-15",
                        "priority", "Medium"),
                item("Login Screen", "status", "In Progress", "owner", "Ananya Iyer", "hours", "16"),
                item("Release Notes", "status", "Not Started", "owner", "Dev Joshi"));   // same name as above

        TreeNode archive = add(folder("Archive"),
                item("Legacy Intranet", "status", "Closed", "deadline", "2024-06-30"));

        TreeNode projects = add(folder("Projects"), website, mobile, archive);

        // Documents: one node with awkward values, to check editing and JSON escaping
        TreeNode documents = add(folder("Documents"),
                item("Meeting Notes",
                        "date", "2026-09-25",
                        "path", "C:\\Acme\\Docs\\meeting-notes.txt",
                        "quote", "He said \"ship it on Friday\"",
                        "summary", "Discussed the sprint goals, the API integration risks, "
                                + "and who reviews the homepage mockup before the release."));

        return add(folder("Acme Tech Ltd", "industry", "Software", "headquarters", "Ahmedabad"),
                engineering, design, projects, documents);
    }

    // Small helpers so the sample data above reads like the tree it builds.

    private static TreeNode folder(String name, String... keyValues) {
        return node(name, true, keyValues);
    }

    private static TreeNode item(String name, String... keyValues) {
        return node(name, false, keyValues);
    }

    /** keyValues is a flat list: key1, value1, key2, value2, ... */
    private static TreeNode node(String name, boolean isFolder, String... keyValues) {
        TreeNode node = new TreeNode(name, isFolder);
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            node.getProperties().put(keyValues[i], keyValues[i + 1]);
        }
        return node;
    }

    private static TreeNode add(TreeNode parent, TreeNode... children) {
        for (TreeNode child : children) {
            parent.addChild(child);
        }
        return parent;
    }
}
