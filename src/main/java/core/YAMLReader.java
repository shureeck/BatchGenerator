package core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import core.pojo.Group;
import logger.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static logger.LogMessages.*;

public class YAMLReader {
    private ArrayList<Group> groupList;

    public YAMLReader(File file) {
        groupList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            groupList.addAll(Arrays.asList(mapper.readValue(file, Group[].class)));
        } catch (IOException e) {
            LogUtils.error(e.getMessage());
            LogUtils.error(e.getStackTrace());
            e.printStackTrace();
        }
        LogUtils.info(String.format(FILE_READ, file.getName()));
    }

    public ArrayList<Group> getGroupList() {
        return groupList;
    }
}
