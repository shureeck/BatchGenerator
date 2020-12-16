package core.pojo;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Main;
import gui.GUIStart;
import gui.HelpController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static javafx.stage.Modality.APPLICATION_MODAL;

public class Command {
    @JsonProperty("command")
    private String name;
    @JsonProperty("attributes")
    private ArrayList<Attribute> attributes;
    @JsonProperty("old")
    private String old;
    @JsonProperty("help")
    private String help;

    public Command(String name, ArrayList<Attribute> attributes) {
        this.attributes = attributes;
        this.name = name;
    }

    public Command() {
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }

    public String getHelp() {
        if (help == null) return null;
        String webPage = new File(Main.class.getClassLoader().getResource("CLI_Improvements.htm").getPath())
                .getAbsolutePath().replace("\\", "/");
        return "file://" + webPage + help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void onHelpClick() {
        HelpController controller;
        Stage stage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUIStart.class.getResource("fxml/helpGui.fxml"));
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        controller = loader.getController();
        controller.setHelpPage(getHelp());
        Parent root = loader.getRoot();
        stage.setTitle("Help");
        stage.setScene(new Scene(root));
        stage.resizableProperty().setValue(true);
        stage.getScene().getStylesheets().add((getClass().getResource("/style.css")).toExternalForm());
        stage.initModality(APPLICATION_MODAL);
        stage.setResizable(true);
        stage.setHeight(600);
        stage.setWidth(800);
        stage.showAndWait();
    }
}
