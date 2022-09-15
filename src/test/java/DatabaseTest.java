import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.database.Database;
import server.database.DatabaseResponse;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTest {
    private static final String TEST_DB_PATH = "src/test/java/database/testDb.json";
    static Database database;

    private static Stream<Arguments> saveDbArgs() {
        return Stream.of(
                Arguments.of("{\n" +
                        "   \"type\":\"set\",\n" +
                        "   \"key\":\"person\",\n" +
                        "   \"value\":{\n" +
                        "      \"name\":\"Elon Musk\",\n" +
                        "      \"car\":{\n" +
                        "         \"model\":\"Tesla Roadster\",\n" +
                        "         \"year\":\"2018\"\n" +
                        "      },\n" +
                        "      \"rocket\":{\n" +
                        "         \"name\":\"Falcon 9\",\n" +
                        "         \"launches\":\"87\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}", "{\"response\":\"OK\"}")
        );
    }


    private static Stream<Arguments> addToDatabaseArgs() {
        return Stream.of(
                Arguments.of("{\"type\":\"set\",\"key\":\"primitiveKey\",\"value\":\"testValue\"}",
                        "{\"response\":\"OK\"}"),
                Arguments.of("{\n" +
                        "   \"type\":\"set\",\n" +
                        "   \"key\":\"person\",\n" +
                        "   \"value\":{\n" +
                        "      \"name\":\"Elon Musk\",\n" +
                        "      \"car\":{\n" +
                        "         \"model\":\"Tesla Roadster\",\n" +
                        "         \"year\":\"2018\"\n" +
                        "      },\n" +
                        "      \"rocket\":{\n" +
                        "         \"name\":\"Falcon 9\",\n" +
                        "         \"launches\":\"87\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}", "{\"response\":\"OK\"}")
        );
    }

    private static Stream<Arguments> deleteFromDatabaseArgs() {
        return Stream.of(
                Arguments.of("{\n" +
                        "   \"type\":\"set\",\n" +
                        "   \"key\":\"person\",\n" +
                        "   \"value\":{\n" +
                        "      \"name\":\"Elon Musk\",\n" +
                        "      \"car\":{\n" +
                        "         \"model\":\"Tesla Roadster\",\n" +
                        "         \"year\":\"2018\"\n" +
                        "      },\n" +
                        "      \"rocket\":{\n" +
                        "         \"name\":\"Falcon 9\",\n" +
                        "         \"launches\":\"87\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}", "{\"type\":\"delete\",\"key\":[\"person\",\"car\",\"year\"]}", "{\"response\":\"OK\"}"),
                Arguments.of("{\"type\":\"set\",\"key\":[\"person\",\"rocket\",\"launches\"],\"value\":\"88\"}",
                        "{\"type\":\"delete\",\"key\":[\"person\",\"car\",\"year\"]}",
                        "{\"response\":\"ERROR\",\"reason\":\"No such key\"}")
        );
    }

    private static Stream<Arguments> getFromDatabaseArgs() {
        return Stream.of(
                Arguments.of("{\n" +
                        "   \"type\":\"set\",\n" +
                        "   \"key\":\"person\",\n" +
                        "   \"value\":{\n" +
                        "      \"name\":\"Elon Musk\",\n" +
                        "      \"car\":{\n" +
                        "         \"model\":\"Tesla Roadster\",\n" +
                        "         \"year\":\"2018\"\n" +
                        "      },\n" +
                        "      \"rocket\":{\n" +
                        "         \"name\":\"Falcon 9\",\n" +
                        "         \"launches\":\"87\"\n" +
                        "      }\n" +
                        "   }\n" +
                        "}", "{\"type\":\"get\",\"key\":[\"person\",\"car\",\"year\"]}", "{\"response\":\"OK\",\"value\":\"2018\"}"),
                Arguments.of("{\"type\":\"set\",\"key\":\"1\",\"value\":\"Hello world!\"}",
                        "{\"type\":\"get\",\"key\":\"dummy\"}",
                        "{\"response\":\"ERROR\",\"reason\":\"No such key\"}")
        );
    }

    @BeforeAll
    public static void initDatabase() throws FileNotFoundException {
        database = new Database(TEST_DB_PATH);
    }

    @AfterEach
    public void clearDbFile() throws IOException {
        new PrintWriter(TEST_DB_PATH).close();
        Files.write(Path.of(TEST_DB_PATH), "{}".getBytes());
    }

    @ParameterizedTest
    @MethodSource("addToDatabaseArgs")
    @DisplayName("setByKey test")
    void shouldAddToDatabase(String request, String expectedResponse) {
        // given
        JsonParser parser = new JsonParser();

        JsonElement key = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("key");
        JsonElement value = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("value");

        // when
        DatabaseResponse actualResponse = database.setByKey(key, value);

        // then
        assertEquals(parser.parse(actualResponse.getJsonString()), parser.parse(expectedResponse));
    }

    @ParameterizedTest
    @MethodSource("deleteFromDatabaseArgs")
    @DisplayName("deleteByKey test")
    void shouldDeleteFromDatabase(String toAddJson, String request, String expectedResponse) {
        // given
        JsonParser parser = new JsonParser();

        var toAddJsonKey = JsonParser.parseString(toAddJson)
                .getAsJsonObject()
                .get("key");
        var toAddJsonValue = JsonParser.parseString(toAddJson)
                .getAsJsonObject()
                .get("value");
        database.setByKey(toAddJsonKey, toAddJsonValue);

        var requestKey = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("key");

        // when
        DatabaseResponse actualResponse = database.deleteByKey(requestKey);

        // then
        assertEquals(parser.parse(actualResponse.getJsonString()), parser.parse(expectedResponse));

    }

    @ParameterizedTest
    @MethodSource("getFromDatabaseArgs")
    @DisplayName("getByKey test")
    void shouldGetFromDatabase(String toAddJson, String request, String expectedResponse) {
        // given
        JsonParser parser = new JsonParser();

        var toAddJsonKey = JsonParser.parseString(toAddJson)
                .getAsJsonObject()
                .get("key");
        var toAddJsonValue = JsonParser.parseString(toAddJson)
                .getAsJsonObject()
                .get("value");
        database.setByKey(toAddJsonKey, toAddJsonValue);

        var requestKey = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("key");
        // when
        DatabaseResponse actualResponse = database.getByKey(requestKey);

        // then
        assertEquals(parser.parse(actualResponse.getJsonString()), parser.parse(expectedResponse));
    }


    @ParameterizedTest
    @MethodSource("saveDbArgs")
    @DisplayName("saveDb test")
    void shouldSaveDatabaseChanges(String request) throws FileNotFoundException {
        // given
        JsonParser parser = new JsonParser();

        JsonElement key = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("key");
        JsonElement value = JsonParser.parseString(request)
                .getAsJsonObject()
                .get("value");

        // when
        database.setByKey(key, value);

        // then
        JsonObject expected = new JsonObject();
        expected.add(key.getAsString(), value);
        JsonObject actual = new Gson().fromJson(new FileReader(TEST_DB_PATH), JsonObject.class);

        assertEquals(expected.entrySet(), actual.entrySet());
    }
}