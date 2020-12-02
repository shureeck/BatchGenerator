package gui;

import core.Main;
import core.NewCLICommand;
import core.YAMLReader;
import core.pojo.Command;
import core.pojo.Group;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logger.LogUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

import static javafx.stage.Modality.APPLICATION_MODAL;
import static logger.LogMessages.*;

public class MainGuiController {

    private org.w3c.dom.Node rootNode;
    private org.w3c.dom.Document document;
    private ArrayList<NewCLICommand> newCLIScript;
    private YAMLReader yamlOld;
    private YAMLReader yamlNew;

    @FXML
    private TabPane tabPane = new TabPane();
    @FXML
    private TabPane newTabPane = new TabPane();
    @FXML
    private AnchorPane xmlAnchorPane = new AnchorPane();
    @FXML
    private TextArea printBatch = new TextArea();
    @FXML
    private SplitPane splitPaneA = new SplitPane();
    @FXML
    private Button save = new Button();
    @FXML
    private RadioMenuItem xmlView = new RadioMenuItem();
    @FXML
    private RadioMenuItem sctsView = new RadioMenuItem();
    @FXML
    private VBox contentVbox = new VBox();


    @FXML
    private void initialize() {
        sctsView.setSelected(true);
        tabPane.getTabs().clear();
        SplitPane.Divider divider = splitPaneA.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldvalue, newvalue) -> divider.setPosition(0.7));
        File old = new File(Main.class.getClassLoader().getResource("cli.yaml").getPath());

        File newCli = new File(Main.class.getClassLoader().getResource("new_cli.yaml").getPath());

        yamlOld = new YAMLReader(old);
        yamlNew = new YAMLReader(newCli);
        for (Group group : yamlOld.getGroupList()) {
            tabPane.getTabs().add(group.getTab(this));
        }
        for (Group group : yamlNew.getGroupList()) {
            newTabPane.getTabs().add(group.getTab(this));
        }

        if (xmlView.isSelected()) {
            tabPane.setVisible(true);
            newTabPane.setVisible(false);
        } else {
            newTabPane.setVisible(true);
            tabPane.setVisible(false);
        }
        printBatch.setEditable(false);
        if (document == null) save.setDisable(true);
        LogUtils.info(GUI_STARTED);
    }

    private GridPane controlBlockCreation(NewCLICommand newCLICommand) {
        GridPane grid = new GridPane();
        Button remove = new Button();
        Button edit = new Button();
        Button up = new Button();
        Button down = new Button();

        remove.getStyleClass().add("remove-btn");
        edit.getStyleClass().add("edit-btn");
        up.getStyleClass().add("up-btn");
        down.getStyleClass().add("down-btn");

        grid.add(remove, 0, 0);
        grid.add(edit, 0, 1);
        grid.add(up, 1, 0);
        grid.add(down, 1, 1);
        grid.setMaxWidth(44);
        grid.setMaxHeight(44);

        remove.setOnAction(actionEvent -> onRemoveClick(newCLICommand));
        up.setOnAction(actionEvent -> MainGuiController.this.onMoveCommand(-1, newCLICommand));
        down.setOnAction(actionEvent -> onMoveCommand(1, newCLICommand));
        edit.setOnAction(actionEvent -> onEditClick(newCLICommand));
        return grid;
    }

    @FXML
    public void onCommandClick(Command command) {
        LogUtils.info(CLICKED_ON_COMMAND + command.getName());
        BuilderController controller;
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/bilder.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
        controller.setNewCLI(sctsView.isSelected());
        controller.setCommand(command);
        Parent root = loader.getRoot();
        stage.setTitle("Command builder");
        stage.setScene(new Scene(root));
        stage.resizableProperty().setValue(true);
        stage.getScene().getStylesheets().add((getClass().getResource("/style.css")).toExternalForm());
        stage.initModality(APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
        if (controller.isAddButtonClick()) {
            if (sctsView.isSelected()) {
                printBatch.clear();
                contentVbox.getChildren().clear();

                if (newCLIScript == null) newCLIScript = new ArrayList<>();
                save.setDisable(false);
                NewCLICommand newCliCommand = controller.getNewCLICommand();
                newCLIScript.add(newCliCommand);
                LogUtils.info(String.format(NEW_COMMAND_ADDED, command.getName()));
                writeScript();
            } else {
                if (document == null) initDocument();
                save.setDisable(false);
                setCommandNode(controller.getCommandNode());
                LogUtils.info(String.format(NEW_COMMAND_ADDED, command.getName()));
            }
        }
    }

    private String nodeToString(Node node, boolean omitXMLDecalration) {
        String xmlString = "";
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(node);
            transformer.transform(source, result);
            xmlString = result.getWriter().toString();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return xmlString;
    }

    @FXML
    public void setCommandNode(org.w3c.dom.Node node) {
        rootNode.appendChild(document.importNode(node, true));
        contentVbox.getChildren().clear();
        for (int i = 0; i < rootNode.getChildNodes().getLength(); i++) {
            if (rootNode.getChildNodes().item(i) instanceof Element) {
                String xmlString = nodeToString(rootNode.getChildNodes().item(i), true);
                TextFlow tf = new TextFlow();
                Text text = new Text(xmlString);
                tf.getStyleClass().add("text-flow");
                text.getStyleClass().add("text-edit");

                tf.getChildren().add(text);

                contentVbox.getChildren().add(tf);
                contentVbox.requestLayout();
            }

        }

    }

    private void initDocument() {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = dBuilder.newDocument();
        org.w3c.dom.Node tree = document.createElement("tree");
        org.w3c.dom.Node instances = document.createElement("instances");
        rootNode = document.createElement("BatchJob");

        document.appendChild(tree);
        tree.appendChild(instances);
        instances.appendChild(rootNode);
    }

    @FXML
    private void onCancelClick() {
        LogUtils.info(APP_CLOSED);
        System.exit(0);
    }

    @FXML
    private void onSaveClick() {
        LogUtils.info(SAVE_BUTTON_CLICKED);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        if (sctsView.isSelected()) {
            fileChooser.setInitialFileName("batch.scts");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SCTS", "*.scts"));
        } else {
            fileChooser.setInitialFileName("batch.xml");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        }
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        File file = fileChooser.showSaveDialog(stage);
        if (sctsView.isSelected()) {
            if (file != null && newCLIScript != null) {
                StringBuilder builder = new StringBuilder();
                for (NewCLICommand command : newCLIScript) {
                    builder.append(command.toString() + "\n");
                }
                try {
                    Files.write(file.toPath(), builder.toString().getBytes());
                } catch (IOException e) {
                    LogUtils.error(e.getMessage());
                    LogUtils.error(e.getStackTrace());
                    e.printStackTrace();
                }
            }
        } else {
            if (file != null && document != null) {
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    StreamResult result = new StreamResult(file);
                    DOMSource source = new DOMSource(document);
                    transformer.transform(source, result);
                } catch (TransformerException e) {
                    LogUtils.error(e.getMessage());
                    LogUtils.error(e.getStackTrace());
                    e.printStackTrace();
                }
            }
        }
        if (file != null) LogUtils.info(String.format(FILE_SAVED, file.getAbsolutePath()));
        else LogUtils.info(SAVE_DIALOG_CANCELED);
    }

    @FXML
    public void onVieMenuItemClick() {
        if (xmlView.isSelected()) {
            LogUtils.info(VIEW_CHANGED + "XML View");
            tabPane.setVisible(true);
            newTabPane.setVisible(false);
            printBatch.clear();
            if (document == null) save.setDisable(true);
            else {
                save.setDisable(false);

                String xmlString = "";
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    StreamResult result = new StreamResult(new StringWriter());
                    DOMSource source = new DOMSource(document);
                    transformer.transform(source, result);
                    xmlString = result.getWriter().toString();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
                printBatch.setText(xmlString);
            }
        } else {
            LogUtils.info(VIEW_CHANGED + "SCTS View");
            tabPane.setVisible(false);
            newTabPane.setVisible(true);
            printBatch.clear();
            if (newCLIScript == null) save.setDisable(true);
            else {
                save.setDisable(false);
                for (NewCLICommand tmp : newCLIScript) {
                    printBatch.appendText(tmp.toString() + "\n");
                }
            }
        }
    }

    public void writeScript() {
        if (sctsView.isSelected()) {
            printBatch.clear();
            contentVbox.getChildren().clear();

            if (newCLIScript == null) newCLIScript = new ArrayList<>();
            save.setDisable(false);

            for (NewCLICommand tmp : newCLIScript) {
                HBox hb = new HBox();
           //     hb.setStyle("-fx-background-color: white; -fx-border-color: #C0C0C0; -fx-border-width: 0 0 1 0;");
                hb.setAlignment(Pos.CENTER);
                HBox.setHgrow(hb, Priority.ALWAYS);
                TextFlow tf = new TextFlow();
                HBox.setHgrow(tf, Priority.ALWAYS);
                tf.maxWidth(Double.MAX_VALUE);
                tf.getStyleClass().add("text-flow");
                Text text = new Text(tmp.toString());
                text.getStyleClass().add("text-edit");
                tf.getChildren().add(text);
                hb.getChildren().add(tf);
                hb.getChildren().add(controlBlockCreation(tmp));
                contentVbox.getChildren().add(hb);
                contentVbox.requestLayout();
            }

        }
    }

    public void onRemoveClick(NewCLICommand newCLICommand) {
        newCLIScript.remove(newCLICommand);
        if (newCLIScript.size() == 0) {
            newCLIScript = null;
            save.setDisable(true);
        }
        writeScript();
    }

    private void onEditClick(NewCLICommand newCLICommand) {
        Command command = null;
        for (Group group : yamlNew.getGroupList()) {
            command = group.getCommands().stream().filter((p) -> (p.getName().equals(newCLICommand.getName()))).findFirst().orElse(null);
            if (command != null) break;
        }
        BuilderController controller;
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/bilder.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
        controller.setNewCLI(sctsView.isSelected());
        controller.setCommand(command);
        controller.setCommandForEdit(newCLICommand);
        Parent root = loader.getRoot();
        stage.setTitle("Command builder");
        stage.setScene(new Scene(root));
        stage.resizableProperty().setValue(true);
        stage.getScene().getStylesheets().add((getClass().getResource("/style.css")).toExternalForm());
        stage.initModality(APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
        if (controller.isAddButtonClick()) {
            int id = newCLIScript.indexOf(newCLICommand);
            newCLIScript.remove(id);
            newCLIScript.add(id, controller.getNewCLICommand());
        }
        writeScript();
    }


    public void onMoveCommand(int i, NewCLICommand newCLICommand) {
        int idx = newCLIScript.indexOf(newCLICommand);
        if (i > 0) {
            if ((idx + 1) < newCLIScript.size()) {
                newCLIScript.remove(newCLICommand);
                newCLIScript.add((idx + 1), newCLICommand);
            }
        } else {
            if ((idx - 1) >= 0) {
                newCLIScript.remove(newCLICommand);
                newCLIScript.add((idx - 1), newCLICommand);
            }
        }
        writeScript();
    }
}
