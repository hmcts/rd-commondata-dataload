package uk.gov.hmcts.reform.rd.commondata.smoketests;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.springframework.http.HttpStatus.OK;

public class SmokeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8100"
        );

    @Test
    public void isRunningAndHealthy() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .when()
            .get("/health")
            .then()
            .statusCode(OK.value())
            .and()
            .extract().body().asString();

        Assertions.assertThat(response)
            .contains("UP");
    }
}
