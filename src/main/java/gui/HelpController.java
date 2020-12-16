package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;

public class HelpController {

    @FXML
    private WebView webView = new WebView();
    @FXML
    private Button cancelBtn = new Button();

    @FXML
    private void initialize() {
        cancelBtn.setOnAction(actionEvent -> onCancelClick());

    }

    public void onCancelClick() {
        webView.getScene().getWindow().hide();
    }

    public void setHelpPage(String url) {
        System.out.println(url);
        webView.getEngine().load(url);
        webView.requestLayout();
    }
}
