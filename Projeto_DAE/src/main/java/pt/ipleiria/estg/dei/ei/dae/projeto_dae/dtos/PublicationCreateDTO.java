package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;


import java.util.List;

public class PublicationCreateDTO {
    private String title;

    private String description;

    private String area;

    // send author usernames
    private List<String> authors;

    // send tag names
    private List<String> tags;

    public PublicationCreateDTO() {
    }

    public PublicationCreateDTO(String title, String description,String area, List<String> authors, List<String> tags) {
        this.title = title;
        this.description = description;
        this.area = area;
        this.authors = authors;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getArea() {return area;}
    public List<String> getAuthors() {
        return authors;
    }
    public List<String> getTags() {
        return tags;
    }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setArea(String area) { this.area = area; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
