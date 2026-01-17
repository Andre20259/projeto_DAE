package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

public class UserBean {
    @PersistenceContext
    private EntityManager entityManager;
    public User find(String username) {
        return entityManager.find(User.class, username);
    }
}
