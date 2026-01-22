package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationDTO {
    public Long id;
    public String title;
    public String filename;
    public String uploadDate;
    public String description;
    public String area;
    public List<UserDTO> authors;
    public List<TagDTO> tags;
    public List<CommentDTO> comments;
    public List<RatingDTO> ratings;
    public float averageRating;

    public PublicationDTO() {}

    public PublicationDTO(Long id, String title, String filename, String uploadDate,
                          String description, String area, List<UserDTO> authors,
                          List<TagDTO> tags, List<CommentDTO> comments,
                          List<RatingDTO> ratings, float averageRating) {
        this.id = id;
        this.title = title;
        this.filename = filename;
        this.uploadDate = uploadDate;
        this.description = description;
        this.area = area;
        this.authors = authors;
        this.tags = tags;
        this.comments = comments;
        this.ratings = ratings;
        this.averageRating = averageRating;
    }

    public static PublicationDTO from(Publication publication){
        return new PublicationDTO(
                publication.getId(),
                publication.getTitle(),
                publication.getFilename(),
                publication.getUploadDate().toString(),
                publication.getDescription(),
                publication.getArea(),
                UserDTO.from(publication.getAuthors()),
                TagDTO.from(publication.getTags()),
                CommentDTO.from(publication.getComments()),
                RatingDTO.from(publication.getRatings()),
                publication.getAverageRating()
        );
    }

    public static List<PublicationDTO> from(List<Publication> publications){
        return publications.stream().map(PublicationDTO::from).collect(Collectors.toList());
    }

    public List<CommentDTO> getComments(){
        return this.comments;
    }

    public void setComments(List<CommentDTO> comments){
        this.comments = comments;
    }
}

