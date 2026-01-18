package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Colaborator;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;

import java.util.List;

@Stateless
public class ColaboratorBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String username, String password, String name, String email) throws MyEntityExistsException {
        if (entityManager.find(Colaborator.class, username) != null) {
            throw new MyEntityExistsException(
                    "Colaborator with username '" + username + "' already exists");
        }
        try{
            Colaborator colaborator = new Colaborator(username, Hasher.hash(password), email, name);
            entityManager.persist(colaborator);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(String username, String password, String name, String email) {
        Colaborator colaborator = entityManager.find(Colaborator.class, username);
        if (colaborator == null) {
            System.err.println("ERROR_COLABORATOR_NOT_FOUND: " + username);
            return;
        }
        entityManager.lock(colaborator, LockModeType.OPTIMISTIC);
        colaborator.setPassword(Hasher.hash(password));
        colaborator.setName(name);
        colaborator.setEmail(email);
    }

    public Colaborator find(String username) throws MyEntityNotFoundException {
        var colaborator = entityManager.find(Colaborator.class, username);
        if(colaborator == null) throw new MyEntityNotFoundException("Not found");
        return colaborator;
    }

    public List<Colaborator> findAll(){
        return entityManager.createNamedQuery("getAllColaborators", Colaborator.class).getResultList();
    }

    public void delete(String username) throws MyEntityNotFoundException {
        Colaborator colaborator = find(username);
        entityManager.remove(colaborator);
    }
}
