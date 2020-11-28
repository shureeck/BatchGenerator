package core;

public enum AttributeType {
    BOOLEAN("Boolean"),
    STRING("String"),
    SCHEMA_PATH("SchemaPath"),
    FILE_PATH("FilePath"),
    JSON("JSON"),
    FOLDER_PATH("FolderPath");

    String type;
   AttributeType(String type){
        this.type=type;
    }

    public String getType() {
        return type;
    }
}
