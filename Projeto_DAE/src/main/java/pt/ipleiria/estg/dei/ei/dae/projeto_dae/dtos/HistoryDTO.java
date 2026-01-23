package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import jakarta.ejb.Local;
import jakarta.security.enterprise.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.History;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryDTO  implements Serializable {
    private Long id;

    private String changes;

    private String author;

    private Long publicationId;

    private String date;

    public HistoryDTO() {}

    public HistoryDTO(Long id, String changes, String author, Long publicationId, String date) {
        this.id = id;
        this.changes = changes;
        this.author = author;
        this.publicationId = publicationId;
        this.date = date;
    }

    public static HistoryDTO from(History history) {
        return new HistoryDTO(
                history.getId(),
                history.getChanges(),
                history.getAuthor().getName(),
                history.getPublication().getId(),
                history.getDate().toString()
        );
    }

    public static List<HistoryDTO> from(List<History> histories) {
        return histories.stream().map(HistoryDTO::from).collect(Collectors.toList());
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getPublication() {
        return publicationId;
    }

    public void setPublication(Long publication) {
        this.publicationId = publication;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
