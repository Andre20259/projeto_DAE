package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.History;

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
        return entityManager.createQuery("SELECT h FROM History h", History.class).getResultList();
    }


    public List<History> findByAuthor(String author) {
        return entityManager.createQuery("SELECT h FROM History h WHERE h.author = author", History.class).getResultList();
    }

    public List<History> findByPublicationId (Long publicationId) {
        return entityManager.createQuery("SELECT h FROM History h WHERE h.publication_id = publication_id", History.class).getResultList();
    }

    public void create(String changes, String author, long publication_id, LocalDateTime date){
        try{
            History history = new History(changes, author, publication_id, date);
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
