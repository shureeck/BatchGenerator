package gui;

import core.Main;
import core.NewCLICommand;
import core.YAMLReader;
import core.pojo.Command;
import core.pojo.Group;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

public class MainGuiController {

    private org.w3c.dom.Node rootNode;
    private org.w3c.dom.Document document;
    private ArrayList<NewCLICommand> newCLIScript;
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
    private void initialize() {
        tabPane.getTabs().clear();
        SplitPane.Divider divider = splitPaneA.getDividers().get(0);
        divider.positionProperty().addListener((observable, oldvalue, newvalue) -> divider.setPosition(0.7));
        File old = new File(Main.class.getClassLoader().getResource("cli.yaml").getPath());

        File newCli = new File(Main.class.getClassLoader().getResource("new_cli.yaml").getPath());

        YAMLReader yamlOld = new YAMLReader(old);
        YAMLReader yamlNew = new YAMLReader(newCli);
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
    }

    @FXML
    public void onCommandClick(Command command) {
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
                if (newCLIScript == null) newCLIScript = new ArrayList<>();
                save.setDisable(false);
                newCLIScript.add(controller.getNewCLICommand());
                for (NewCLICommand tmp : newCLIScript) {
                    printBatch.appendText(tmp.toString() + "\n");
                }
            } else {
                if (document == null) initDocument();
                save.setDisable(false);
                setCommandNode(controller.getCommandNode());
            }
        }
    }

    @FXML
    public void setCommandNode(org.w3c.dom.Node node) {
        rootNode.appendChild(document.importNode(node, true));
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
        System.exit(0);
    }

    @FXML
    private void onSaveClick() {
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
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    public void onVieMenuItemClick() {
        if (xmlView.isSelected()) {
            tabPane.setVisible(true);
            newTabPane.setVisible(false);
            if (document == null) save.setDisable(true);
            else save.setDisable(false);
        } else {
            tabPane.setVisible(false);
            newTabPane.setVisible(true);
            if (newCLIScript == null) save.setDisable(true);
            else save.setDisable(false);
        }
    }
}
