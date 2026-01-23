package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "getAllHistory",
                query = "SELECT h FROM History h"
        )

})
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String changes;

    @ManyToOne
    @JoinColumn(name = "author")
    private User author;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "publication_id")
    private Publication publication;

    private LocalDateTime date;

    public History() {}

    public History(String changes, User author, Publication publication, LocalDateTime date) {
        this.changes = changes;
        this.author = author;
        this.publication = publication;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
