package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationSummaryDTO {
    private Long id;
    private String summary;

    public PublicationSummaryDTO(Long id,String summary) {
        this.summary = summary;
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
