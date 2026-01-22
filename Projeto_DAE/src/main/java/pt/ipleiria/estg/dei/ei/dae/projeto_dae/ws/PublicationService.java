package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import java.util.logging.Logger;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.json.bind.annotation.JsonbDateFormat;
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

    private static final Logger LOGGER = Logger.getLogger(PublicationService.class.getName());


    @POST
    @Path("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(MultipartFormDataInput form) throws IOException, MyEntityNotFoundException {
        var map = form.getFormDataMap();

        var filePart = map.get("file").get(0);
        var fileName = filePart.getFileName();
        var fileStream = filePart.getBody(InputStream.class, null);

        String publicationJson = map.get("publication").get(0).getBodyAsString();
        LOGGER.info("publicationJson = " + publicationJson.replaceAll("\\R", " ")); // single-line log

        Jsonb jsonb = JsonbBuilder.create();
        PublicationCreateDTO dto = jsonb.fromJson(publicationJson, PublicationCreateDTO.class);

        // Log DTO as JSON (easy to read)
        String dtoAsJson = jsonb.toJson(dto);
        LOGGER.info("Deserialized PublicationCreateDTO = " + dtoAsJson);

        // also log individual fields to be explicit
        LOGGER.info("DTO.title = " + dto.getTitle());
        LOGGER.info("DTO.authors = " + String.valueOf(dto.getAuthors())); // shows null or list
        LOGGER.info("DTO.tags = " + String.valueOf(dto.getTags()));

        Publication publication = publicationBean.create(fileName, fileStream, dto);

        return Response.status(Response.Status.CREATED)
                .entity(PublicationDTO.from(publication))
                .build();
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

