package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import java.io.Serializable;

public class Colaborator extends User implements Serializable {

    public Colaborator() {
    }

    public Colaborator(String username, String password, String email, String name) {
        super(username, password, email, name);
    }
}
