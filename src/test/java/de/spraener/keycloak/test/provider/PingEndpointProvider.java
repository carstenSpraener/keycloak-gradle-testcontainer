package de.spraener.keycloak.test.provider;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.services.resource.RealmResourceProvider;

/**
 * Just a little ResourceProvider to test the Container starting the
 * Endpoint-Provider-Extension
 */
public class PingEndpointProvider implements RealmResourceProvider {

    @Override
    public Object getResource() {
        return this;
    }

    @Override
    public void close() {
    }

    @GET
    @Path("ping")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        return Response.ok("pong!").build();
    }

}
