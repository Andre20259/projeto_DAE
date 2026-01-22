package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Stateless
public class PublicationBean {
    private static final String PUBLICATION_DIR = "/tmp/publications";
    @EJB
    private UserBean userBean;
    @EJB
    private TagBean tagBean;

    @PersistenceContext
    private EntityManager entityManager;

    public Publication create(String filename, InputStream stream, PublicationCreateDTO dto)
            throws MyEntityNotFoundException, IOException {

        // Retrieve author and tag entities
        List<String> authors = dto.getAuthors();
        List<String> tags = dto.getTags();

        if (authors == null || authors.isEmpty()) {
            throw new IllegalArgumentException("Publication must have at least one author");
        }

        if (tags == null || tags.isEmpty()) {
            throw new IllegalArgumentException("Publication must have at least one tag");
        }

        List<User> authorEntities = new ArrayList<>();
        List<Tag> tagEntities = new ArrayList<>();

        for (String author : authors) {
            User user = userBean.find(author);
            authorEntities.add(user);
        }

        for (String tagName : tags) {
            Tag tag = tagBean.find(tagName);
            tagEntities.add(tag);
        }

        // Store the file
        var targetDirectoryPath = Paths.get(PUBLICATION_DIR);
        if (!Files.exists(targetDirectoryPath)) {
            Files.createDirectories(targetDirectoryPath);
        }
        var targetFilePath = targetDirectoryPath
                .resolve("file_" + UUID.randomUUID());
        Files.copy(stream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Create and persist the publication
        var publication = new Publication(
                dto.getTitle(),
                filename,
                targetFilePath.toString(),
                java.time.LocalDateTime.now(),
                dto.getDescription(),
                dto.getArea(),
                authorEntities,
                tagEntities
        );
        entityManager.persist(publication);

        return  publication;
    }

    public List<Publication> findAll() {
        return entityManager
                .createNamedQuery("getAllPublications", Publication.class)
                .getResultList();
    }

    public Publication find(Long id) {
        return entityManager.find(Publication.class, id);
    }

    public Publication generateAndStoreSummary(Long publicationId) {

        Publication pub = entityManager.find(Publication.class, publicationId);
        String llmBaseUrl = "https://localhost:11434/";

        if (pub == null) {
            throw new NotFoundException("Publication not found");
        }

        // Avoid regenerating unless explicitly desired
        if (pub.getSummary() != null && !pub.getSummary().isBlank()) {
            return pub;
        }

        // ---------- AI PROMPT ----------
        String prompt = """
        Summarize the following publication in a concise academic style.
        Limit the summary to 5 sentences.

        Title: %s

        Content:
        %s
        """.formatted(pub.getTitle(), pub.getDescription());

        // ---------- REQUEST BODY ----------
        JsonObject body = Json.createObjectBuilder()
                .add("model", "qwen2.5:3b")
                .add("prompt", prompt)
                .add("stream", false)
                .build();

        // ---------- HTTP CALL ----------
        Client client = ClientBuilder.newClient();
        JsonObject res = client
                .target(llmBaseUrl + "/api/generate")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body), JsonObject.class);

        // ---------- STORE RESULT ----------
        String summary = res.getString("response").trim();
        pub.setDescription(summary);

        return pub;
    }
}
