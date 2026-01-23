package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public class User {
    @Id
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private boolean isActive = true;

    @Version
    private int version;

    @ManyToMany(mappedBy = "authors")
    private List<Publication> publications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @ManyToMany(mappedBy = "subscriptions")
    private List<Tag> subscribedTags = new ArrayList<>();

    public User() {
    }

    public User(String username, String password, String email, String name) {
        setUsername(username);
        setPassword(password);
        setEmail(email);
        setName(name);
        setActive(true);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void addPublication(Publication publication) {
        this.publications.add(publication);
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void addRating(Rating rating) {this.ratings.add(rating);}

    public void removePublication(Publication publication) {
        this.publications.remove(publication);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }

    public void removeRating(Rating rating) { this.ratings.remove(rating);}

    public List<Publication> getPublications() {
        return publications;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Tag> getSubscribedTags() {
        return subscribedTags;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = ratings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return username != null && username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

}
