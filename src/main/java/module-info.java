 module gui {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
     requires java.xml;
     requires com.fasterxml.jackson.databind;
     requires com.fasterxml.jackson.dataformat.yaml;
     requires org.apache.logging.log4j;
     exports core.pojo;
     opens gui;
}
