package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.TagDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.TagBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

import java.util.List;

@Path("/tags")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class TagService {

    @EJB
    private TagBean tagBean;

    @Context
    private SecurityContext securityContext;

    @GET
    //@Path("/")
    public List<TagDTO> getAllTags() {
        return TagDTO.from(tagBean.findAll());
    }

    @GET
    @Path("/hidden")
    @RolesAllowed({"Administrator", "Responsible"})
    public List<TagDTO> getHiddenTags() {
        return TagDTO.from(tagBean.findHidden());
    }

    @POST
    //@Path("/")
    //@Authenticated
    //@RolesAllowed({"Administrator", "Responsible"})
    public Response createNewTag (TagDTO tagDTO) {
        tagBean.create(tagDTO.getName());
        return Response.status(Response.Status.CREATED).entity(tagDTO).build();
    }

    @GET
    @Path("{name}")
    public Response getTag(@PathParam("name") String name) throws MyEntityNotFoundException {
        var tag = tagBean.find(name);
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag with name " + name + " not found");
        }
        return Response.ok(TagDTO.from(tag)).build();
    }

    @DELETE
    @Path("{name}")
    @RolesAllowed({"Administrator", "Responsible"})
    public Response deleteTag(@PathParam("name") String name) {
        tagBean.delete(name);
        return Response.ok("{\"message\":\"Tag '" + name + "' eliminada.\"}").build();
    }

    @PUT
    @Path("{name}")
    @RolesAllowed({"Administrator", "Responsible"})
    public Response setVisibility(@PathParam("name") String name, TagDTO tagDTO) {
        Tag updatedTag = tagBean.setVisible(name, tagDTO.isVisible());
        return Response.ok(TagDTO.from(updatedTag)).build();
    }

    @POST
    @Path("{name}/subscribe")
    public Response subscribe(@PathParam("name") String tagName) {
        User user = (User) securityContext.getUserPrincipal();
        tagBean.subscribeUserToTag(user.getName(), tagName);
        return Response.ok("{\"message\":\"Subscribed to tag '" + tagName + "'\"}").build();
    }

    @DELETE
    @Path("{name}/unsubscribe")
    public Response unsubscribe(@PathParam("name") String tagName) {
        User user = (User) securityContext.getUserPrincipal();
        tagBean.unsubscribeUserFromTag(user.getName(), tagName);
        return Response.ok("{\"message\":\"Unsubscribed from tag '" + tagName + "'\"}").build();
    }







}
