package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;

import java.time.LocalDateTime;

public class PublicationDTO {
    public Long id;
    public String title;
    public String filename;
    public LocalDateTime uploadDate;
    public String description;

    public PublicationDTO() {}

    public PublicationDTO(Publication p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.filename = p.getFilename();
        this.uploadDate = p.getUploadDate();
        this.description = p.getDescription();
    }
}

