package gui;

import core.NewCLICommand;
import core.pojo.Attribute;
import core.pojo.Command;
import gui.fxml.AttributeContainer;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Objects;

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
    }

    public void setCommand(Command command) {
        this.command = command;
        commandName.setText(command.getName());
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
            attributeListVbox.getChildren().get(i).setOnMouseClicked(this::doubleClick);
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
        Label name = new Label(string.split("=", 2)[0]);
        Label label = new Label(string.split("=", 2)[1]);
        HBox hBox = new HBox();

        name.getStyleClass().add("label-attrname-command");
        label.getStyleClass().add("label-attr-command");
        hBox.setStyle("-fx-padding: 0 5 0 20;");

        hBox.getChildren().add(name);
        hBox.getChildren().add(new Label("="));
        hBox.getChildren().add(label);

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
        ((Element) commandNode).setAttribute(name.getText(), label.getText().substring(1, (label.getText().length() - 1)));
    }

    public void fillNewCommand(String string) {
        Label name = new Label("-" + string.split("=", 2)[0] + ":");
        Label label = new Label(string.split("=", 2)[1]);
        HBox hBox = new HBox();

        name.getStyleClass().add("label-command");
        label.getStyleClass().add("label-attr-command");
        hBox.setStyle("-fx-padding: 0 5 0 20;");

        hBox.getChildren().add(name);
        hBox.getChildren().add(label);

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
        newCLICommand.setAttribute(name.getText() + "= " + label.getText());
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
            event.setDropCompleted(true);
        } else {
            event.setDropCompleted(false);
        }
        event.consume();
        if (commandNode != null) {
            addBtn.setDisable(false);
        }
    }

    public void onAddButtonClick(Event event) {
        addButtonClick = true;
        onButtonCancelClick(event);
    }

    public void onButtonCancelClick(Event event) {
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
                AttributeContainer container = ((AttributeContainer) event.getSource());
                if (isNewCLI) {
                    fillNewCommand(container.getAttributeName() + container.getAttributeString());
                    if (newCLICommand != null) {
                        addBtn.setDisable(false);
                    }
                } else {
                    fillCommand(container.getAttributeName() + container.getAttributeString());
                    if (commandNode != null) {
                        addBtn.setDisable(false);
                    }
                }
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
}
