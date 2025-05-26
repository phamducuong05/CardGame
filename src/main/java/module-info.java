module com.myteam.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens com.myteam.game to javafx.fxml;

    exports com.myteam.game;
}
