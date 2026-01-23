package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.History;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class HistoryBean{
    @PersistenceContext
    private EntityManager entityManager;

    public History find(long id) {
        return entityManager.find(History.class, id);
    }

    public List<History> findAll() {
        return entityManager.createQuery("SELECT h FROM History h", History.class)
                .getResultList();
    }

    public List<History> findByAuthor(String author) {
        return entityManager.createQuery("SELECT h FROM History h WHERE h.author = :author", History.class)
                .setParameter("author", author)
                .getResultList();
    }

    public List<History> findByPublicationId(Long publicationId) {
          List<History> list = entityManager
                .createNamedQuery("getHistoryByPublicationId", History.class)
                .setParameter("publicationId", publicationId)
                .getResultList();
        return list;
    }

    public void create(String changes, User author, Publication publication, LocalDateTime date){
        try{
            History history = new History(changes, author, publication, date);
            entityManager.persist(history);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void delete(long id){
        if( entityManager.find(History.class, id) == null ){
            History history = find(id);
            entityManager.remove(history);
        }
    }
}
