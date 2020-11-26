package core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gui.fxml.AttributeContainer;
import java.util.ArrayList;

public class Attribute {
    @JsonProperty("attribute")
    private String name;
    @JsonProperty("require")
    private boolean require = true;
    @JsonProperty("type")
    private String type;
    @JsonProperty("values")
    private ArrayList<String> values;
    @JsonProperty("old")
    private String old;
    @JsonIgnore
    private AttributeContainer attributeContainer;

    public Attribute(String name, boolean require, String type, ArrayList<String> values) {
        this.name = name;
        this.require = require;
        this.type = type;
        this.values = values;
    }

    public Attribute() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public AttributeContainer getAttributeContainer() {
        attributeContainer = new AttributeContainer(this);
        return attributeContainer;
    }

    public String getOld() {
        return old;
    }

    public void setOld(String old) {
        this.old = old;
    }
}
