package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "getAllTags",
                query = "SELECT h FROM History h ORDER BY h.name"
        )
})
@Table(name = "history")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String changes;

    @NotBlank
    private String author;

    @NotNull
    private long publication_id;

    private LocalDateTime date;

    public History() {}

    public History(String changes, String author, long publication_id, LocalDateTime date) {
        this.changes = changes;
        this.author = author;
        this.publication_id = publication_id;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getPublication_id() {
        return publication_id;
    }

    public void setPublication_id(long publication_id) {
        this.publication_id = publication_id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
