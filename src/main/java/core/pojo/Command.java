package core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Command {
    @JsonProperty("command")
    private String name;
    @JsonProperty("attributes")
    private ArrayList<Attribute> attributes;
    @JsonProperty("old")
    private String old;

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
}
