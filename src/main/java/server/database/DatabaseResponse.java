package server.database;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class DatabaseResponse {
    @Expose private String response;
    @Expose private String reason;
    @Expose private JsonElement value;
    private boolean isExit;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public JsonElement getValue() {
        return value;
    }

    public void setValue(JsonElement value) {
        this.value = value;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    public String getJsonString() {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this);
    }
}
