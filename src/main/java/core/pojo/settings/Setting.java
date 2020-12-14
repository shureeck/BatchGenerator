package core.pojo.settings;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static core.AttributeType.*;

public class Setting {
    @JsonProperty("setting")
    private String setting;
    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private String type;
    @JsonProperty("value")
    private ArrayList<String> value;
    @JsonIgnore
    private HBox hbox;
    @JsonIgnore
    private String actualValue;


    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HBox getValueHbox() {
        setField();
        return hbox;
    }

    private void setField() {
        this.hbox = new HBox();
        if (type == null) {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.hbox.getChildren().add(tf);
        } else if (type.equalsIgnoreCase(BOOLEAN.getType())) {
            ObservableList<String> booleanList = FXCollections.observableArrayList(
                    new ArrayList<>(Arrays.asList("true", "false")));
            ComboBox comboBox = new ComboBox(booleanList);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(comboBox, Priority.ALWAYS);
            comboBox.getSelectionModel().selectFirst();
            this.hbox.getChildren().add(comboBox);
        } else if (type.equalsIgnoreCase(STRING.getType())) {
            if (value == null || value.isEmpty()) {
                TextField tf = new TextField();
                HBox.setHgrow(tf, Priority.ALWAYS);
                this.hbox.getChildren().add(tf);
            } else {
                ObservableList<String> valueList = FXCollections.observableArrayList(value);
                ComboBox comboBox = new ComboBox(valueList);
                comboBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(comboBox, Priority.ALWAYS);
                comboBox.getSelectionModel().selectFirst();
                this.hbox.getChildren().add(comboBox);
            }
        } else if (type.equalsIgnoreCase(FILE_PATH.getType())
                || type.equalsIgnoreCase(FOLDER_PATH.getType())) {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.hbox.getChildren().add(tf);
            Button browseBtn = new Button("Browse");
            this.hbox.getChildren().add(browseBtn);
            if (type.equalsIgnoreCase(FILE_PATH.getType())) {
                browseBtn.setOnAction(actionEvent -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "AWS Schema Conversion Tool"));
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        tf.setText(file.getAbsolutePath());
                    }
                });
            } else {
                browseBtn.setOnAction(actionEvent -> {
                    DirectoryChooser fileChooser = new DirectoryChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "AWS Schema Conversion Tool"));
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    File file = fileChooser.showDialog(stage);
                    if (file != null) {
                        tf.setText(file.getAbsolutePath());
                    }
                });
            }
        } else {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.hbox.getChildren().add(tf);
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setValue(ArrayList<String> value) {
        this.value = value;

    }

    public ArrayList<String> getValue() {
        return value;
    }

    public String getCurrentValue() {
        Object object = hbox.getChildren().get(0);
        if (object instanceof TextField) return ((TextField) object).getText();
        if (object instanceof ComboBox) return (String) ((ComboBox) object).getSelectionModel().getSelectedItem();
        return "";
    }
}
