# CSC360 Group 7 — Tree of Objects Editor (Taskwood)

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen.svg)
![Theme](https://img.shields.io/badge/UI%20Theme-Catppuccin%20Mocha-purple.svg)

> **Course:** AU CSC360 — Computer Graphics & Image Processing  
> **Team:** Group 7  
> **Project:** Tree of Objects Editor / Taskwood Manager  

---

## 📌 Overview

**Taskwood / Tree of Objects Editor** is a local-first JavaFX desktop application designed to organize hierarchical data structures (such as Workspaces, Projects, and Tasks or organizational nodes) into an interactive, expandable tree. 

Each node in the tree can act as either a **container (folder)** or an **item (leaf)** and supports custom dynamic key-value metadata properties edited via a real-time Property Inspector table. All changes automatically persist locally using Java Object Serialization.

---

## ✨ Key Features

- 🌲 **Hierarchical Tree View:** 
  - Unlimited nesting levels with expandable/collapsible folder nodes and item leaves.
  - Interactive right-click **Context Menu** for adding, renaming, and deleting nodes on the fly.
- 📋 **Dynamic Property Inspector:**
  - View and modify key-value attribute maps associated with any selected tree node.
  - Double-click inline cell editing powered by `TextFieldTableCell`.
  - Convenient control panel to add new attributes or remove existing ones.
- 💾 **Automatic Object Persistence:**
  - Complete tree structure state serialization to `tree_data.ser` in the user's home directory (`PersistenceUtil`).
  - Fallback automatic initialization of a default template tree if no saved state exists.
- 🎨 **Modern Catppuccin Mocha Dark UI:**
  - Modern, dark-themed styling with rich typography, clean borders, custom split pane divider handles, and highlighted table/tree selections.
- 🚀 **Standard Java Launcher Integration:**
  - Includes a non-application wrapper launcher [`Main.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/Main.java) to prevent module path bootstrap issues during standalone runtime execution.

---

## 🛠️ Project Architecture & Structure

The codebase is built cleanly using the **Model-View-Controller (MVC)** design pattern:

```text
CSC360-Group7/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── treeapp/
                    ├── App.java                   # JavaFX Application Entry Point & Global CSS Styles
                    ├── Main.java                  # Standalone Launcher Wrapper
                    ├── controller/
                    │   └── MainController.java    # Central UI Controller & Event Handlers
                    ├── model/
                    │   └── TreeNode.java          # Recursive Serializable Object Model
                    └── util/
                        └── PersistenceUtil.java   # Binary Object Serialization & Default Tree Generation
```

### Component Breakdown

| File | Description |
| :--- | :--- |
| [`TreeNode.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/model/TreeNode.java) | Data model representing a node in the tree hierarchy. Contains UUID, name, folder flag, child list, and a key-value property map (`LinkedHashMap`). |
| [`MainController.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/controller/MainController.java) | Manages the JavaFX UI components (`TreeView`, `TableView`, `SplitPane`), layout assembly, context menus, and inline editing commit handlers. |
| [`PersistenceUtil.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/util/PersistenceUtil.java) | Handles binary serialization (`ObjectOutputStream`/`ObjectInputStream`) saving data to `~/tree_data.ser`. |
| [`App.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/App.java) | Configures the primary stage, applies the Catppuccin Mocha dark theme stylesheet, and handles window focus logic. |
| [`Main.java`](file:///c:/Users/User/OneDrive/Documents/AU%20CSC360/CSC360-Group7/src/main/java/com/treeapp/Main.java) | Entry wrapper invoking `App.main(args)` to ensure compatibility with standard Java runtime execution without explicit `--module-path` flags. |

---

## ⚡ Prerequisites

Make sure you have the following installed on your machine:

- **Java Development Kit (JDK):** Version 17 or higher
- **Apache Maven:** Version 3.6 or higher

---

## 🚀 Building & Running

### 1. Build the Project
Compile the project using Maven:
```bash
mvn clean compile
```

### 2. Run the Application
You can launch the application using the **JavaFX Maven Plugin**:
```bash
mvn javafx:run
```

Alternatively, run via Maven Exec Plugin targeting the launcher class:
```bash
mvn exec:java -Dexec.mainClass="com.treeapp.Main"
```

---

## 📖 Usage Instructions

1. **Navigating the Tree:**
   - Click on any node in the left pane tree view to load its dynamic properties in the right inspector pane.
   - Click the expand arrow next to a folder node to toggle child visibility.
2. **Managing Nodes (Right-Click Context Menu):**
   - **Add Child Folder / Item:** Right-click a folder node to create nested containers or leaf nodes.
   - **Rename Node:** Right-click any node and select *Rename* to update its display label.
   - **Delete Node:** Right-click a node and select *Delete* to remove it and all of its sub-children.
3. **Editing Properties:**
   - **Add Property:** Enter a *New Key* and *New Value* in the text fields at the bottom of the inspector pane, then click **Add Property**.
   - **Inline Edit:** Double-click any key or value cell in the Property Table to edit text directly. Press `Enter` to commit changes.
   - **Delete Property:** Select a property row in the table and click **Delete Selected**.
4. **Saving Data:**
   - Click the **Save Tree State** button at the bottom of the tree view to persist your data to disk immediately. Data also persists upon serialization requests.

---

## 🧰 Built With

- **Language:** Java 17
- **UI Framework:** JavaFX 21 (`javafx-controls`)
- **Build System:** Apache Maven
- **Plugin:** `javafx-maven-plugin` (0.0.8)
- **Styling:** Custom CSS (Catppuccin Mocha Palette)