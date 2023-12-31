package de.spraener.keycloak.test;

import dasniko.testcontainers.keycloak.ExtendableKeycloakContainer;
import de.spraener.keycloak.test.provider.PingEndpointProviderFactory;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static io.restassured.RestAssured.given;

@Testcontainers
public class XKeycloakContainerTests {

    // Let JUnit manage the container
    @Container
    private static final XKeycloakContainer testContainer = new XKeycloakContainer()
            // add the gradle classes dir (test) to the providers jar
            // NOTE! Normally this is the build/classes/main directory!
            .withProviderJarContentModifier( i -> i.importDirectory("build/classes/java/test"))
            // add the gradle resource dir (test) to the providers jar
            // NOTE! Normally this is the build/resources/main directory!
            .withProviderJarContentModifier( i -> i.importDirectory("build/resources/test"))
            // add remote debugging configuration to the container
            .withConfigurationModifier(XKeycloakContainerTests::configureDebug)
            ;

    /**
     * Private Method for BiConsumer-Interface to configure the TestContainer
     * @param ekc // An ExtendableKeycloakContainer
     * @param commandParts // A list of CommandParts for the container. Add options here
     */
    private static void configureDebug(ExtendableKeycloakContainer<?> ekc, List<String> commandParts) {
        // Set the Environment-Variable "DEBUG_PORT" to the required port
        ekc.withEnv("DEBUG_PORT", "*:8787");
        // The port needs to be exported to a fixed address. This is only possible
        // in a XKeycloakContainer
        ((XKeycloakContainer)ekc).withDebugPort(8787, 8787);
        // Do not suspend the container. Otherwise, the tests will stop if there
        // is no connecting debugger.
        ekc.withEnv("DEBUG_SUSPEND", "n");
        // add the "-debug" option to the container start command
        commandParts.add("--debug");
    }

    @Test
    void testPingAvailable() {
        String baseURI = testContainer.getAuthServerUrl();
        given()
                .baseUri(baseURI)
                .basePath("/realms/master/" + PingEndpointProviderFactory.PROVIDER_ID)
                .when()
                .get("/ping")
                .then()
                .assertThat()
                .statusCode(200)
                .body(new StringContains("pong!"))
                ;
    }

}
