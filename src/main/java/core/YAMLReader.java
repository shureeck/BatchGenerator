package core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import core.pojo.Group;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class YAMLReader {
    private ArrayList<Group> groupList;

    public YAMLReader(File file) {
        groupList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            groupList.addAll(Arrays.asList(mapper.readValue(file, Group[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Group> getGroupList() {
        return groupList;
    }
}
