package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationSummaryDTO {
    private String summary;
    private boolean privileged;

    public PublicationSummaryDTO(String summary, boolean privileged) {
        this.summary = summary;
        this.privileged = privileged;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }
}
