package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

public class Publication {
    @ManyToOne
    @JoinColumn(name = "user_username", nullable = false)
    private User user;
}
