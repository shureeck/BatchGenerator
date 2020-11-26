package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUIStart extends Application {
    private Stage primaryStage = new Stage();


    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("fxml/mainGui.fxml"));
        loader.load();
        Parent root = loader.getRoot();
        primaryStage.setTitle("Batch Generator");
        primaryStage.setScene(new Scene(root));
        primaryStage.resizableProperty().setValue(true);
        primaryStage.getScene().getStylesheets().add((getClass().getResource("/style.css")).toExternalForm());
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
