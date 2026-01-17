package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

import java.util.List;

@Entity
public class Publication {

    @ManyToMany(mappedBy = "publications")
    private List<Tag> tags;
}
