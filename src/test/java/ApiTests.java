import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.example.Verification;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ApiTests {
    ObjectMapper mapper = new ObjectMapper();
    @Test
    void testGetUser() throws JsonProcessingException {
        // Read the JSON test case
        File file = new File("src/test/resources/getUser.json");
        JsonPath jsonPath = JsonPath.from(file);

        // Send the API request
        RequestSpecification requestSpec = given()
                .baseUri(jsonPath.getString("request.url"))
                .headers(jsonPath.getMap("request.headers"));
        Response response = requestSpec
                .when()
                .request(jsonPath.getString("request.method"))
                .then()
                .extract()
                .response();

        // Verify the API response headers
        List<Verification> headerVerifications = jsonPath.getList("response.headerVerifications").stream().map((validation) -> {
            try {
                return mapper.readValue(mapper.writeValueAsString(validation), Verification.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

        for (Verification verification : headerVerifications) {
            String type = verification.getType();
            String headerName = verification.getHeaderName();

            switch (type) {
                case "headerExists": {
                    response.then().header(headerName, notNullValue());
                    break;
                }
                case "headerEquals": {
                    String expectedValue = verification.getExpectedValue();
                    response.then().header(headerName, equalTo(expectedValue));
                    break;
                }
                default:
                    System.out.println("Invalid type");
            }
        }

        // Verify the API response body
        List<Verification> bodyVerifications = jsonPath.getList("response.bodyVerifications").stream().map((validation) -> {
            try {
                return mapper.readValue(mapper.writeValueAsString(validation), Verification.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());


        for (Verification verification : bodyVerifications) {
            String type = verification.getType();
            switch (type) {
                case "bodyEquals": {
                    String expectedValue = verification.getExpectedValue();
                    response.then().body(equalTo(expectedValue));
                    break;
                }
                case "bodyContains": {
                    String expectedValue = verification.getExpectedValue();
                    Map<String, Object> expectedValueMap = JsonPath.from(expectedValue).getMap("");

                    // iterate over expectedValueMap and print key and value
                    for (Map.Entry<String, Object> entry : expectedValueMap.entrySet()) {
                        String key = entry.getKey();
                        Object value = entry.getValue();
                        response.then().body(key, equalTo(value));
                    }

                    break;
                }
                case "keyExists": {
                    String jsonPathExpression = verification.getJsonPath();
                    response.then().body(jsonPathExpression, notNullValue());
                    break;
                }
                case "valueEquals": {
                    String jsonPathExpression = verification.getJsonPath();
                    String expectedValue = verification.getExpectedValue();
                    response.then().body(jsonPathExpression, equalTo(expectedValue));
                    break;
                }
            }
        }

        // Verify the API response status
        int expectedStatus = jsonPath.getInt("response.status");
        response.then().statusCode(expectedStatus);
    }

}
