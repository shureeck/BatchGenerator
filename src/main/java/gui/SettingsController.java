package gui;

import core.Main;
import core.YAMLReader;
import core.pojo.settings.Setting;
import core.pojo.settings.SettingType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import logger.LogUtils;

import java.io.File;

import static logger.LogMessages.CANCEL_BUTTON_CLICKED;

public class SettingsController {
    private boolean addButtonClick = false;
    private ObservableList<SettingType> settingTypes;
    @FXML
    private TableView<Setting> settingTable;
    @FXML
    private TableColumn<Setting, String> descriptionColumn;
    @FXML
    private TableColumn<Setting, String> settingColumn;
    @FXML
    private TableColumn<Setting, HBox> valueColumn;
    @FXML
    private TextArea textArea;

    public boolean isAddButtonClick() {
        return addButtonClick;
    }

    @FXML
    public void initialize() {
        File file = new File(Main.class.getClassLoader().getResource("settings.yaml").getPath());
        YAMLReader reader = new YAMLReader(file, SettingType[].class);
        settingTypes = FXCollections.observableArrayList(reader.getSettingTypes());
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<Setting, String>("description"));
        settingColumn.setCellValueFactory(new PropertyValueFactory<Setting, String>("setting"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<Setting, HBox>("valueHbox"));
        settingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        descriptionColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25);
        settingColumn.setMaxWidth(1f * Integer.MAX_VALUE * 25);
        valueColumn.setMaxWidth(1f * Integer.MAX_VALUE * 50);
        settingTable.setItems(FXCollections.observableArrayList(settingTypes.get(0).getSettings()));
    }

    @FXML
    private void onCancelClick() {
        LogUtils.info(CANCEL_BUTTON_CLICKED);
        settingTable.getScene().getWindow().hide();
    }

    public void setTextArea(TextArea area) {
        textArea = area;
    }

    @FXML
    private void onAddButtonClick() {
        String settingTemplate = "\"%s\":\"%s\"";
        Setting setting = settingTable.getSelectionModel().getSelectedItem();
        String text = textArea.getText();
        text = text.substring(0, text.length() - 1) + String.format(settingTemplate, setting.getSetting(),
                setting.getCurrentValue().replace("\\", "\\\\")) + "\n}";
        textArea.setText(text);
        textArea.requestLayout();
    }

}
