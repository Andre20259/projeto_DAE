package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.AdministratorBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.ColaboratorBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.ResponsibleBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

import java.util.Map;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserService {

    @EJB
    private UserBean userBean;

    @EJB
    private ColaboratorBean colaboratorBean;

    @EJB
    private ResponsibleBean responsibleBean;

    @EJB
    private AdministratorBean administratorBean;

    @Context
    private SecurityContext securityContext;

    private String currentUsername() {
        return securityContext.getUserPrincipal().getName();
    }

    private boolean isAdmin() {
        return securityContext.isUserInRole("Administrator");
    }

    private boolean isSelf(String username) {
        return currentUsername().equals(username);
    }

    @OPTIONS
    @Path("/")
    @PermitAll
    public Response options() {
        return Response.ok()
                .header("Allow", "GET, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @OPTIONS
    @Path("/{id}")
    @PermitAll
    public Response optionsSingle() {
        return Response.ok()
                .header("Allow", "GET, PATCH, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @OPTIONS
    @Path("/{id}/activity")
    @PermitAll
    public Response optionsSingleA() {
        return Response.ok()
                .header("Allow", "GET, PATCH, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @OPTIONS
    @Path("/{id}/role")
    @PermitAll
    public Response optionsSingleR() {
        return Response.ok()
                .header("Allow", "GET, PATCH, POST, PUT, DELETE, OPTIONS")
                .build();
    }


    @OPTIONS
    @Path("/{id}/history")
    @PermitAll
    public Response optionsSingleH() {
        return Response.ok()
                .header("Allow", "GET, PATCH, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @OPTIONS
    @Path("/{id}/change-password")
    @PermitAll
    public Response optionsSinglePW() {
        return Response.ok()
                .header("Allow", "GET, PATCH, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    // 3.1 - own activity /api/users/{id}/activity
    @GET
    @Path("{id}/activity")
    @Authenticated
    public Response getOwnActivity(@PathParam("id") String id) throws MyEntityNotFoundException {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        UserActivityDTO dto = userBean.getActivity(id);
        return Response.ok(dto).build();
    }

    // 3.2 - edit own profile /api/users/{id}
    @PUT
    @Path("{id}")
    @Authenticated
    public Response updateProfile(@PathParam("id") String id, Map<String, String> body)
            throws MyEntityNotFoundException {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String email = body.get("email");
        String name = body.get("name");
        boolean isActive = Boolean.parseBoolean(body.get("isActive"));
        userBean.update(id, name, email, isActive);
        return Response.ok(Map.of("message", "Profile updated successfully")).build();
    }

    // 3.3 - change own password /api/users/{id}/change-password
    @PUT
    @Path("{id}/change-password")
    @Authenticated
    public Response changePassword(@PathParam("id") String id, Map<String, String> body)
            throws Exception {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userBean.updatePassword(id, oldPassword, newPassword);
        return Response.ok(Map.of("message", "Password changed successfully")).build();
    }

    // 3.6 - get own data /api/users/{id}
    @GET
    @Path("{id}")
    @Authenticated
    public Response getUser(@PathParam("id") String id) throws MyEntityNotFoundException {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        var user = userBean.find(id);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");
        return Response.ok(UserDTO.from(user)).build();
    }

    // ===== PUBLIC endpoint: anyone can create users /api/users =====
    // 3.7 - admin creates users /api/users
    @POST
    @PermitAll
    public Response createUser(UserCreateDTO dto) throws MyEntityExistsException {
        colaboratorBean.create(dto.getUsername(), dto.getPassword(), dto.getName(), dto.getEmail());
        return Response.status(Response.Status.CREATED)
                .entity(UserDTO.from(userBean.find(dto.getUsername())))
                .build();
    }

    // 3.9 - admin deletes user /api/users/{id}
    @DELETE
    @Path("{id}")
    @Authenticated
    @RolesAllowed("Administrator")
    public Response deleteUser(@PathParam("id") String id) throws MyEntityNotFoundException {
        userBean.delete(id);
        return Response.ok(Map.of("message", "User deleted")).build();
    }

    // 3.13 - admin gets any user's history /api/users/{id}/history
    @GET
    @Path("{id}/history")
    @Authenticated
    @RolesAllowed("Administrator")
    public Response getUserHistory(@PathParam("id") String id) throws MyEntityNotFoundException {
        UserActivityDTO dto = userBean.getActivity(id);
        return Response.ok(dto).build();
    }

    @GET
    @Authenticated
    @RolesAllowed("Administrator")
    public Response getAllUsers() {
        var users = userBean.findAll();
        var usersDTO = users.stream().map(UserDTO::from).toList();
        return Response.ok(usersDTO).build();
    }

    @PUT
    @Path("{username}/role")
    @Authenticated
    @RolesAllowed("Administrator")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUserRole(@PathParam("username") String username, RoleDTO dto)
            throws MyEntityNotFoundException, MyEntityExistsException {
        userBean.changeRole(username, dto.role);
        return Response.ok(Map.of("message", "User role updated successfully")).build();
    }

}
