import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import server.database.Database;
import server.database.DatabaseResponse;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseTest {
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
                        "{\"type\":\"get\",\"key\":[\"person\",\"car\",\"year\"]}",
                        "{\"response\":\"ERROR\",\"reason\":\"No such key\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("addToDatabaseArgs")
    @DisplayName("setByKey test")
    void shouldAddToDatabase(String request, String expectedResponse) {
        // given
        Database database = new Database();
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
        Database database = new Database();
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
        Database database = new Database();
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
}