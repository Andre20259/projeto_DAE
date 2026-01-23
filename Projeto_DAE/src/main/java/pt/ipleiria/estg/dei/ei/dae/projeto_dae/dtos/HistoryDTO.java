package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.History;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryDTO  implements Serializable {
    private Long id;

    private String changes;

    private String author;

    private long publication_id;

    private LocalDateTime date;

    public HistoryDTO() {}

    public HistoryDTO(Long id, String changes, String author, long publication_id, LocalDateTime date) {}


    public static HistoryDTO from(History history){
            return new HistoryDTO(history.getId(), history.getChanges(),history.getAuthor(),history.getPublication_id(),history.getDate());
    }

    public static List<HistoryDTO> from(List<History> histories){
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
