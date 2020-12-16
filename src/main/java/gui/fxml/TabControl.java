package gui.fxml;

import core.NewCLICommand;
import core.pojo.Command;
import gui.BuilderController;
import gui.HelpController;
import gui.MainGuiController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import logger.LogUtils;

import java.io.IOException;
import java.util.ArrayList;

import static javafx.stage.Modality.APPLICATION_MODAL;
import static logger.LogMessages.*;

public class TabControl extends Tab {

    private ArrayList<Command> commands;

    public TabControl(String title) {
        setText(title);
        LogUtils.info(String.format(NEW_TAB_CREATED, title));
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
            HBox hBox = new HBox();
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            HBox.setHgrow(hBox, Priority.ALWAYS);
            LogUtils.info(String.format(COMMAND_ADDED, command.getName()));
            Hyperlink hyperlink = new Hyperlink(command.getName());
            hyperlink.setOnAction(actionEvent -> controller.onCommandClick(command));
            hBox.getChildren().add(hyperlink);
            if (command.getHelp() != null) {
                Button help = new Button();
                help.getStyleClass().add("help-btn");
                help.setMaxSize(20, 20);
                help.setMinSize(20, 20);
                help.setOnAction(actionEvent -> command.onHelpClick());
                HBox tmpHbox = new HBox(help);
                HBox.setHgrow(tmpHbox, Priority.ALWAYS);
                tmpHbox.setAlignment(Pos.CENTER_RIGHT);
                hBox.getChildren().add(tmpHbox);
            }
            vBox.getChildren().add(hBox);
        }
        scrollPane.setContent(vBox);
        setContent(scrollPane);
    }

}
