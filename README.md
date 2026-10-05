# Grove

![Java](https://img.shields.io/badge/Java-17-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue.svg)
![Build](https://img.shields.io/badge/Build-Maven-brightgreen.svg)
![Theme](https://img.shields.io/badge/Theme-Light%20%2F%20Dark-purple.svg)

> **Course:** CSC360 — Computer Graphics and Digital Image Processing
> **Institution:** Ahmedabad University · Monsoon Semester 2026
> **Team:** Group 7

---

## What is Grove?

Grove is a desktop application for organising things into a tree — exactly like the folder panel on the left side of Windows File Explorer, but for any kind of structured data you want. You can create folders and items, give each one a name and as many custom properties as you like, drag things around to reorder them, and see everything as clean JSON at any time. Every change saves automatically, so nothing is ever lost between sessions.

The window has three panels, plus a status bar at the bottom:

| Panel | What you see |
|:------|:-------------|
| **Explorer** (left) | Your entire tree. Browse, search, add, rename, move, and delete nodes. |
| **Property Editor** (centre) | Edit the name and properties of whatever node you have selected. |
| **JSON Preview** (right) | The selected node shown as JSON. Copy it, export it to a file, or import from one. |
| **Status bar** (bottom) | Live count of total nodes, folders, and items. Shows when the tree was last saved. |

---

## The Story Behind Grove

In Session 9, when the professor introduced Group 7's project, he described it like this: *"Think of the left panel in Windows File Explorer — a tree of objects, where clicking a node opens it for editing, and everything is saved to disk so nothing is lost between sessions."*

That one sentence turned something abstract into something concrete and familiar. Every person in the group had used a file explorer their whole life without thinking of it as a tree data structure. The moment it was framed that way, it became something we could actually design and build from scratch. Grove is what that idea became.

---

## Features

| Feature | What it does |
|:--------|:-------------|
| **Real-time search** | Type in the search bar to filter nodes by name or property as you type. Matching paths stay visible; everything else fades away. Click ✕ to clear. |
| **Drag and drop** | Drag a node onto a folder to move it inside, or drag to the edge of a row to reorder. Illegal moves (dropping a folder into itself, for example) are blocked automatically. |
| **JSON import and export** | Export any node (and everything under it) to a `.json` file. Import a `.json` file and either replace the whole tree or add it as a child. |
| **Expand / Collapse all** | Open or close every folder at once from the toolbar or the right-click menu. |
| **Right-click menu** | New Folder, New Item, Rename, Duplicate, Delete, Expand All, Collapse All. |
| **Light and dark theme** | Toggle with the button in the top-right or `Ctrl+T`. The app remembers your choice. |
| **Live editing** | Changes to a node's name or properties apply as you type. You will never lose work by clicking somewhere else. |
| **Live status bar** | Always shows the current node count and confirms when the tree was last saved. |
| **Safe delete** | Before deleting a folder, Grove tells you exactly how many child nodes will also be removed and asks you to confirm. |
| **Auto-save** | Every change is written to disk immediately. There is no Save button to forget. |
| **Input validation** | Blank names and duplicate property keys are rejected with a clear message. |
| **Keyboard shortcuts** | `F2` to rename, `Delete` to delete, `Ctrl+T` to toggle theme. |

---

## Screenshots

| Dark Theme | Light Theme |
|:----------:|:-----------:|
| ![Dark](docs/screenshots/shot_dark.png) | ![Light](docs/screenshots/shot_light.png) |

| Search Active | Copy Feedback |
|:-------------:|:-------------:|
| ![Search](docs/screenshots/shot_search.png) | ![Copy](docs/screenshots/shot_copy_feedback.png) |

---

## Get Started

**You need:**

| Tool | Minimum version | How to check |
|:-----|:----------------|:-------------|
| JDK | 17 | `java -version` |
| Apache Maven | 3.6 | `mvn -version` |

**Run it:**

```bash
git clone https://github.com/YugBangoriya/CSC360-Group7.git
cd CSC360-Group7
mvn clean javafx:run
```

The first run downloads the JavaFX runtime — it needs an internet connection and takes about a minute. After that it starts quickly.

> **VS Code:** install the *Extension Pack for Java*, open the project folder, and run `mvn clean javafx:run` in the integrated terminal (`` Ctrl+` ``). The green Run button will fail with a JavaFX error — always use the Maven command above.

---

## How to Use It

**Search for a node**
Type in the search box above the Explorer. The tree filters as you type, showing only matching nodes and the folders that contain them. Click **✕** to show everything again.

**Add a folder or item**
1. Select the folder you want to add inside (or any item to add alongside it).
2. Click **+ Folder** or **+ Item**, or right-click → **New Folder / New Item**.
3. Type a name and press OK.

**Edit a node**
1. Click the node in the Explorer.
2. Change the name in the **Node Name** field on the right.
3. To add a property: type a key and value at the bottom of the property table and click **Add**.
4. To edit a property: double-click its key or value in the table, type, press `Enter`.
5. To delete a property: select its row and click **Delete** in the property section.

**Move nodes around**
Drag a node onto a folder to move it inside. Drag to the top or bottom edge of a row to place it just above or below that row.

**Import or export JSON**
Select a node, then click **Export** in the JSON Preview header to save it as a `.json` file. Click **Import** to load a `.json` file and choose whether to replace the whole tree or add it as a child.
A sample file for testing is at `docs/sample-data/acme-tech.json`.

**Reset to the default tree**
Delete `~/tree_data.ser` from your home folder and relaunch Grove.

---

## Project Structure

```
CSC360-Group7/
├── pom.xml                            ← tells Maven how to build the project
├── README.md
├── docs/
│   ├── sample-data/acme-tech.json     ← sample tree for testing import
│   └── screenshots/
└── src/main/
    ├── Main.java                      ← starts the app
    ├── App.java                       ← sets up the window and theme
    ├── SplashScreen.java              ← animated startup screen
    ├── controller/MainController.java ← the brain: handles all data changes
    ├── model/                         ← the data: TreeNode, PropertyEntry
    ├── view/                          ← the three panels the user sees
    ├── util/                          ← save/load, JSON, theme switching
    └── resources/styles/              ← dark-theme.css, light-theme.css
```

Grove follows the Model-View-Controller pattern: the data lives in `model/`, the visuals live in `view/`, and all the logic connecting them lives in `controller/`. This keeps each part independent, which makes the code easier to understand and extend.

For architecture diagrams, a full code walkthrough, and how this project maps to course sessions → **[Project Wiki](../../wiki)**

---

## Known Limitations

- The save file (`~/tree_data.ser`) is not human-readable. If `TreeNode` changes between versions, you may need to delete it and start fresh.
- There is no undo / redo.
- Drag-and-drop works only within the tree.
- The Explorer toolbar buttons can get visually clipped at narrow window widths — widen the window if that happens.

---

## License & Attribution

Course project for **CSC360 — Computer Graphics and Digital Image Processing** at Ahmedabad University, Monsoon Semester 2026.
**Faculty:** Susanta Tewari · **Team:** Group 7
