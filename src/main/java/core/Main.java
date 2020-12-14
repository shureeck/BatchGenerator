package core;

import core.pojo.settings.SettingType;

import java.io.File;

public class Main {
    public static void main(String... args) {

        File file = new File(Main.class.getClassLoader().getResource("settings.yaml").getPath());
        YAMLReader reader = new YAMLReader(file, SettingType[].class);
        reader.getSettingTypes();
    }
}
