package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Stateless
public class PublicationBean {
    private static final String PUBLICATION_DIR = "/tmp/publications";

    @PersistenceContext
    private EntityManager entityManager;

    public void create(String filename, InputStream stream, PublicationCreateDTO dto)
            throws MyEntityNotFoundException, IOException {
    }

    public Publication find(Long id) {
        return entityManager.find(Publication.class, id);
    }
}
