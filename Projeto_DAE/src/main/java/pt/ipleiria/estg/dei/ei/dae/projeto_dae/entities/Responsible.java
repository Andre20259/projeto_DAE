package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import java.io.Serializable;

public class Responsible extends User implements Serializable {

    public Responsible() {
    }

    public Responsible(String username, String password, String email, String name) {
        super(username, password, email, name);
    }
}
