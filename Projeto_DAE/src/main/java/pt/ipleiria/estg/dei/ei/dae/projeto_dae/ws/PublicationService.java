package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.*;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.CommentBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.RatingBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Path("publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PublicationService {
    @Context
    private SecurityContext securityContext;
    @EJB
    private PublicationBean publicationBean;
    @EJB
    private CommentBean commentBean;
    @EJB
    private RatingBean ratingBean;


    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(MultipartFormDataInput form) throws IOException, MyEntityNotFoundException {

        var map = form.getFormDataMap();

        var filePart = map.get("file").get(0);
        var fileName = filePart.getFileName();
        var fileStream = filePart.getBody(InputStream.class, null);

        String publicationJson =
                map.get("publication").get(0).getBodyAsString();

        Jsonb jsonb = JsonbBuilder.create();
        PublicationCreateDTO dto =
                jsonb.fromJson(publicationJson, PublicationCreateDTO.class);

        Publication publication = publicationBean.create(fileName, fileStream, dto);

        return Response.status(Response.Status.CREATED)
                .entity(PublicationDTO.from(publication))
                .build();
    }

    @PATCH
    @Path("/{id}")
    @RolesAllowed({"Responsible", "Administrator"})
    public PublicationDTO updateVisibility(@PathParam("id") Long id, VisibilityDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.updateVisibility(id, dto.isVisible());
        return PublicationDTO.from(publication);
    }

    @PUT
    @Path("/{id}")
    public PublicationDTO updatePublication(@PathParam("id") Long id, PublicationCreateDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.find(id);
        String username = securityContext.getUserPrincipal().getName();

        boolean isAuthor = publication.getAuthors() != null &&
                publication.getAuthors().stream()
                        .anyMatch(a -> username.equals(a.getUsername()));

        if (!isAuthor) {
            throw new ForbiddenException("Only authors can update this publication");
        }

        publication = publicationBean.updatePublication(id, dto);
        return PublicationDTO.from(publication);
    }

    @PUT
    @Path("/{id}/tags")
    public PublicationDTO updateTags(@PathParam("id") Long id, ManageTagDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.find(id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found");
        }

        String username = securityContext.getUserPrincipal().getName();
        boolean isAuthor = publication.getAuthors() != null &&
                publication.getAuthors().stream()
                        .anyMatch(a -> username.equals(a.getUsername()));

        boolean isResponsibleOrAdmin =
                securityContext.isUserInRole("Responsible") || securityContext.isUserInRole("Administrator")
                        || securityContext.isUserInRole("RESPONSIBLE") || securityContext.isUserInRole("ADMINISTRATOR");

        switch (dto.getAction()) {
            case "add":
                if (!isAuthor) {
                    throw new ForbiddenException("Only authors can add tags");
                }
                break;
            case "remove":
                if (!(isAuthor || isResponsibleOrAdmin)) {
                    throw new ForbiddenException("Only authors or Responsible/Administrator can remove tags");
                }
                break;
            default:
                throw new BadRequestException("Invalid action: " + dto.getAction());
        }

        publication = publicationBean.updateTags(id, dto);
        return PublicationDTO.from(publication);
    }

    @GET
    @Path("/")
    public List<PublicationDTO> getAllPublications() {
        return PublicationDTO.from(publicationBean.findAll());
    }

    @GET
    @Path("/{id}/summary")
    public Response generateSummary(@PathParam("id") Long id) throws MyEntityNotFoundException {

        Publication pub = publicationBean.generateAndStoreSummary(id);

        boolean privileged = securityContext.isUserInRole("COLABORATOR");

        PublicationSummaryDTO dto = new PublicationSummaryDTO(pub.getSummary(), privileged);

        return Response.ok()
                .entity(dto)
                .build();
    }

    @GET
    @Path("/{id}")
    public PublicationDTO getPublication(@PathParam("id") long id) throws MyEntityNotFoundException {
        Publication publication = publicationBean.find(id);

        // determine if caller is privileged; adjust role names if different in your app
        boolean privileged = securityContext.isUserInRole("RESPONSIBLE")
                || securityContext.isUserInRole("ADMINISTRATOR");

        PublicationDTO dto = PublicationDTO.from(publication);

        if (!privileged && dto.getComments() != null) {
            dto.setComments(dto.getComments().stream()
                    .filter(c -> c.isVisible())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @GET
    @Path("/me")
    public List<PublicationDTO> getMyPublications() {
        String username = securityContext.getUserPrincipal().getName();
        return PublicationDTO.from(publicationBean.findByAuthor(username));
    }

    @POST
    @Path("/{id}/comments")
    public Response addComment(@PathParam("id") Long publicationId, CommentDTO dto) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        Comment comment = commentBean.create(username, publicationId, dto.content);
        return Response.status(Response.Status.CREATED).entity(CommentDTO.from(comment)).build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}")
    public Response editComment(@PathParam("id") Long publicationId, @PathParam("commentId") Long commentId, CommentDTO dto) {
        String username = securityContext.getUserPrincipal().getName();
        Comment comment = commentBean.editComment(commentId, username, dto.content);
        return Response.ok(CommentDTO.from(comment)).build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}/visibility")
    @RolesAllowed({"Administrator", "Responsible"})
    public Response setVisibility(@PathParam("id") Long publicationId, @PathParam("commentId") Long commentId, CommentDTO dto) {
        Comment updatedComment = commentBean.setVisible(commentId, publicationId, dto.isVisible());
        return Response.ok(CommentDTO.from(updatedComment)).build();
    }

    @POST
    @Path("/{id}/ratings")
    public Response addRating(@PathParam("id") Long publicationId, RatingDTO dto) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        Rating rating = ratingBean.create(username, publicationId, dto.score);
        return Response.status(Response.Status.CREATED).entity(RatingDTO.from(rating)).build();
    }

    @PUT
    @Path("/{id}/ratings/{ratingId}")
    public Response editRating(@PathParam("id") Long publicationId, @PathParam("ratingId") Long ratingId, RatingDTO dto) {
        String username = securityContext.getUserPrincipal().getName();
        ratingBean.delete(ratingId);
        Rating rating = ratingBean.create(username, publicationId, dto.score);
        return Response.ok(RatingDTO.from(rating)).build();
    }

    @DELETE
    @Path("/{id}/ratings/{ratingId}")
    public Response deleteRating(@PathParam("id") Long publicationId, @PathParam("ratingId") Long ratingId) {
        ratingBean.delete(ratingId);
        return Response.noContent().build();
    }
}

