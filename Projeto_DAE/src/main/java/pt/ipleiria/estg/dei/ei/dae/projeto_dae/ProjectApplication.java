package pt.ipleiria.estg.dei.ei.dae.projeto_dae;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

@ApplicationPath("/api")
public class ProjectApplication extends Application {
    @OPTIONS
    @Path("{path: .*}")
    public Response options() {
        return Response.ok().build();
    }
}