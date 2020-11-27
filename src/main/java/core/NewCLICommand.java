package core;

import java.util.ArrayList;

public class NewCLICommand {
    private String name;
    private ArrayList<String> attributes;

    public NewCLICommand(String name) {
        this.name = name;
        attributes = new ArrayList<>();
    }

    public void setAttribute(String attribute) {
        attributes.add(attribute);
    }

    public String toString() {
        StringBuilder command = new StringBuilder(name + "\n");
        for (String attr : attributes) {
            command.append("\t" + attr + "\n");
        }
        command.append("/");
        return command.toString();
    }

    public ArrayList<String> getAttributes() {
        return attributes;
    }
}
