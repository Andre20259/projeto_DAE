package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import java.io.Serializable;

public class Administrator extends User implements Serializable {

    public Administrator() {
    }

    public Administrator(String username, String password, String email, String name) {
        super(username, password, email, name);
    }
}
