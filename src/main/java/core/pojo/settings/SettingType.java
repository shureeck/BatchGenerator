package core.pojo.settings;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class SettingType {
    @JsonProperty ("type")
    private String type;
    @JsonProperty("settings")
    private ArrayList<Setting> settings;

    public ArrayList<Setting> getSettings() {
        return settings;
    }

    public void setSettings(ArrayList<Setting> settings) {
        this.settings = settings;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
