package gui;

import core.Main;
import core.NewCLICommand;
import core.YAMLReader;
import core.pojo.Command;
import core.pojo.Group;
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
import java.util.Objects;

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
    private Button up = new Button();
    @FXML
    private Button down = new Button();
    @FXML
    private Button edit = new Button();
    @FXML
    private Button remove = new Button();


    @FXML
    private void initialize() {
        sctsView.setSelected(true);
        tabPane.getTabs().clear();
        SplitPane.Divider divider = splitPaneA.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldvalue, newvalue) -> divider.setPosition(0.7));
        File old = new File(Main.class.getClassLoader().getResource("cli.yaml").getPath());

        File newCli = new File(Main.class.getClassLoader().getResource("new_cli.yaml").getPath());

        yamlOld = new YAMLReader(old, Group[].class);
        yamlNew = new YAMLReader(newCli, Group[].class);
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
        remove.setDisable(true);
        up.setDisable(true);
        down.setDisable(true);
        edit.setDisable(true);
    }

    private GridPane controlBlockCreation(Object object) {
        if (sctsView.isSelected()) {
            NewCLICommand command = ((NewCLICommand) object);
        }
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

        if (sctsView.isSelected()) {
            NewCLICommand command = ((NewCLICommand) object);
            remove.setOnAction(actionEvent -> onRemoveClick(command));
            up.setOnAction(actionEvent -> MainGuiController.this.onMoveCommand(-1, command));
            down.setOnAction(actionEvent -> onMoveCommand(1, command));
            edit.setOnAction(actionEvent -> onEditClick(command));
        } else {
            Node command = (Node) object;
            edit.setOnAction(actionEvent -> onEditClick(command));
            remove.setOnAction(actionEvent -> onRemoveClick(command));
            down.setOnAction(actionEvent -> onMoveCommand(1, command));
            up.setOnAction(actionEvent -> onMoveCommand(-1, command));
        }
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
                if (newCLIScript == null) newCLIScript = new ArrayList<>();
                save.setDisable(false);
                NewCLICommand newCliCommand = controller.getNewCLICommand();
                newCLIScript.add(newCliCommand);
                LogUtils.info(String.format(NEW_COMMAND_ADDED, command.getName()));
                writeScript();
            } else {
                if (document == null) initDocument();
                save.setDisable(false);
                rootNode.appendChild(document.importNode(controller.getCommandNode(), true));
                writeScript();

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
            if (document == null) save.setDisable(true);
            else {
                save.setDisable(false);
            }
        } else {
            LogUtils.info(VIEW_CHANGED + "SCTS View");
            tabPane.setVisible(false);
            newTabPane.setVisible(true);
            if (newCLIScript == null) save.setDisable(true);
            else {
                save.setDisable(false);
            }
        }
        writeScript();
    }

    public void writeScript() {
        contentVbox.getChildren().clear();
        if (sctsView.isSelected()) {
            if (newCLIScript != null) {
                for (NewCLICommand tmp : newCLIScript) {
                    fillContentVbox(tmp.toString(), tmp);
                }
            }
        } else {
            if (document != null) {
                fillContentVbox("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", null);
                fillContentVbox("<tree>", null);
                fillContentVbox("\t<instances>", null);
                fillContentVbox("\t\t<BatchJob>", null);
                for (int i = 0; i < rootNode.getChildNodes().getLength(); i++) {
                    if (rootNode.getChildNodes().item(i) instanceof Element) {
                        String xmlString = nodeToString(rootNode.getChildNodes().item(i), true);
                        fillContentVbox("\t\t\t" + xmlString, rootNode.getChildNodes().item(i));
                    }
                }
                fillContentVbox("\t\t</BatchJob>", null);
                fillContentVbox("\t</instances>", null);
                fillContentVbox("</tree>", null);
            }
        }
    }

    public void fillContentVbox(String string, Object node) {
        HBox hb = new HBox();
        hb.getStyleClass().add("hbox-focused");
        hb.setAlignment(Pos.CENTER);
        HBox.setHgrow(hb, Priority.ALWAYS);
        TextFlow tf = new TextFlow();
        HBox.setHgrow(tf, Priority.ALWAYS);
        tf.maxWidth(Double.MAX_VALUE);
        tf.getStyleClass().add("text-flow");
        Text text = new Text(string);
        text.getStyleClass().add("text-edit");
        tf.getChildren().add(text);
        hb.getChildren().add(tf);
        contentVbox.getChildren().add(hb);
        contentVbox.requestLayout();

        hb.setOnMouseClicked(event -> {
            ((HBox) event.getSource()).requestFocus();
            remove.setDisable(false);
            up.setDisable(false);
            down.setDisable(false);
            edit.setDisable(false);
            remove.setOnAction(actionEvent -> {
                if (node instanceof NewCLICommand) {
                    onRemoveClick((NewCLICommand) node);
                } else onRemoveClick((Node) node);
            });
            edit.setOnAction(actionEvent -> MainGuiController.this.onEditClick(node));
            up.setOnAction(actionEvent -> {
                if (node instanceof NewCLICommand) {
                    onMoveCommand(-1, (NewCLICommand) node);
                } else onMoveCommand(-1, (Node) node);
            });
            down.setOnAction(actionEvent -> {
                if (node instanceof NewCLICommand) {
                    onMoveCommand(1, (NewCLICommand) node);
                } else onMoveCommand(1, (Node) node);
            });
            event.consume();
        });

    }


    public void onRemoveClick(NewCLICommand newCLICommand) {
        newCLIScript.remove(newCLICommand);
        if (newCLIScript.size() == 0) {
            newCLIScript = null;
            save.setDisable(true);
        }
        writeScript();
        remove.setDisable(true);
        up.setDisable(true);
        down.setDisable(true);
        edit.setDisable(true);

    }

    public void onRemoveClick(Node node) {
        rootNode.removeChild(node);
        if (!rootNode.hasChildNodes()) {
            document = null;
            save.setDisable(true);
        }
        writeScript();
        remove.setDisable(true);
        up.setDisable(true);
        down.setDisable(true);
        edit.setDisable(true);
    }

    private void onEditClick(Object object) {
        Command command = null;
        if (object instanceof NewCLICommand) {
            for (Group group : yamlNew.getGroupList()) {
                command = group.getCommands().stream().filter((p) -> (p.getName().equals(((NewCLICommand) object).getName()))).
                        findFirst().orElse(null);
                if (command != null) break;
            }
        } else {
            for (Group group : yamlOld.getGroupList()) {
                command = group.getCommands().stream().filter((p) -> (p.getName().equals(((Node) object).getNodeName()))).
                        findFirst().orElse(null);
                if (command != null) break;
            }
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
        controller.setCommandForEdit(object);
        Parent root = loader.getRoot();
        stage.setTitle("Command builder");
        stage.setScene(new Scene(root));
        stage.resizableProperty().setValue(true);
        stage.getScene().getStylesheets().add((getClass().getResource("/style.css")).toExternalForm());
        stage.initModality(APPLICATION_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
        if (controller.isAddButtonClick()) {
            if (object instanceof NewCLICommand) {
                int id = newCLIScript.indexOf(object);
                newCLIScript.remove(id);
                newCLIScript.add(id, controller.getNewCLICommand());

            } else {
                rootNode.replaceChild(document.importNode(controller.getCommandNode(), true), (Node) object);
            }
        }
        writeScript();
        remove.setDisable(true);
        up.setDisable(true);
        down.setDisable(true);
        edit.setDisable(true);
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
        contentVbox.getChildren().get(newCLIScript.indexOf(newCLICommand)).requestFocus();
    }

    public void onMoveCommand(int i, Node node) {
        int idx = 0;
        for (int j = 0; j < rootNode.getChildNodes().getLength(); j++) {
            if (rootNode.getChildNodes().item(j).equals(node)) {
                idx = j;
            }
        }
        if (i > 0) {
            if (idx == rootNode.getChildNodes().getLength() - 1) return;
            rootNode.removeChild(node);
            rootNode.insertBefore(node, rootNode.getChildNodes().item(idx + 1));
        } else {
            if (idx == 0) return;
            rootNode.removeChild(node);
            rootNode.insertBefore(node, rootNode.getChildNodes().item(idx - 1));
        }
        writeScript();

        for (int j = 0; j < rootNode.getChildNodes().getLength(); j++) {
            if (rootNode.getChildNodes().item(j).equals(node)) {
                contentVbox.getChildren().get(j + 4).requestFocus();
                break;
            }

        }
    }
}
