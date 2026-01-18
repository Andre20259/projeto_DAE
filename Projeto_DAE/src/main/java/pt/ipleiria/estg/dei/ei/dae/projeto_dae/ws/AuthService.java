package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.AuthDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.ColaboratorBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.TokenIssuer;

import java.util.Map;

@Path("auth")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class AuthService {
    @EJB
    private UserBean userBean;

    @EJB
    private ColaboratorBean colaboratorBean;

    @Context
    private SecurityContext securityContext;

    @POST
    @Path("/login")
    public Response authenticate(@Valid AuthDTO auth) {
        if (userBean.canLogin(auth.getUsername(), auth.getPassword())) {
            String token = TokenIssuer.issue(auth.getUsername());
            return Response.ok(token).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/recover-password")
    public Response recoverPassword(Map<String, String> data) {
        String email = data.get("email");
        // Lógica aqui
        return Response.ok("{\"message\": \"Password recovery link sent to " + email + "\"}").build();
    }

    @POST
    @Path("/register")
    public Response register(@Valid UserDTO userDTO) throws MyEntityExistsException {
        // Cria o utilizador como Colaborador por defeito
        colaboratorBean.create(
                userDTO.getUsername(),
                userDTO.getPassword(),
                userDTO.getName(),
                userDTO.getEmail()
        );
        return Response.status(Response.Status.CREATED)
                .entity("{\"message\": \"Utilizador registado com sucesso\"}")
                .build();
    }

}
