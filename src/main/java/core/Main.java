package core;

import java.io.File;

public class Main {
    public static void main(String... args) {

        File file = new File(Main.class.getClassLoader().getResource("new_cli.yaml").getPath());
        YAMLReader reader = new YAMLReader(file);
        reader.getGroupList();
    }
}
