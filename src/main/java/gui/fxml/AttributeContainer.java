package gui.fxml;

import core.pojo.Attribute;
import gui.BuilderController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static core.AttributeType.*;

public class AttributeContainer extends HBox {
    private Attribute attribute;

    public AttributeContainer(Attribute attribute) {
        this.attribute = attribute;
        Label attrName = new Label(this.attribute.getName() + "=");
        attrName.getStyleClass().add("label-attribute");
        this.getChildren().add(attrName);
        setField();
        getStyleClass().add("hbox-attribute");
        setOnDragDetected(this::dragDetected);
        setOnDragDone(this::dragDone);
    }

    private void setField() {
        if (attribute.getType().equalsIgnoreCase(BOOLEAN.getType())) {
            ObservableList<String> booleanList = FXCollections.observableArrayList(
                    new ArrayList<>(Arrays.asList("true", "false")));
            ComboBox comboBox = new ComboBox(booleanList);
            comboBox.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(comboBox, Priority.ALWAYS);
            comboBox.getSelectionModel().selectFirst();
            this.getChildren().add(comboBox);
        } else if (attribute.getType().equalsIgnoreCase(STRING.getType())) {
            if (attribute.getValues() == null || attribute.getValues().isEmpty()) {
                TextField tf = new TextField();
                HBox.setHgrow(tf, Priority.ALWAYS);
                this.getChildren().add(tf);
            } else {
                ObservableList<String> valueList = FXCollections.observableArrayList(attribute.getValues());
                ComboBox comboBox = new ComboBox(valueList);
                comboBox.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(comboBox, Priority.ALWAYS);
                comboBox.getSelectionModel().selectFirst();
                this.getChildren().add(comboBox);
            }
        } else if (attribute.getType().equalsIgnoreCase(SCHEMA_PATH.getType())) {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            tf.setPromptText("Schemas.SCHEMA_NAME.Tables.TABLE_NAME");
            this.getChildren().add(tf);
        } else if (attribute.getType().equalsIgnoreCase(FILE_PATH.getType())
                || attribute.getType().equalsIgnoreCase(FOLDER_PATH.getType())) {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.getChildren().add(tf);
            Button browseBtn = new Button("Browse");
            this.getChildren().add(browseBtn);
            if (attribute.getType().equalsIgnoreCase(FILE_PATH.getType())) {
                browseBtn.setOnAction(actionEvent -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "AWS Schema Conversion Tool"));
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) tf.setText(file.getAbsolutePath());
                });
            } else {
                browseBtn.setOnAction(actionEvent -> {
                    DirectoryChooser fileChooser = new DirectoryChooser();
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home"), "AWS Schema Conversion Tool"));
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    File file = fileChooser.showDialog(stage);
                    if (file != null) tf.setText(file.getAbsolutePath());
                });
            }
        } else {
            TextField tf = new TextField();
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.getChildren().add(tf);
        }
    }

    public void dragDetected(MouseEvent event) {
        String attributeValueString = getAttributeString();
        String attributeNameLabel = attribute.getName();
        Dragboard dragboard = this.startDragAndDrop(TransferMode.COPY_OR_MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(attributeNameLabel + attributeValueString);
        dragboard.setContent(content);
        event.consume();
    }

    private void dragDone(DragEvent event) {
        // Check how data was transfered to the target. If it was moved, clear the text in the source.
        TransferMode modeUsed = event.getTransferMode();
        if (modeUsed == TransferMode.MOVE) {
            this.setDisable(true);
        }
        event.consume();
    }

    public String getAttributeString() {
        String attributeValueString = "";
        ObservableList<Node> list = this.getChildren();
        if (list.get(1) instanceof Label) attributeValueString = ((Label) list.get(1)).getText();
        else if (list.get(1) instanceof TextField) attributeValueString = ((TextField) list.get(1)).getText();
        else if (list.get(1) instanceof ComboBox)
            attributeValueString = ((ComboBox) list.get(1)).getValue().toString();
        attributeValueString = "=\"" + attributeValueString + "\"";
        return attributeValueString;
    }

    public String getAttributeName() {
        return attribute.getName();
    }

}


