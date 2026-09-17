// FILE: src/main/java/com/treeapp/util/PersistenceUtil.java
package com.treeapp.util;

import com.treeapp.model.TreeNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistenceUtil {

    private static final String FILE_PATH = System.getProperty("user.home") + File.separator + "tree_data.ser";

    public static void save(TreeNode root) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(root);
            System.out.println("Tree data successfully saved to: " + FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TreeNode load() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                TreeNode root = (TreeNode) ois.readObject();
                System.out.println("Tree data loaded from: " + FILE_PATH);
                return root;
            } catch (Exception e) {
                System.err.println("Failed to load existing tree data, initializing default tree.");
                e.printStackTrace();
            }
        }
        return createDefaultTree();
    }

    public static TreeNode createDefaultTree() {
        TreeNode root = new TreeNode("Root", true);

        // People folder
        TreeNode people = new TreeNode("People", true);

        TreeNode alice = new TreeNode("Alice", false);
        alice.getProperties().put("role", "Engineer");
        alice.getProperties().put("team", "Backend");

        TreeNode bob = new TreeNode("Bob", false);
        bob.getProperties().put("role", "Designer");
        bob.getProperties().put("team", "Frontend");

        people.addChild(alice);
        people.addChild(bob);

        // Projects folder
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
