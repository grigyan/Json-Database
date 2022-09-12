package server.database;

import com.google.gson.*;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private final String DB_PATH = "src/main/java/server/data/db.json";
    private final File DB_FILE = new File(DB_PATH);
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private final Lock READ_LOCK = LOCK.readLock();
    private final Lock WRITE_LOCK = LOCK.writeLock();
    private final JsonObject database;


    public Database() {
        try {
            database = new Gson().fromJson(new FileReader(DB_FILE), JsonObject.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseResponse sendRequest(DatabaseRequest request) {
        switch (request.getType()) {
            case "set":
                return this.setByKey(request.getKey(), request.getValue());
            case "get":
                return this.getByKey(request.getKey());
            case "delete":
                return this.deleteByKey(request);
        }

        throw new RuntimeException("Request type is not valid");
    }

    private DatabaseResponse setByKey(JsonElement key, JsonElement value) {
        WRITE_LOCK.lock();
        if (key.isJsonPrimitive()) {
            database.add(key.getAsString(), value);
        } else if (key.isJsonArray()) {
            JsonArray keys = key.getAsJsonArray();
            String toAddKey = keys.remove(keys.size() - 1).getAsString();
            findElement(keys, true).getAsJsonObject().add(toAddKey, value);
        }
        saveDatabase();

        DatabaseResponse response = new DatabaseResponse();
        response.setResponse("OK");
        WRITE_LOCK.unlock();
        return response;
    }

    private DatabaseResponse getByKey(JsonElement key) {
        try {
            READ_LOCK.lock();
            DatabaseResponse response = new DatabaseResponse();

            if (key.isJsonPrimitive() && database.has(key.getAsString())) {
                var jsonElement = database.get(key.getAsString());
                response.setResponse("OK");
                response.setValue(jsonElement);
                return response;
            } else if (key.isJsonArray()) {
                try {
                    var jsonElement = findElement(key.getAsJsonArray(), false);
                    response.setResponse("OK");
                    response.setValue(jsonElement);
                } catch (RuntimeException e) {
                    response.setResponse("ERROR");
                    response.setReason("No such key");
                    return response;
                }
                return response;
            }
            return null;
        } finally {
            READ_LOCK.unlock();
        }
    }

    private DatabaseResponse deleteByKey(DatabaseRequest requestJ) {
        try {
            WRITE_LOCK.lock();
            DatabaseResponse response = new DatabaseResponse();
            JsonElement key = requestJ.getKey();

            if (key.isJsonPrimitive() && database.has(key.getAsString())) {
                database.remove(key.getAsString());
                response.setResponse("OK");
                return response;
            } else if (key.isJsonArray()) {
                JsonArray keys = key.getAsJsonArray();
                String toRemoveKey = keys.remove(keys.size() - 1).getAsString();

                try {
                    findElement(keys, false).getAsJsonObject().remove(toRemoveKey);
                } catch (RuntimeException e) {
                    response.setResponse("ERROR");
                    response.setReason("No such key");
                    return response;
                }

                response.setResponse("OK");
                return response;
            }

            return response;
        } finally {
            saveDatabase();
            WRITE_LOCK.unlock();
        }
    }

    private JsonElement findElement(JsonArray keys, boolean createIfAbsent) {
        JsonElement tmp = database;
        if (createIfAbsent) {
            for (JsonElement key : keys) {
                if (!tmp.getAsJsonObject().has(key.getAsString())) {
                    tmp.getAsJsonObject().add(key.getAsString(), new JsonObject());
                }
                tmp = tmp.getAsJsonObject().get(key.getAsString());
            }
        } else {
            for (JsonElement key : keys) {
                if (!key.isJsonPrimitive() || !tmp.getAsJsonObject().has(key.getAsString())) {
                    throw new RuntimeException("No Such Key");
                }
                tmp = tmp.getAsJsonObject().get(key.getAsString());
            }
        }
        return tmp;
    }

    private void saveDatabase() {
        try (FileWriter writer = new FileWriter(DB_PATH)) {
            writer.write(new GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                    .toJson(database));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
