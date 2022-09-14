package server.database;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.*;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private final String DB_PATH = "src/main/java/server/data/db.json";
    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private final Lock READ_LOCK = LOCK.readLock();
    private final Lock WRITE_LOCK = LOCK.writeLock();
    private final JsonObject database;


    public Database() {
        try {
            database = new Gson().fromJson(new FileReader(DB_PATH), JsonObject.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public DatabaseResponse sendRequest(DatabaseRequest request) {
        switch (request.getType()) {
            case "set":
                var setResponse = this.setByKey(request.getKey(), request.getValue());
                saveDatabase();
                return setResponse;
            case "get":
                return this.getByKey(request.getKey());
            case "delete":
                var deleteResponse = this.deleteByKey(request.getKey());
                saveDatabase();
                return deleteResponse;
        }

        throw new RuntimeException("Request type is not valid");
    }

    @VisibleForTesting
    public DatabaseResponse setByKey(JsonElement key, JsonElement value) {
        WRITE_LOCK.lock();
        if (key.isJsonPrimitive()) {
            database.add(key.getAsString(), value);
        } else if (key.isJsonArray()) {
            JsonArray keys = key.getAsJsonArray();
            String toAddKey = keys.remove(keys.size() - 1).getAsString();
            createAbsentKeys(keys);
            database.getAsJsonObject().add(toAddKey, value);
        }
        WRITE_LOCK.unlock();

        DatabaseResponse response = new DatabaseResponse();
        response.setResponse("OK");
        return response;
    }

    @VisibleForTesting
    public DatabaseResponse getByKey(JsonElement key) {
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
                    var jsonElement = findElement(key.getAsJsonArray());
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

    @VisibleForTesting
    public DatabaseResponse deleteByKey(JsonElement key) {
        try {
            WRITE_LOCK.lock();
            DatabaseResponse response = new DatabaseResponse();

            if (key.isJsonPrimitive() && database.has(key.getAsString())) {
                database.remove(key.getAsString());
                response.setResponse("OK");
                return response;
            } else if (key.isJsonArray()) {
                JsonArray keys = key.getAsJsonArray();
                String toRemoveKey = keys.remove(keys.size() - 1).getAsString();

                try {
                    findElement(keys).getAsJsonObject().remove(toRemoveKey);
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
            WRITE_LOCK.unlock();
        }
    }

    private JsonElement findElement(JsonArray keys) {
        JsonElement currentElement = database;

        for (JsonElement key : keys) {
            if (!key.isJsonPrimitive() || !currentElement.getAsJsonObject().has(key.getAsString())) {
                throw new RuntimeException("No Such Key");
            }
            currentElement = currentElement.getAsJsonObject().get(key.getAsString());
        }

        return currentElement;
    }

    private void createAbsentKeys(JsonArray keys) {
        for (JsonElement key : keys) {
            database.getAsJsonObject().add(key.getAsString(), new JsonObject());
        }
    }

    public void saveDatabase() {
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
