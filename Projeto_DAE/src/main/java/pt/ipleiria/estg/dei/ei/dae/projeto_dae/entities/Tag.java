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

    private boolean visible = true;

    @ManyToMany(mappedBy = "tags")
    private List<Publication> publications;

    @ManyToMany
    @JoinTable(
            name = "user_subscribed_tags",
            joinColumns = @JoinColumn(name = "tag_name"),
            inverseJoinColumns = @JoinColumn(name = "user_username")
    )
    private List<User> subscriptions;

    public Tag() {}

    public Tag(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() { return visible; }

    public void setVisible(boolean visible) { this.visible = visible; }


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

    public void addPublication(Publication publication) {
        this.publications.add(publication);
    }
}