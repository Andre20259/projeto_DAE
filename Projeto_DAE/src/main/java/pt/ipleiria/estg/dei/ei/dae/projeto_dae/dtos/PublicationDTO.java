package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;

import java.time.LocalDateTime;
import java.util.List;

public class PublicationDTO {
    public Long id;
    public String title;
    public String filename;
    public LocalDateTime uploadDate;
    public String description;
    public String area;
    public List<UserDTO> authors;
    public List<TagDTO> tags;
    public List<CommentDTO> comments;
    public List<RatingDTO> ratings;

    public PublicationDTO() {}

    public PublicationDTO(Publication p) {
        this.id = p.getId();
        this.title = p.getTitle();
        this.filename = p.getFilename();
        this.uploadDate = p.getUploadDate();
        this.description = p.getDescription();
        this.area = p.getArea();
        this.authors = UserDTO.from(p.getAuthors());
        this.tags = TagDTO.from(p.getTags());
        this.comments = CommentDTO.from(p.getComments());
        this.ratings = RatingDTO.from(p.getRatings());
    }
}

