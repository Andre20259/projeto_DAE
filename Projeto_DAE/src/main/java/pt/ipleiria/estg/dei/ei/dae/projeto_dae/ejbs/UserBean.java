package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.UserActivityDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class UserBean {
    @PersistenceContext
    private EntityManager entityManager;

    public User find(String username) {
        return entityManager.find(User.class, username);
    }

    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public boolean canLogin(String username, String password) {
        var user = find(username);
        boolean result = false;
        if (user != null && Hasher.verify(password, user.getPassword()) && user.isActive()) {
            result = true;
        }
        return result;
    }

    public void update(String username, String name, String email, boolean active) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) {
            throw new MyEntityNotFoundException("Utilizador não encontrado");
        }
        user.setName(name);
        user.setEmail(email);
        user.setActive(active);
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

    // very simple activity aggregation for 3.1 and 3.13
    public UserActivityDTO getActivity(String username) throws MyEntityNotFoundException {
        User user = find(username);
        if (user == null) throw new MyEntityNotFoundException("Utilizador não encontrado");

        long uploads = user.getPublications().size();
        long commentsPosted = user.getComments().size();

        long ratingsGiven = entityManager.createQuery(
                        "SELECT COUNT(r) FROM Rating r WHERE r.user.username = :u",
                        Long.class)
                .setParameter("u", username)
                .getSingleResult();

        // we only have uploadDate in Publication, so use latest upload as last_activity
        LocalDateTime last = user.getPublications().stream()
                .map(p -> p.getUploadDate())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        String lastActivity = last != null ? last.toString() : null;

        return new UserActivityDTO(
                uploads,
                0,                 
                ratingsGiven,
                commentsPosted,
                lastActivity
        );
    }
}