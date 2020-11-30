package gui;

import core.NewCLICommand;
import core.pojo.Attribute;
import core.pojo.Command;
import gui.fxml.AttributeContainer;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import logger.LogUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Objects;

import static logger.LogMessages.*;

public class BuilderController {
    private Command command;
    @FXML
    private VBox attributeListVbox = new VBox();
    @FXML
    private VBox commandVbox = new VBox();
    @FXML
    private Label commandName = new Label();
    @FXML
    private Button addBtn = new Button();
    @FXML
    private Button cancelBtn = new Button();
    @FXML
    private Button addChecked = new Button();

    private org.w3c.dom.Node commandNode;
    private NewCLICommand newCLICommand;
    private org.w3c.dom.Document document;
    private boolean addButtonClick = false;
    private boolean isNewCLI = false;

    @FXML

    private void initialize() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = dBuilder.newDocument();

        commandVbox.setOnDragOver(this::dragOver);
        commandVbox.setOnDragDropped(this::dragDropped);
        addBtn.setDisable(true);
        LogUtils.info(COMMAND_BUILDER_STARTED);
    }

    public void setCommand(Command command) {
        this.command = command;
        commandName.setText(command.getName());
        this.commandName.getStyleClass().add("command");
        if (Objects.nonNull(command.getAttributes())) {
            for (Attribute attribute : command.getAttributes()) {
                attributeListVbox.getChildren().add(attribute.getAttributeContainer());
            }
        } else {
            Label openTag;
            if (isNewCLI) {
                openTag = new Label(commandName.getText());
                openTag.getStyleClass().add("label-command-newcli");
                newCLICommand = new NewCLICommand(commandName.getText());
                if (newCLICommand != null) {
                    addBtn.setDisable(false);
                }
            } else {
                commandNode = document.createElement(commandName.getText());
                openTag = new Label("<" + commandName.getText() + " />");
                openTag.getStyleClass().add("label-command");
                if (commandNode != null) {
                    addBtn.setDisable(false);
                }
            }
            commandVbox.getChildren().add(openTag);
        }
        addBtn.setOnAction(this::onAddButtonClick);
        cancelBtn.setOnAction(this::onButtonCancelClick);
        for (int i = 0; i < attributeListVbox.getChildren().size(); i++) {
            ((AttributeContainer) attributeListVbox.getChildren().get(i)).getChildren().get(0)
                    .setOnMouseClicked(this::doubleClick);
        }
    }

    private void dragOver(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasString()) {
            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        }
        event.consume();
    }

    public void fillCommand(String string) {
        Button remove = new Button();
        HBox buttonHbox = new HBox(remove);
        HBox.setHgrow(buttonHbox, Priority.ALWAYS);
        buttonHbox.minWidth(Double.MAX_VALUE);
        buttonHbox.getStyleClass().add("btn-hbox");
        remove.getStyleClass().add("remove-btn");
        String value = string.split("=", 2)[1];
        Label name = new Label(string.split("=", 2)[0]);
        Label label = new Label("\"" + value + "\"");

        HBox hBox = new HBox();
        remove.setOnAction(actionEvent -> onRemoveClick(hBox));

        name.getStyleClass().add("label-attrname-command");
        label.getStyleClass().add("label-attr-command");
        hBox.setFillHeight(true);
        hBox.setStyle("-fx-padding: 0 5 0 20;");

        hBox.getChildren().add(name);
        hBox.getChildren().add(new Label("="));
        hBox.getChildren().add(label);
        hBox.getChildren().add(buttonHbox);


        if (commandVbox.getChildren().size() == 0) {
            Label openTag = new Label("<" + commandName.getText());
            Label closedTag = new Label("/>");

            openTag.getStyleClass().add("label-command");
            closedTag.getStyleClass().add("label-command");

            commandVbox.getChildren().add(openTag);
            commandVbox.getChildren().add(closedTag);
            commandNode = document.createElement(commandName.getText());
        }
        commandVbox.getChildren().add((commandVbox.getChildren().size() - 1), hBox);
        ((Element) commandNode).setAttribute(name.getText(), value);
    }

    public void fillNewCommand(String string) {
        Button remove = new Button();
        HBox buttonHbox = new HBox(remove);
        HBox.setHgrow(buttonHbox, Priority.ALWAYS);


        buttonHbox.getStyleClass().add("btn-hbox");
        remove.getStyleClass().add("remove-btn");

        Label name = new Label("-" + string.split("=", 2)[0] + ": ");
        String value = string.split("=", 2)[1].replace("\\", "\\\\");
        Label label = new Label(value.startsWith("{") && value.endsWith("}") ? value : "'" + value + "'");
        HBox hBox = new HBox();
        remove.setOnAction(actionEvent -> onRemoveClick(hBox));

        name.getStyleClass().add("label-command");
        label.getStyleClass().add("label-attr-command");
        hBox.setStyle("-fx-padding: 0 5 0 20;");

        hBox.getChildren().add(name);
        hBox.getChildren().add(label);
        hBox.getChildren().add(buttonHbox);

        if (commandVbox.getChildren().size() == 0) {
            Label openTag = new Label(commandName.getText());
            Label closedTag = new Label("/");

            openTag.getStyleClass().add("label-command-newcli");
            closedTag.getStyleClass().add("label-command-newcli");

            commandVbox.getChildren().add(openTag);
            commandVbox.getChildren().add(closedTag);
            newCLICommand = new NewCLICommand(commandName.getText());
        }
        commandVbox.getChildren().add((commandVbox.getChildren().size() - 1), hBox);
        newCLICommand.setAttribute(name.getText() + label.getText());
    }

    private void dragDropped(DragEvent event) {
        // Transfer the data to the target
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasString()) {
            if (isNewCLI) {
                fillNewCommand(dragboard.getString());
            } else {
                fillCommand(dragboard.getString());
            }
            LogUtils.info(String.format(ATTRIBUTE_ADDED, dragboard.getString()));
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
        if (isNewCLI) {
            if (newCLICommand != null) {
                addBtn.setDisable(false);
            }
        } else {
            if (commandNode != null) {
                addBtn.setDisable(false);
            }
        }
    }

    public void onAddButtonClick(Event event) {
        LogUtils.info(ADD_BUTTON_CLICKED);
        addButtonClick = true;
        onButtonCancelClick(event);
    }

    public void onButtonCancelClick(Event event) {
        if (!isAddButtonClick()) LogUtils.info(CANCEL_BUTTON_CLICKED);
        cancelBtn.getScene().getWindow().hide();
    }

    public boolean isAddButtonClick() {
        return addButtonClick;
    }

    public Node getCommandNode() {
        return commandNode;
    }

    public void doubleClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                AttributeContainer container = (AttributeContainer) ((CheckBox) event.getSource()).getParent();
                LogUtils.info(DOUBLE_CLICK_ON + container.getAttributeName());
                if (container.getAttributeString().isEmpty() || container.getAttributeString().matches("[{]\n*[}]")) {
                    container.setStyle("-fx-background-color: #FFCCCC;");
                    LogUtils.error(ATTR_IS_NOT_FILLED);
                    return;
                }
                if (isNewCLI) {
                    fillNewCommand(container.getAttributeName() + "=" + container.getAttributeString());
                    if (newCLICommand != null) {
                        addBtn.setDisable(false);
                    }
                } else {
                    fillCommand(container.getAttributeName() + "=" + container.getAttributeString());
                    if (commandNode != null) {
                        addBtn.setDisable(false);
                    }
                }
                LogUtils.info(String.format(ATTRIBUTE_ADDED, container.getAttributeName() + "=" + container.getAttributeString()));
                container.setDisable(true);

            }
        }
    }

    public void setNewCLI(boolean newCLI) {
        isNewCLI = newCLI;
    }

    public NewCLICommand getNewCLICommand() {
        return newCLICommand;
    }

    public void onRemoveClick(HBox hBox) {
        Label label = (Label) hBox.getChildren().get(0);
        LogUtils.info(String.format(REMOVE_ATTRIBUTE, label.getText()));
        if (isNewCLI) {
            for (String tmp : newCLICommand.getAttributes()) {
                if (tmp.split(":")[0].equals(label.getText().substring(0, label.getText().trim().length() - 1))) {
                    newCLICommand.getAttributes().remove(tmp);
                    break;
                }
            }
            if (newCLICommand.getAttributes().size() == 0) {
                newCLICommand = null;
                addBtn.setDisable(true);
            }
        } else {
            ((Element) commandNode).removeAttribute(label.getText());
            if (!commandNode.hasAttributes()) {
                commandNode = null;
                addBtn.setDisable(true);
            }
        }
        for (int i = 0; i < attributeListVbox.getChildren().size(); i++) {
            String attributeName = label.getText().trim().matches("-.+:")
                    ? label.getText().trim().substring(1, label.getText().trim().length() - 1) : label.getText();
            if (((AttributeContainer) attributeListVbox.getChildren().get(i)).getAttributeName().equals(attributeName)) {
                (attributeListVbox.getChildren().get(i)).setDisable(false);
            }
        }
        commandVbox.getChildren().remove(hBox);
        if (commandVbox.getChildren().size() == 2) commandVbox.getChildren().clear();
    }

    public void addChecked() {
        ObservableList<javafx.scene.Node> attributes = attributeListVbox.getChildren();
        for (int i = 0; i < attributes.size(); i++) {
            AttributeContainer container = (AttributeContainer) attributes.get(i);
            CheckBox chb = (CheckBox) container.getChildren().get(0);
            if (chb.isSelected() && !container.isDisabled()) {
                if (container.getAttributeString().isEmpty() || container.getAttributeString().matches("[{]\n*[}]")) {
                    container.setStyle("-fx-background-color: #FFCCCC;");
                    LogUtils.error(ATTR_IS_NOT_FILLED);
                } else {
                    if (isNewCLI) {
                        fillNewCommand(container.getAttributeName() + "=" + container.getAttributeString());
                        if (newCLICommand != null) {
                            addBtn.setDisable(false);
                        }
                    } else {
                        fillCommand(container.getAttributeName() + "=" + container.getAttributeString());
                        if (commandNode != null) {
                            addBtn.setDisable(false);
                        }
                    }
                    LogUtils.info(String.format(ATTRIBUTE_ADDED, container.getAttributeName() + "=" + container.getAttributeString()));
                    container.setDisable(true);
                }

            }
        }

    }

}
