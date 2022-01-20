module com.cocoon.resourcepackpacker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.cocoon.resourcepackpacker to javafx.fxml;
    exports com.cocoon.resourcepackpacker;
    exports com.cocoon.resourcepackpacker.controllers;
    opens com.cocoon.resourcepackpacker.controllers to javafx.fxml;
}