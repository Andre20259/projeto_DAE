package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Responsible;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;

import java.util.List;

@Stateless
public class ResponsibleBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String username, String password, String name, String email) throws MyEntityExistsException {
        if (entityManager.find(Responsible.class, username) != null) {
            throw new MyEntityExistsException(
                    "Responsible with username '" + username + "' already exists");
        }
        try{
            Responsible responsible = new Responsible(username, Hasher.hash(password), email, name);
            entityManager.persist(responsible);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(String username, String password, String name, String email) throws MyEntityNotFoundException {
        Responsible responsible = entityManager.find(Responsible.class, username);
        if (responsible == null) {
            throw new MyEntityNotFoundException("Responsible with username '" + username + "' not found");
        }
        entityManager.lock(responsible, LockModeType.OPTIMISTIC);
        responsible.setPassword(Hasher.hash(password));
        responsible.setName(name);
        responsible.setEmail(email);
    }

    public Responsible find(String username) throws MyEntityNotFoundException {
        var responsible = entityManager.find(Responsible.class, username);
        if(responsible == null) throw new MyEntityNotFoundException("Not found");
        return responsible;
    }

    public List<Responsible> findAll() {
        return entityManager.createNamedQuery("getAllResponsibles", Responsible.class).getResultList();
    }

    public void delete(String username) throws MyEntityNotFoundException {
        Responsible responsible = find(username);
        entityManager.remove(responsible);
    }
}
