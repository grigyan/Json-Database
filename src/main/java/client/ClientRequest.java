package client;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class ClientRequest {
    @Parameter(names = "-t")
    @Expose
    private String type = "";

    @Parameter(names = "-k")
    @Expose
    private String key;

    @Parameter(names = "-v")
    @Expose
    private String value;

    @Parameter(names = "-in")
    private String fileName;

    public String getRequestJson() {
        if (null != fileName) {
            try {
                JsonElement json = new Gson().fromJson(new FileReader(getFilePath()), JsonElement.class);
                return new Gson().toJson(json);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return new Gson().toJson(this);
    }

    private String getFilePath() {
        return "src/main/java/client/resources/" + fileName;
    }
}
