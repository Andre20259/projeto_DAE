package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "publications")
public class Publication {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @NotBlank
    private String title;

    @NotBlank
    private String filename;

    @NotBlank
    private String filePath;

    @NotNull
    private LocalDateTime uploadDate;

    private String description;

    @NotBlank
    private String area;

    @ManyToMany
    @JoinTable(
            name = "publication_authors",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> authors;

    @ManyToMany
    @JoinTable(
            name = "publication_tags",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "publication")
    private List<Comment> comments;

    @OneToMany(mappedBy = "publication")
    private List<Rating> ratings;

    public Publication() {
    }

    public Publication(String title, String filename, String filePath,
                       LocalDateTime uploadDate, String description, String area, List<User> authors, List<Tag> tags) {
        this.title = title;
        this.filename = filename;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.description = description;
        this.area = area;
        this.authors = authors;
        this.tags = tags;
        this.comments = new ArrayList<>();
        this.ratings = new ArrayList<>();
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public List<User> getAuthors() {
        return authors;
    }

    public void setAuthors(List<User> authors) {
        this.authors = authors;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    // add Tag
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    // remove Tag
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    // add Comment
    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    // get Comments
    public List<Comment> getComments() {
        return comments;
    }

    // remove Comment
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }

    // add Rating
    public void addRating(Rating rating) {
        this.ratings.add(rating);
    }

    // get Ratings
    public List<Rating> getRatings() {
        return ratings;
    }

    // remove Rating
    public void removeRating(Rating rating) {
        this.ratings.remove(rating);
    }
}
