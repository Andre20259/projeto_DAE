package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

@Stateless
public class RatingBean {
    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private PublicationBean publicationBean;

    public Rating create(String username, Long publicationId, int score) {
        Publication publication = publicationBean.find(publicationId);
        if(publication == null) {
            throw new RuntimeException("Publication with id " + publicationId + " not found");
        }
        User user = entityManager.find(User.class, username);
        if(user == null) {
            throw new RuntimeException("User " + username + " not found");
        }
        Rating rating = new Rating(user, publication, score);
        entityManager.persist(rating);
        publication.addRating(rating);
        return rating;
    }
}
