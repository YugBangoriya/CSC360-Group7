package com.treeapp;

/**
 * Entry point launcher for the Tree Object Editor application.
 * Indirectly invokes App.main to bypass JavaFX module path restrictions when running JARs.
 */
public class Main {

    public static void main(String[] args) {
        App.main(args);
    }
}
