package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.ManageTagDTO;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public List<Publication> findWithFilters(
            String title,
            String author,
            String tag,
            String area,
            LocalDate date,
            String sortBy,
            String order
    ) {
        StringBuilder jpql = new StringBuilder("SELECT DISTINCT p FROM Publication p");
        boolean joinAuthors = author != null && !author.isBlank();
        boolean joinTags = tag != null && !tag.isBlank();

        if (joinAuthors) jpql.append(" JOIN p.authors a");
        if (joinTags) jpql.append(" JOIN p.tags t");

        jpql.append(" WHERE 1=1");

        if (title != null && !title.isBlank()) {
            jpql.append(" AND LOWER(p.title) LIKE :title");
        }
        if (area != null && !area.isBlank()) {
            jpql.append(" AND LOWER(p.area) = :area");
        }

        if (date != null) {
            jpql.append(" AND p.uploadDate >= :startDate AND p.uploadDate < :endDate");
        }

        if (joinAuthors) {
            jpql.append(" AND LOWER(a.username) = :author");
        }
        if (joinTags) {
            jpql.append(" AND LOWER(t.name) = :tag");
        }

        // Sorting
        String sortExpr = "p.id";
        if ("title".equalsIgnoreCase(sortBy)) sortExpr = "p.title";
        else if ("date".equalsIgnoreCase(sortBy)) sortExpr = "p.uploadDate";
        else if ("area".equalsIgnoreCase(sortBy)) sortExpr = "p.area";

        String sortOrder = "DESC".equalsIgnoreCase(order) ? "DESC" : "ASC";
        jpql.append(" ORDER BY ").append(sortExpr).append(" ").append(sortOrder);

        TypedQuery<Publication> q =
                entityManager.createQuery(jpql.toString(), Publication.class);

        if (title != null && !title.isBlank()) {
            q.setParameter("title", "%" + title.toLowerCase() + "%");
        }
        if (area != null && !area.isBlank()) {
            q.setParameter("area", area.toLowerCase());
        }


        if (date != null) {
            q.setParameter("startDate", date.atStartOfDay());
            q.setParameter("endDate", date.plusDays(1).atStartOfDay());
        }

        if (joinAuthors) {
            q.setParameter("author", author.toLowerCase());
        }
        if (joinTags) {
            q.setParameter("tag", tag.toLowerCase());
        }

        return q.getResultList();
    }


    public Publication find(Long id) {
        return entityManager.find(Publication.class, id);
    }

    public List<Publication> findByAuthor(String author) {
        return entityManager
                .createNamedQuery("getPublicationsByAuthor", Publication.class)
                .setParameter("author", author)
                .getResultList();
    }

    public List<Publication> findAllInitialized() {
        List<Publication> list = entityManager
                .createQuery("SELECT p FROM Publication p", Publication.class)
                .getResultList();

        for (Publication p : list) {
            p.getAuthors().size();
            p.getTags().size();
            p.getComments().size();
            p.getRatings().size();
        }

        return list;
    }

    public Publication findInitialized(Long id) throws MyEntityNotFoundException {
        Publication p = entityManager.find(Publication.class, id);

        if (p == null) {
            throw new MyEntityNotFoundException("Publication not found");
        }

        p.getAuthors().size();
        p.getTags().size();
        p.getComments().size();
        p.getRatings().size();

        return p;
    }

    public List<Publication> findByAuthorInitialized(String username) {
        List<Publication> list = entityManager
                .createNamedQuery("getPublicationsByAuthor", Publication.class)
                .setParameter("author", username)
                .getResultList();

        for (Publication p : list) {
            p.getAuthors().size();
            p.getTags().size();
            p.getComments().size();
            p.getRatings().size();
        }
        return list;
    }



    public Publication updateVisibility(Long id, boolean visible) throws MyEntityNotFoundException {
        Publication publication = entityManager.find(Publication.class, id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found");
        }
        publication.setVisible(visible);
        return publication;
    }

    public Publication updatePublication(Long id, PublicationCreateDTO dto) throws MyEntityNotFoundException {
        Publication publication = entityManager.find(Publication.class, id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found");
        }
        publication.setTitle(dto.getTitle());
        publication.setDescription(dto.getDescription());
        publication.setArea(dto.getArea());
        return publication;
    }

    public Publication updateTags(Long id, ManageTagDTO dto) throws MyEntityNotFoundException {
        Publication publication = entityManager.find(Publication.class, id);
        if (publication == null) {
            throw new MyEntityNotFoundException("Publication not found");
        }
        Tag tag = tagBean.find(dto.getName());
        if (tag == null) {
            throw new MyEntityNotFoundException("Tag not found");
        }
        if ("add".equals(dto.getAction()) && !tag.isVisible()) {
            throw new IllegalArgumentException("Tag not available: " + dto.getName());
        }

        List<Tag> tags = publication.getTags();

        switch (dto.getAction()) {
            case "add":
                if (tags != null && tags.contains(tag)) {
                    throw new IllegalArgumentException("Publication already has tag: " + dto.getName());
                }
                publication.addTag(tag);
                break;
            case "remove":
                if (tags == null || tags.isEmpty()) {
                    throw new MyEntityNotFoundException("Publication has no tags");
                }
                if (!tags.contains(tag)) {
                    throw new MyEntityNotFoundException("Publication does not have tag: " + dto.getName());
                }
                publication.removeTag(tag);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + dto.getAction());
        }
        return publication;
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
        pub.setSummary(summary);

        entityManager.merge(pub);

        return pub;
    }

}
