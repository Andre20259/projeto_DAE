package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;

import java.io.Serializable;

@Entity
@NamedQueries({
        @NamedQuery(
                name = "getAllAdministrators",
                query = "SELECT a FROM Administrator a ORDER BY a.username"
        )
})

@DiscriminatorValue("ADMIN")
public class Administrator extends User implements Serializable {

    public Administrator() {
    }

    public Administrator(String username, String password, String email, String name) {
        super(username, password, email, name);
    }
}
