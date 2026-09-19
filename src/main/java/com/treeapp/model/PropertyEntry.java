package com.treeapp.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * One row of the property table (key/value pair).
 * Uses JavaFX properties so the TableView can bind to it and edit it in place.
 */
public class PropertyEntry {

    private final StringProperty key;
    private final StringProperty value;

    public PropertyEntry(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String k) {
        key.set(k);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String v) {
        value.set(v);
    }

    public StringProperty valueProperty() {
        return value;
    }
}
