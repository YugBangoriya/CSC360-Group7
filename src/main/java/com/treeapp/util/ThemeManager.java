package com.treeapp.util;

import javafx.scene.Scene;

import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Switches the whole application between the dark and light stylesheets
 * and remembers the user's choice between runs.
 */
public class ThemeManager {

    public enum Theme {
        DARK("/styles/dark-theme.css"),
        LIGHT("/styles/light-theme.css");

        private final String resource;

        Theme(String resource) {
            this.resource = resource;
        }

        public String url() {
            return Objects.requireNonNull(
                    ThemeManager.class.getResource(resource), "Missing stylesheet: " + resource).toExternalForm();
        }

        public Theme other() {
            return this == DARK ? LIGHT : DARK;
        }
    }

    private static final String PREF_KEY = "theme";
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    private Scene scene;
    private Theme current;

    public ThemeManager() {
        String saved = prefs.get(PREF_KEY, Theme.DARK.name());
        try {
            current = Theme.valueOf(saved);
        } catch (IllegalArgumentException e) {
            current = Theme.DARK;
        }
    }

    public void attach(Scene scene) {
        this.scene = scene;
        apply();
    }

    public Theme getCurrent() {
        return current;
    }

    public void toggle() {
        current = current.other();
        prefs.put(PREF_KEY, current.name());
        apply();
    }

    private void apply() {
        if (scene == null) {
            return;
        }
        scene.getStylesheets().clear();
        scene.getStylesheets().add(current.url());
    }
}
