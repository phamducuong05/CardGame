module com.mycompany.javafxapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.mycompany.javafxapp to javafx.fxml;

    exports com.mycompany.javafxapp;
}
