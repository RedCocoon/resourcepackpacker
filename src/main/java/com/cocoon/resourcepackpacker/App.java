package com.cocoon.resourcepackpacker;

import com.cocoon.resourcepackpacker.controllers.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class App extends Application {
    static double height = 700;
    static double width = 1000;

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = construct();

        stage.setTitle("Resource Pack Packer");
        stage.setScene(scene);
        stage.show();

        // Load files based on config (if available)
    }

    public static Scene construct() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("main.fxml"));
        fxmlLoader.setController(new MainController());
        // Create & Return new scene
        return new Scene(fxmlLoader.load(), width, height);
    }

    public static void main(String[] args) {
        launch();
    }
}