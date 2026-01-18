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
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs.PublicationBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Authenticated;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Path("publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class PublicationService {
    @Context
    private SecurityContext securityContext;
    @EJB
    private PublicationBean publicationBean;

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

    @GET
    @Path("/")
    public List<PublicationDTO> getAllPublications() {
        return PublicationDTO.from(publicationBean.findAll());
    }
}

