package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "getAllTags",
                query = "SELECT t FROM Tag t ORDER BY t.name"
        )
})
@Table(name = "tags")
public class Tag {
    @Id
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<Publication> publications;

    @ManyToMany(mappedBy = "subscriptions")
    private List<User> subscriptions;

    public Tag() {}

    public Tag(String name, List<Publication> publications, List<User> subscriptions) {
        this.name = name;
        this.publications = publications;
        this.subscriptions = subscriptions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public List<User> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<User> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
