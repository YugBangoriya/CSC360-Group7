# CSC360 Group 7: Tree Object Editor

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen.svg)
![Theme](https://img.shields.io/badge/Theme-Light%20%2F%20Dark-purple.svg)

> **Course:** CSC360, Computer Graphics & Image Processing
> **Team:** Group 7
> **Task:** Write a JavaFX program with a tree of objects, with support for editing each object.

---

## Overview

The Tree Object Editor is a JavaFX desktop app for working with a tree of objects. Each object is either a **folder** (it can hold other objects) or an **item** (it can't). Every object has a name and any number of key/value properties, and you edit all of it from the screen. Your changes are saved to your computer automatically.

The window has three panels, plus a status bar across the bottom:

| Panel | What it does |
| :--- | :--- |
| **Explorer** (left) | Search, filter, add, rename, duplicate, delete, expand/collapse, and drag nodes around. |
| **Property Editor** (middle) | Edit the selected node's name and its key/value properties. |
| **JSON Preview** (right) | Shows the selected node and everything under it as JSON. Copy, import, or export it. |
| **Status bar** (bottom) | Live counts of total nodes, folders and items, and when the tree was last saved. |

## Screenshots

| Dark | Light |
| :--: | :--: |
| ![Dark theme](docs/screenshots/shot_dark.png) | ![Light theme](docs/screenshots/shot_light.png) |

---

## Features

| Feature | What it does |
| :--- | :--- |
| **Real-time search** | The search box above the Explorer filters nodes by name or by any property key/value as you type, and auto-expands folders so matches stay visible. Click **✕** to clear it. |
| **JSON import and export** | Export the selected node and everything under it to a `.json` file, or import a `.json` file and choose to replace the whole tree or insert it as a new child. |
| **Drag and drop** | Drag a node onto a folder to move it inside, or onto the top/bottom edge of a row to reorder it. Drops that would break the tree are blocked: you can't move the root, drop a folder into itself or one of its own children, or drop something inside an item. |
| **Expand / Collapse** | Toolbar buttons and right-click menu items to expand or collapse every folder at once. |
| **Right-click menu** | New Folder, New Item, Rename, Duplicate, Delete, Expand All, Collapse All. |
| **Light and dark theme** | Switch with the button in the top right, or press `Ctrl+T`. The app remembers your choice. |
| **Live editing** | Name and property changes go straight into the node as you type, so switching to another node never loses your work. |
| **Live status bar** | Shows the total node, folder and item counts, and confirms when the tree was last saved. |
| **Animated copy feedback** | The Copy button briefly shows "Copied! ✓". |
| **Safe deleting** | Deleting asks for confirmation and tells you how many nodes inside it will go too. |
| **Automatic saving** | Every change is written to disk, and the status bar and Property Editor confirm it (or report a failure). |
| **Input checks** | Duplicate property keys and blank names are rejected. |
| **Keyboard shortcuts** | `F2` rename, `Delete` delete, `Ctrl+T` switch theme. |

---

## Getting Started

### What you need

- **JDK 17 or newer** (check with `java -version`)
- **Apache Maven 3.6 or newer** (check with `mvn -version`)

### Run it

```bash
git clone https://github.com/YugBangoriya/CSC360-Group7.git
cd CSC360-Group7
mvn clean javafx:run
```

The first run downloads JavaFX, so it needs an internet connection and may take a minute.

> `mvnw.cmd` is in the repo, but it only uses a bundled Maven if one is unpacked into `tools\maven\` locally; that folder isn't part of the repo, so on a fresh clone it just calls your system's `mvn`. Install Maven and use `mvn` directly, as above.

### Run it in VS Code

1. Install the **Extension Pack for Java**.
2. Open the project folder and wait for Java to finish loading.
3. Open the terminal (`` Ctrl+` ``) and run `mvn clean javafx:run`.

Running the main class with the green Run button can fail with *"JavaFX runtime components are missing"*. Use the Maven command above instead.

---

## How to Use It

**Search nodes**
- Type in the search box above the Explorer to filter by name or by property key/value, live. Click **✕** to clear it.

**Import / export JSON**
- Select a node, then click **Export** in the JSON Preview header to save it (and everything under it) as a `.json` file.
- Click **Import**, choose a `.json` file, then pick **Replace Entire Tree** or **Add as Child Node**.
- A sample file is included at `docs/sample-data/acme-tech.json` — a 25-node company tree for trying out every feature.

**Add nodes**
1. Select a folder in the Explorer, or select an item to add next to it.
2. Click **+ Folder** or **+ Item** (or right-click and choose **New Folder** / **New Item**).
3. Type a name and press OK.

**Edit a node**
1. Click the node in the Explorer.
2. Change the name in **Node Name**.
3. To add a property, type a key and value at the bottom and click **Add** (or press `Enter`).
4. To change a property, double-click its key or value in the table, type, and press `Enter`.
5. To remove a property, select its row and click **Delete** in the Property Editor.

**Move nodes**
- Drag a node onto a folder to put it inside.
- Drag it to the top or bottom edge of a row to put it just above or below that row.

**Expand / collapse**
- Use the toolbar's **Expand** / **Collapse** buttons, or the right-click menu's **Expand All** / **Collapse All**, to open or close every folder at once.

**Other**
- **Rename:** select a node and press `F2`, or use the right-click menu.
- **Duplicate:** right-click a node and choose **Duplicate**. The copy is named "... (copy)" and gets its own properties and children.
- **Delete a node:** select it and press `Delete`, or click **Delete** in the Explorer.
- **Copy the JSON:** click **Copy** above the JSON Preview; it briefly shows "Copied! ✓".
- **Status bar:** check the bottom of the window for the current node/folder/item counts and the last save time.

Your data is stored in a file called `tree_data.ser` in your home folder. Delete that file if you want to start again from the default tree.

---

## Project Structure

The project follows a Model-View-Controller layout. Source files sit directly under `src/main` (there is no `java/com/treeapp/...` package path), and each top-level folder is its own Java package.

```text
CSC360-Group7/
├── pom.xml
├── README.md
├── mvnw.cmd                            # Falls back to your system `mvn` unless tools/maven is present locally
├── docs/
│   ├── README.md                       # Guide to the docs/ folder itself
│   ├── sample-data/
│   │   └── acme-tech.json              # 25-node sample company tree, for Import
│   └── screenshots/                    # Dark, light, search and copy-feedback screenshots
└── src/main/
    ├── Main.java                       # Launcher (avoids the JavaFX module-path error)
    ├── App.java                        # Window, top bar and theme button
    ├── controller/
    │   └── MainController.java         # Connects the panels to the data; every change to the tree happens here
    ├── model/
    │   ├── TreeNode.java                # One node: id, name, folder flag, children, properties
    │   └── PropertyEntry.java           # One row of the property table
    ├── view/
    │   ├── ExplorerPanel.java           # Tree, search bar, toolbar, right-click menu, drag and drop
    │   ├── PropertyEditorPanel.java
    │   └── JsonPreviewPanel.java        # JSON view with Copy, Import and Export actions
    ├── util/
    │   ├── PersistenceUtil.java         # Saves and loads ~/tree_data.ser; builds the default sample tree
    │   ├── JsonFormatter.java           # Turns a node into JSON text
    │   ├── JsonParser.java              # Parses JSON files back into TreeNode objects
    │   └── ThemeManager.java            # Switches and remembers the theme
    └── resources/styles/
        ├── dark-theme.css
        └── light-theme.css
```

### How the pieces fit together

- **Model** (`TreeNode`, `PropertyEntry`) holds the data and knows nothing about the screen.
- **Views** (`ExplorerPanel`, `PropertyEditorPanel`, `JsonPreviewPanel`) draw the screen and report what the user did.
- **Controller** (`MainController`) receives those reports, updates the model, refreshes the status bar, and saves.
- **Styling** lives in the two CSS files. Both use the same class names, so switching theme only swaps the stylesheet.

---

## Built With

- Java 17
- JavaFX 21 (`javafx-controls`)
- Apache Maven, with `javafx-maven-plugin` 0.0.8
- Plain CSS for the light and dark themes

---

## Known Limitations

- Data is saved with Java serialization, so `tree_data.ser` is not human-readable and may not open if the `TreeNode` class changes in a future version.
- There is no undo or redo yet.
- Drag and drop works inside the tree only; you can't drag between panels.
- The five Explorer toolbar buttons (`+ Folder`, `+ Item`, `Delete`, `Expand`, `Collapse`) can get visually truncated at smaller window widths; widen the window or shorten the labels if this bothers you.
