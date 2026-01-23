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
        user.addRating(rating);
        return rating;
    }

    public Rating editRating(Long ratingID, Long publicationId,String username, int newScore) {
        Rating rating = entityManager.find(Rating.class, ratingID);
        if (rating == null) {
            throw new RuntimeException("Rating with id " + ratingID + " not found");
        }
        if(!rating.getPublication().getId().equals(publicationId)){
            throw new RuntimeException("Rating with id " + ratingID + " does not belong to publication with id " + publicationId);
        }
        if(!rating.getUser().getUsername().equals(username)){
            throw new RuntimeException("User " + username + " is not the author of rating with id " + ratingID);
        }
        rating.setScore(newScore);
        return rating;
    }

    public void delete(Long ratingId, Long publicationId, String username) {
        Rating rating = entityManager.find(Rating.class, ratingId);
        if (rating == null) {
            throw new RuntimeException("Rating with id " + ratingId + " not found");
        }
        if(!rating.getPublication().getId().equals(publicationId)){
            throw new RuntimeException("Rating with id " + ratingId + " does not belong to publication with id " + publicationId);
        }
        if(!rating.getUser().getUsername().equals(username)){
            throw new RuntimeException("User " + username + " is not the author of rating with id " + ratingId);
        }

        Publication publication = rating.getPublication();
        if (publication != null) {
            publication.removeRating(rating);
        }
        User user = rating.getUser();
        if (user != null) {
            user.removeRating(rating);
        }

        entityManager.remove(rating);
    }


}
