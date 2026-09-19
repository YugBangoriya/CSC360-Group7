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

    public static TreeNode createDefaultTree() {
        TreeNode root = new TreeNode("Root", true);

        TreeNode people = new TreeNode("People", true);

        TreeNode alice = new TreeNode("Alice", false);
        alice.getProperties().put("role", "Engineer");
        alice.getProperties().put("team", "Backend");

        TreeNode bob = new TreeNode("Bob", false);
        bob.getProperties().put("role", "Designer");
        bob.getProperties().put("team", "Frontend");

        people.addChild(alice);
        people.addChild(bob);

        TreeNode projects = new TreeNode("Projects", true);

        TreeNode alpha = new TreeNode("Alpha", false);
        alpha.getProperties().put("status", "Active");
        alpha.getProperties().put("deadline", "2024-12-31");

        projects.addChild(alpha);

        root.addChild(people);
        root.addChild(projects);

        return root;
    }
}
