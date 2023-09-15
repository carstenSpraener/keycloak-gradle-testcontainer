package de.spraener.keycloak.test.provider;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

import java.util.logging.Logger;

/**
 * Just a little ResourceProvider to test the Container starting the
 * Endpoint-Provider-Extension
 */
public class PingEndpointProviderFactory implements RealmResourceProviderFactory {
    public static final Logger LOGGER = Logger.getLogger("PingEndpoint");
    public static final String PROVIDER_ID="ping";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        LOGGER.info("Created new Instance of PingEndpointProvider with provider-id: '"+PROVIDER_ID+"'.");
        return new PingEndpointProvider();
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
