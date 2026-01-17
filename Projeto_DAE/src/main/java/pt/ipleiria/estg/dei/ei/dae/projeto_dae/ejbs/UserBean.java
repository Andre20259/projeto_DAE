// java
package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;

import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class UserBean {
    @PersistenceContext
    private EntityManager entityManager;

    public User find(String username) {
        return entityManager.find(User.class, username);
    }

    public boolean canLogin(String username, String password) {
        var user = find(username);
        return user != null && Hasher.verify(password, user.getPassword());
    }
}
