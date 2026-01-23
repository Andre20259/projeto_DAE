package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws.EmailService;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class CommentBean {
    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private EmailService emailService;

    public Comment create(String username, Long publicationId, String content) {
        Publication publication = publicationBean.find(publicationId);
        if(publication == null) {
            throw new RuntimeException("Publication with id " + publicationId + " not found");
        }
        User user = entityManager.find(User.class, username);
        if(user == null) {
            throw new RuntimeException("User " + username + " not found");
        }
        Comment comment = new Comment(user, publication, content);
        comment.setVisible(true);
        entityManager.persist(comment);
        publication.addComment(comment);
        user.addComment(comment);

        List<Tag> tags = publication.getTags();
        List<String> recipients = getReceipientsForTags(tags);
        if (!recipients.isEmpty()) {
            emailService.sendNewCommentNotification(recipients, publication, comment);
        }

        return comment;
    }

    public Comment setVisible(Long commentId, Long publicationId, boolean visible) {
        Comment comment = entityManager.find(Comment.class, commentId);
        if (comment == null) {
            throw new RuntimeException("Comment with id " + commentId + " not found");
        }
        if(!comment.getPublication().getId().equals(publicationId)){
            throw new RuntimeException("Comment with id " + commentId + " does not belong to publication with id " + publicationId);
        }
        comment.setVisible(visible);
        return comment;
    }

    public List<Comment> findHiddenComments(Long publicationId) {
        Publication publication = publicationBean.find(publicationId);
        if(publication == null) {
            throw new RuntimeException("Publication with id " + publicationId + " not found");
        }
        return entityManager.createQuery("SELECT c FROM Comment c WHERE c.publication.id = :pubId AND c.isVisible = false", Comment.class)
                .setParameter("pubId", publicationId)
                .getResultList();
    }

    public Comment editComment(Long commentId, Long publicationId,String username, String newContent) {
        Comment comment = entityManager.find(Comment.class, commentId);
        if (comment == null) {
            throw new RuntimeException("Comment with id " + commentId + " not found");
        }
        if(!comment.getPublication().getId().equals(publicationId)){
            throw new RuntimeException("Comment with id " + commentId + " does not belong to publication with id " + publicationId);
        }
        if(!comment.getUser().getUsername().equals(username)){
            throw new RuntimeException("User " + username + " is not the author of comment with id " + commentId);
        }
        comment.setContent(newContent);

        Publication publication = entityManager.find(Publication.class, publicationId);
        List<Tag> tags = publication.getTags();
        List<String> recipients = getReceipientsForTags(tags);
        if (!recipients.isEmpty()) {
            emailService.sendUpdatedCommentNotification(recipients, publication, comment);
        }

        return comment;
    }

    public void deleteComment(Long commentId, Long publicationId, String username) {
        Comment comment = entityManager.find(Comment.class, commentId);
        if (comment == null) {
            throw new RuntimeException("Comment with id " + commentId + " not found");
        }
        if(!comment.getPublication().getId().equals(publicationId)){
            throw new RuntimeException("Comment with id " + commentId + " does not belong to publication with id " + publicationId);
        }
        if(!comment.getUser().getUsername().equals(username)){
            throw new RuntimeException("User " + username + " is not the author of comment with id " + commentId);
        }
        Publication publication = comment.getPublication();
        User user = comment.getUser();
        publication.removeComment(comment);
        user.removeComment(comment);
        entityManager.remove(comment);
    }

    private List<String> getReceipientsForTags(List<Tag> tags) {
        List<String> recipients = new ArrayList<>();
        for (Tag t : tags) {
            if (t.getSubscriptions() == null) continue;
            for (User u : t.getSubscriptions()) {
                if (u != null && u.getEmail() != null && !u.getEmail().isBlank()) {
                    recipients.add(u.getEmail());
                }
            }
        }
        return new ArrayList<>(recipients);
    }
}
