package gui.fxml;

import core.pojo.Command;
import gui.BuilderController;
import gui.MainGuiController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class TabControl extends Tab {

    private ArrayList<Command> commands;

    public TabControl(String title) {
        Label label = new Label(title);
        label.setRotate(90);
        StackPane stackPane = new StackPane(new Group(label));
        stackPane.setRotate(90);
        setGraphic(stackPane);
    }

    public void setCommands(ArrayList<Command> commands, MainGuiController controller) {
        VBox vBox = new VBox();
        ScrollPane scrollPane = new ScrollPane();
        this.commands = commands;
        for (Command command : commands) {
            Hyperlink hyperlink = new Hyperlink(command.getName());
            hyperlink.setOnAction(actionEvent -> controller.onCommandClick(command));
            vBox.getChildren().add(hyperlink);
        }
        scrollPane.setContent(vBox);
        setContent(scrollPane);
    }
}
