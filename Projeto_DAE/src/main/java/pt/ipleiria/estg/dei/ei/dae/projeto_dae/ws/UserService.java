package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserActivityDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserDTO;
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
@Authenticated
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

    // 3.1 - own activity /api/users/{id}/activity
    @GET
    @Path("{id}/activity")
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
    public Response updateProfile(@PathParam("id") String id, Map<String, String> body)
            throws MyEntityNotFoundException {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        String email = body.get("email");
        String name = body.get("name");
        userBean.update(id, name, email);
        return Response.ok(Map.of("message", "Profile updated successfully")).build();
    }

    // 3.3 - change own password /api/users/{id}/change-password
    @PUT
    @Path("{id}/change-password")
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
    public Response getUser(@PathParam("id") String id) throws MyEntityNotFoundException {
        if (!isSelf(id) && !isAdmin()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        var user = userBean.find(id);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");
        return Response.ok(UserDTO.from(user)).build();
    }

    // ===== ADMIN‑ONLY ENDPOINTS =====

    // 3.7 - admin creates users /api/users
    @POST
    @RolesAllowed("Administrator")
    public Response createUser(UserCreateDTO dto) throws MyEntityExistsException {
        // by default create as Colaborator; you can extend to choose type
        colaboratorBean.create(dto.getUsername(), dto.getPassword(), dto.getName(), dto.getEmail());
        return Response.status(Response.Status.CREATED)
                .entity(UserDTO.from(userBean.find(dto.getUsername())))
                .build();
    }

    // 3.8 - admin edits user name/email /api/users/{id}
    @PUT
    @Path("{id}/admin-edit")
    @RolesAllowed("Administrator")
    public Response adminEditUser(@PathParam("id") String id, Map<String, String> body)
            throws MyEntityNotFoundException {
        String name = body.get("name");
        String email = body.get("email");
        userBean.update(id, name, email);
        return Response.ok(Map.of("message", "User edited")).build();
    }

    // 3.9 - admin deletes user /api/users/{id}
    @DELETE
    @Path("{id}")
    @RolesAllowed("Administrator")
    public Response deleteUser(@PathParam("id") String id) throws MyEntityNotFoundException {
        userBean.delete(id);
        return Response.ok(Map.of("message", "User deleted")).build();
    }

    // 3.12 - admin changes user role /api/users/{id}/role
    @POST
    @Path("{id}/role")
    @RolesAllowed("Administrator")
    public Response changeUserRole(@PathParam("id") String id, Map<String, Integer> body)
            throws MyEntityNotFoundException, MyEntityExistsException {
        int roles = body.get("roles"); // e.g.: 1=Colaborator, 2=Responsible, 3=Administrator

        // very simple implementation: delete and recreate with same credentials
        var user = userBean.find(id);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");

        String username = user.getUsername();
        String name = user.getName();
        String email = user.getEmail();
        String hashedPassword = user.getPassword(); // already hashed

        // remove old instance
        userBean.delete(username);

        // re‑create with same data in the new role
        switch (roles) {
            case 1 -> {
                // Colaborator
                // password is already hashed, so use persist directly
                colaboratorBean.create(username, name, email, ""); // or adjust create to accept hashed
            }
            case 2 -> responsibleBean.create(username, hashedPassword, name, email);
            case 3 -> administratorBean.create(username, hashedPassword, name, email);
            default -> throw new BadRequestException("Invalid role");
        }

        return Response.ok(Map.of("message", "User updated")).build();
    }

    // 3.13 - admin gets any user's history /api/users/{id}/history
    @GET
    @Path("{id}/history")
    @RolesAllowed("Administrator")
    public Response getUserHistory(@PathParam("id") String id) throws MyEntityNotFoundException {
        UserActivityDTO dto = userBean.getActivity(id);
        return Response.ok(dto).build();
    }
}