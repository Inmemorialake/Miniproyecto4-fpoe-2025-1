module org.example.eiscuno {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens org.example.eiscuno to javafx.fxml, org.junit.platform.commons;
    opens org.example.eiscuno.controller to javafx.fxml;
    opens org.example.eiscuno.model.card to org.junit.platform.commons;
    opens org.example.eiscuno.model.deck to org.junit.platform.commons;
    opens org.example.eiscuno.model.common to org.junit.platform.commons;
    opens org.example.eiscuno.model.player to org.junit.platform.commons;
    opens org.example.eiscuno.model.table to org.junit.platform.commons;
    exports org.example.eiscuno;
}