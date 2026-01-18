package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

@Path("user")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class UserService {

    @EJB
    UserBean userBean = new UserBean();

    @Context
    private SecurityContext securityContext;

    @GET
    @Authenticated
    @Path("/")
    public Response getAuthenticatedUser() {
        var username = securityContext.getUserPrincipal().getName();
        var user = userBean.find(username);
        return Response.ok(UserDTO.from(user)).build();
    }


}
