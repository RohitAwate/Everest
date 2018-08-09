package com.rohitawate.everest.controllers.visualizers;

import javafx.scene.control.ScrollPane;

public abstract class Visualizer extends ScrollPane {

    Visualizer() {
        setFitToHeight(true);
        setFitToWidth(true);
    }

    public abstract void populate(String body) throws Exception;

    public abstract void clear();
}
