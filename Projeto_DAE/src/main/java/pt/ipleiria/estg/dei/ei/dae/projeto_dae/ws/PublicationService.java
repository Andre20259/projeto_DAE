package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.*;
import java.util.logging.Logger;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.json.bind.annotation.JsonbDateFormat;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.*;
import jakarta.annotation.security.PermitAll;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.CommentBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.HistoryBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.RatingBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.*;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Path("/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@PermitAll
public class PublicationService {
    @Context
    private SecurityContext securityContext;
    @EJB
    private PublicationBean publicationBean;
    @EJB
    private CommentBean commentBean;
    @EJB
    private RatingBean ratingBean;
    @EJB
    private HistoryBean historyBean;

    private static final Logger LOGGER = Logger.getLogger(PublicationService.class.getName());

    @OPTIONS
    @Path("/")
    @PermitAll
    public Response options() {
        return Response.ok()
                .header("Allow", "GET, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @POST
    @Path("/")
    @Authenticated
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
                .header("Allow", "GET, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @GET
    @Path("download/{id}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("id") long id)
            throws MyEntityNotFoundException {
        var publication = publicationBean.find(id);
        var filePath = Paths.get(publication.getFilePath());
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("File not found" + filePath)
                    .build();
        }
        var filename = publication.getFilename();
        return Response.ok(filePath.toFile())
                .header("Content-Disposition",
                        "attachment;filename=\"" + filename + "\"")
                .build();
    }

    @PATCH
    @Path("/{id}")
    @Authenticated
    @RolesAllowed({"Responsible", "Administrator"})
    public PublicationDTO updateVisibility(@PathParam("id") Long id, VisibilityDTO dto) throws MyEntityNotFoundException {
        publicationBean.updateVisibility(id, dto.isVisible());
        Publication publication = publicationBean.findInitialized(id);
        return PublicationDTO.from(publication);
    }

    @PUT
    @Path("/{id}")
    @Authenticated
    public PublicationDTO updatePublication(@PathParam("id") Long id, PublicationCreateDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.findInitialized(id);
        String username = securityContext.getUserPrincipal().getName();

        boolean isAuthor = publication.getAuthors() != null &&
                publication.getAuthors().stream()
                        .anyMatch(a -> username.equals(a.getUsername()));

        if (!isAuthor) {
            throw new ForbiddenException("Only authors can update this publication");
        }

        publicationBean.updatePublication(id, dto);
        publication = publicationBean.findInitialized(id);
        return PublicationDTO.from(publication);
    }

    @PUT
    @Path("/{id}/tags")
    @Authenticated
    public PublicationDTO updateTags(@PathParam("id") Long id, ManageTagDTO dto) throws MyEntityNotFoundException {
        Publication publication = publicationBean.findInitialized(id);
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

        publicationBean.updateTags(id, dto);
        publication = publicationBean.findInitialized(id);
        return PublicationDTO.from(publication);
    }

    @GET
    @Path("/")
    public Response getPublications(
            @QueryParam("title") String title,
            @QueryParam("author") String author,
            @QueryParam("tag") String tag,
            @QueryParam("area") String area,
            @QueryParam("date") LocalDate date,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("order") String order
    ) {

        List<Publication> publications =
                publicationBean.findWithFilters(
                        title, author, tag, area, date, sortBy, order
                );

        // privileged users see all comments
        boolean privileged = securityContext.isUserInRole("RESPONSIBLE")
                || securityContext.isUserInRole("ADMINISTRATOR");

        List<PublicationDTO> dtos = PublicationDTO.from(publications);

        if (!privileged) {
            // for Colaborator and other non-privileged users keep only visible comments
            dtos.forEach(dto -> {
                if (dto.getComments() != null) {
                    dto.setComments(dto.getComments().stream()
                            .filter(c -> c.isVisible())
                            .collect(Collectors.toList()));
                }
            });
        }

        return Response.ok(dtos).build();
    }

    @GET
    @Path("/{id}/summary")
    @Authenticated
    public Response generateSummary(@PathParam("id") Long id) throws MyEntityNotFoundException {

        Publication pub = publicationBean.generateAndStoreSummary(id);

        boolean privileged = securityContext.isUserInRole("COLABORATOR");

        PublicationSummaryDTO dto = new PublicationSummaryDTO(pub.getId(),pub.getSummary());

        return Response.ok()
                .entity(dto)
                .header("Allow", "GET, POST, PUT, DELETE, OPTIONS")
                .build();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    public PublicationDTO getPublication(@PathParam("id") long id) throws MyEntityNotFoundException {
        Publication publication = publicationBean.findInitialized(id);

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
    @Authenticated
    public List<PublicationDTO> getMyPublications() {
        String username = securityContext.getUserPrincipal().getName();
        return PublicationDTO.from(publicationBean.findByAuthorInitialized(username));
    }

    @GET
    @Path("/{id}/history")
    public List<HistoryDTO> getPublicationHistory(@PathParam("id") Long publicationId) throws MyEntityNotFoundException {
        return HistoryDTO.from(historyBean.findByPublicationId(publicationId));
    }

    @POST
    @Path("/{id}/comments")
    @Authenticated
    public Response addComment(@PathParam("id") Long publicationId, CommentDTO dto) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        Comment comment = commentBean.create(username, publicationId, dto.content);
        return Response.status(Response.Status.CREATED).header("Allow", "GET, POST, PUT, DELETE, OPTIONS").entity(CommentDTO.from(comment)).build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}")
    @Authenticated
    public Response editComment(@PathParam("id") Long publicationId, @PathParam("commentId") Long commentId, CommentDTO dto) {
        String username = securityContext.getUserPrincipal().getName();
        Comment comment = commentBean.editComment(commentId,publicationId, username, dto.content);
        return Response.ok(CommentDTO.from(comment)).build();
    }

    @PUT
    @Path("/{id}/comments/{commentId}/visibility")
    @Authenticated
    @RolesAllowed({"Administrator", "Responsible"})
    public Response setVisibilityComments(@PathParam("id") Long publicationId, @PathParam("commentId") Long commentId, CommentDTO dto) {
        Comment updatedComment = commentBean.setVisible(commentId, publicationId, dto.isVisible());
        return Response.ok(CommentDTO.from(updatedComment)).header("Allow", "GET, POST, PUT, DELETE, OPTIONS").build();
    }

    @DELETE
    @Path("/{id}/comments/{commentId}")
    @Authenticated
    public Response deleteComment(@PathParam("id") Long publicationId, @PathParam("commentId") Long commentId) {
        String username = securityContext.getUserPrincipal().getName();
        commentBean.deleteComment(commentId, publicationId, username);
        return Response.ok("{\"message\":\"Comment deleted.\"}").build();
    }


    @POST
    @Path("/{id}/ratings")
    @Authenticated
    public Response addRating(@PathParam("id") Long publicationId, RatingDTO dto) throws MyEntityNotFoundException {
        String username = securityContext.getUserPrincipal().getName();
        Rating rating = ratingBean.create(username, publicationId, dto.score);
        return Response.status(Response.Status.CREATED).header("Allow", "GET, POST, PUT, DELETE, OPTIONS").entity(RatingDTO.from(rating)).build();
    }

    @PUT
    @Path("/{id}/ratings/{ratingId}")
    @Authenticated
    public Response editRating(@PathParam("id") Long publicationId, @PathParam("ratingId") Long ratingId, RatingDTO dto) {
        String username = securityContext.getUserPrincipal().getName();
        //ratingBean.delete(ratingId, publicationId, username);
        //Rating rating = ratingBean.create(username, publicationId, dto.score);
        Rating rating = ratingBean.editRating(ratingId,publicationId, username, dto.score);
        return Response.ok(RatingDTO.from(rating)).build();
    }

    @DELETE
    @Path("/{id}/ratings/{ratingId}")
    @Authenticated
    public Response deleteRating(@PathParam("id") Long publicationId, @PathParam("ratingId") Long ratingId) {
        String username = securityContext.getUserPrincipal().getName();
        ratingBean.delete(ratingId, publicationId, username);
        return Response.ok("{\"message\":\"Rating deleted.\"}").build();
    }
}
