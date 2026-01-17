package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.ManyToMany;

@Entity

public class Publication {
    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;

    @ManyToMany(mappedBy = "publications")
    private List<Tag> tags;
}
