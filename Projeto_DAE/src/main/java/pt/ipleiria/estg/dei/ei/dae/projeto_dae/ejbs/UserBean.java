package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;

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

    public void update(String username, String name, String email) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");
        user.setName(name);
        user.setEmail(email);
    }

    public void updatePassword(String username, String oldPassword, String newPassword) throws Exception {
        User user = find(username);
        if (user == null || !Hasher.verify(oldPassword, user.getPassword())) {
            throw new Exception("A password antiga está incorreta");
        }
        user.setPassword(Hasher.hash(newPassword));
    }

    public void delete(String username) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");
        entityManager.remove(user);
    }
}