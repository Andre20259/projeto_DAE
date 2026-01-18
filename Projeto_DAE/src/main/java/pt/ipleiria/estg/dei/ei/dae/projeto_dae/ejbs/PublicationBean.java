package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    public void create(String filename, InputStream stream, PublicationCreateDTO dto)
            throws MyEntityNotFoundException, IOException {

        List<String> authors = dto.getAuthors();
        List<String> tags = dto.getTags();

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

        var targetDirectoryPath = Paths.get(PUBLICATION_DIR);
        if (!Files.exists(targetDirectoryPath)) {
            Files.createDirectories(targetDirectoryPath);
        }
        var targetFilePath = targetDirectoryPath
                .resolve("file_" + UUID.randomUUID());
        Files.copy(stream, targetFilePath, StandardCopyOption.REPLACE_EXISTING);

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
    }

    public Publication find(Long id) {
        return entityManager.find(Publication.class, id);
    }
}
