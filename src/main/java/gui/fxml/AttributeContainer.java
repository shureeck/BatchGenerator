package gui.fxml;

import core.pojo.Attribute;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logger.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static core.AttributeType.*;
import static logger.LogMessages.*;

public class AttributeContainer extends HBox {
    private Attribute attribute;

    public AttributeContainer(Attribute attribute) {
        this.attribute = attribute;
        CheckBox checkBox = new CheckBox(this.attribute.getName() + "=");
        checkBox.setOnDragDetected(this::dragDetected);
        checkBox.setOnDragDone(this::dragDone);
        checkBox.setSelected(true);
        //Label attrName = new Label(this.attribute.getName() + "=");
        checkBox.getStyleClass().add("label-attribute");
        this.getChildren().add(checkBox);
        //this.getChildren().add(attrName);
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
                tf.focusedProperty().addListener((observableValue, aBoolean, t1) -> onInputStart());
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
            tf.focusedProperty().addListener((observableValue, aBoolean, t1) -> onInputStart());
            HBox.setHgrow(tf, Priority.ALWAYS);
            tf.setPromptText("Schemas.SCHEMA_NAME.Tables.TABLE_NAME");
            this.getChildren().add(tf);
        } else if (attribute.getType().equalsIgnoreCase(JSON.getType())) {
            TextArea ta = new TextArea("{\n}");
            ta.focusedProperty().addListener((observableValue, aBoolean, t1) -> onInputStart());
            setMinHeight(150);
            HBox.setHgrow(ta, Priority.ALWAYS);
            this.getChildren().add(ta);
        } else if (attribute.getType().equalsIgnoreCase(FILE_PATH.getType())
                || attribute.getType().equalsIgnoreCase(FOLDER_PATH.getType())) {
            TextField tf = new TextField();
            tf.focusedProperty().addListener((observableValue, aBoolean, t1) -> onInputStart());
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
                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        tf.setText(file.getAbsolutePath());
                        onInputStart();
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
                        onInputStart();
                    }
                });
            }
        } else {
            TextField tf = new TextField();
            tf.focusedProperty().addListener((observableValue, aBoolean, t1) -> onInputStart());
            HBox.setHgrow(tf, Priority.ALWAYS);
            this.getChildren().add(tf);
        }
    }

    public void dragDetected(MouseEvent event) {
        String attributeValueString = getAttributeString();
        String attributeNameLabel = attribute.getName();
        LogUtils.info(DRAG_DETECTED + attributeNameLabel + "=" + attributeValueString);
        if (attributeValueString.isEmpty() || attributeValueString.matches("[{]\n*[}]")) {
            this.setStyle("-fx-background-color: #FFCCCC;");
            LogUtils.error(ATTR_IS_NOT_FILLED);
            return;
        }

        Dragboard dragboard = this.startDragAndDrop(TransferMode.COPY_OR_MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString(attributeNameLabel + "=" + attributeValueString);
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

    public void setOnMouseClick(EventHandler event) {
        this.getChildren().get(0).setOnMouseClicked(event);
    }

    public String getAttributeString() {
        String attributeValueString = "";
        ObservableList<Node> list = this.getChildren();
        if (list.get(1) instanceof Label) attributeValueString = ((Label) list.get(1)).getText();
        else if (list.get(1) instanceof TextField) attributeValueString = ((TextField) list.get(1)).getText();
        else if (list.get(1) instanceof TextArea) {
            attributeValueString = ((TextArea) list.get(1)).getText();
            return attributeValueString;
        } else if (list.get(1) instanceof ComboBox)
            attributeValueString = ((ComboBox) list.get(1)).getValue().toString();
        return attributeValueString;
    }

    public String getAttributeName() {
        return attribute.getName();
    }

    public void onInputStart() {
        this.setStyle("");
    }
}


