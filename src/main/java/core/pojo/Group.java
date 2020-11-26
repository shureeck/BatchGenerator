package core.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import gui.MainGuiController;
import gui.fxml.TabControl;

import java.util.ArrayList;


public class Group {
    @JsonProperty("group")
    private String name;
    @JsonProperty("commands")
    private ArrayList<Command> commands;
    @JsonIgnore
    private TabControl tab;

    public Group(String name, ArrayList<Command> command) {
        this.name = name;
        this.commands = command;
        this.tab = new TabControl(name);
    }

    public Group() {
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<Command> commands) {
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TabControl getTab(MainGuiController controller) {
        this.tab = new TabControl(name);
        this.tab.setCommands(commands, controller);
        return tab;
    }
}
