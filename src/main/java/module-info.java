module com.myteam.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.myteam.game to javafx.fxml;

    exports com.myteam.game;
}
