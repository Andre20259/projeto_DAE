package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "getAllPublications",
                query = "SELECT pub FROM Publication pub ORDER BY pub.title" // JPQL
        ),
        @NamedQuery(
                name = "getPublicationsByAuthor",
                query = "SELECT p FROM Publication p JOIN p.authors a WHERE a.username = :author")
})
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

    @Lob
    private String summary;

    @NotBlank
    private String area;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "publication_authors",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> authors;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "publication_tags",
            joinColumns = @JoinColumn(name = "publication_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "publication", fetch = FetchType.EAGER)
    private List<Comment> comments;

    @OneToMany(mappedBy = "publication", fetch = FetchType.EAGER)
    private List<Rating> ratings;

    private float averageRating;

    private boolean isVisible;

    @OneToMany(mappedBy = "publication", fetch = FetchType.EAGER)
    private List<History> history;

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
        this.averageRating = 0.0f;
        this.isVisible = true;
        this.summary = null;
        this.history = new ArrayList<>();
    }


    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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
        // update average rating
        calculateAverageRating();
    }

    // remove Rating
    public void removeRating(Rating rating) {
        this.ratings.remove(rating);

        // update average rating
        calculateAverageRating();
    }

    // get Ratings
    public List<Rating> getRatings() {
        return ratings;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public boolean isVisible() {
        return isVisible;
    }
    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    private void calculateAverageRating() {
        if (ratings.isEmpty()) {
            this.averageRating = 0.0f;
            return;
        }
        float total = 0.0f;
        for (Rating r : ratings) {
            total += r.getScore();
        }
        this.averageRating = total / ratings.size();
    }
}
