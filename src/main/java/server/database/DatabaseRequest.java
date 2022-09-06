package server.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class DatabaseRequest {
    @Expose private String type;
    @Expose private JsonElement key;
    @Expose private JsonElement value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonElement getKey() {
        return key;
    }

    public void setKey(JsonElement key) {
        this.key = key;
    }

    public JsonElement getValue() {
        return value;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }

    public String getJsonString() {
        return new Gson().toJson(this);
    }

    public boolean isExitRequest() {
        return "exit".equals(type);
    }

}
