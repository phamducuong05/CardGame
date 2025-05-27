module com.myteam.game {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    opens com.myteam.game to javafx.fxml;

    exports com.myteam.game;
    exports com.myteam.game.viewcontroller;
    opens com.myteam.game.viewcontroller to javafx.fxml;
}
