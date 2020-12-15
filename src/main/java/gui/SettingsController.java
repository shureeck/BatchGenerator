package gui;

import core.Main;
import core.YAMLReader;
import core.pojo.settings.Setting;
import core.pojo.settings.SettingType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import logger.LogUtils;

import java.io.File;
import java.util.ArrayList;

import static logger.LogMessages.*;

public class SettingsController {

    @FXML
    private TextArea textArea;
    @FXML
    private TabPane tabPane = new TabPane();

    @FXML
    public void initialize() {
        tabPane.getStyleClass().add("tab-pane-setting");
        File file = new File(Main.class.getClassLoader().getResource("settings.yaml").getPath());
        YAMLReader reader = new YAMLReader(file, SettingType[].class);
        ArrayList<SettingType> settingTypes = reader.getSettingTypes();

        for (int i = 0; i < settingTypes.size(); i++) {

            ScrollPane scrollPane = new ScrollPane();
            Tab tab = new Tab(settingTypes.get(i).getType() + " settings", scrollPane);
            TableView<Setting> settingTableView = new TableView<>();
            scrollPane.setFitToHeight(true);
            scrollPane.setFitToWidth(true);
            VBox.setVgrow(scrollPane, Priority.ALWAYS);

            TableColumn<Setting, String> descriptionColumn = new TableColumn<>("Description");
            TableColumn<Setting, String> settingColumn = new TableColumn<>("Setting");
            TableColumn<Setting, HBox> valueColumn = new TableColumn<>("Value");
            descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
            settingColumn.setCellValueFactory(new PropertyValueFactory<>("setting"));
            valueColumn.setCellValueFactory(new PropertyValueFactory<>("valueHbox"));

            settingTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            descriptionColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25);
            settingColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25);
            valueColumn.setMaxWidth(1f * Integer.MAX_VALUE * 50);

            settingTableView.getColumns().add(descriptionColumn);
            settingTableView.getColumns().add(settingColumn);
            settingTableView.getColumns().add(valueColumn);

            if (settingTypes.get(i).getSettings() != null) {
                settingTableView.setItems(FXCollections.observableArrayList(settingTypes.get(i).getSettings()));
            } else {
                LogUtils.error(String.format(NO_SETTINGS, settingTypes.get(i).getType()));
            }
            scrollPane.setContent(settingTableView);
            tabPane.getTabs().add(tab);
            LogUtils.info(String.format(TAB_ADDED, settingTypes.get(i).getType()));
        }
        tabPane.requestLayout();
        LogUtils.info(SETTINGS_LIST_SHOWN);
    }

    @FXML
    private void onCancelClick() {
        LogUtils.info(CANCEL_BUTTON_CLICKED);
        tabPane.getScene().getWindow().hide();
    }

    public void setTextArea(TextArea area) {
        textArea = area;
    }

    @FXML
    private void onAddButtonClick() {
        String settingTemplate = "\"%s\":\"%s\"";
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        TableView<Setting> tableView = (TableView<Setting>) ((ScrollPane) tab.getContent()).getContent();
        Setting setting = tableView.getSelectionModel().getSelectedItem();
        if (setting != null) {
            String text = textArea.getText();
            text = text.substring(2, text.length() - 2);
            if (!text.trim().isEmpty()) {
                text = text.trim() + ",\n" + String.format(settingTemplate, setting.getSetting(),
                        setting.getCurrentValue().replace("\\", "\\\\"));
            } else {
                text = String.format(settingTemplate, setting.getSetting(),
                        setting.getCurrentValue().replace("\\", "\\\\"));
            }
            textArea.setText("'{\n" + text + "\n}'");
            LogUtils.info(String.format(SETTING_ADDED, String.format(settingTemplate, setting.getSetting(),
                    setting.getCurrentValue().replace("\\", "\\\\"))));
            textArea.requestLayout();
        } else {
            LogUtils.error(NO_SELECTION);
        }
    }

}
