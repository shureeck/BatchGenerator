 module gui {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
     requires java.xml;
     requires com.fasterxml.jackson.databind;
     requires com.fasterxml.jackson.dataformat.yaml;
     exports core.pojo;
     opens gui;
}
