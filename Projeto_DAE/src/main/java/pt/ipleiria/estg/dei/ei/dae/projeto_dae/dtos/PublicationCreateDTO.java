package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;


import java.util.List;

public class PublicationCreateDTO {
    private String title;

    private String description;

    // send author usernames
    private List<String> authors;

    // send tag names
    private List<String> tags;

    public PublicationCreateDTO() {
    }

    public PublicationCreateDTO(String title, String description, List<String> authors, List<String> tags) {
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public List<String> getAuthors() {
        return authors;
    }
    public List<String> getTags() {
        return tags;
    }
}
