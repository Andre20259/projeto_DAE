package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Administrator;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityExistsException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.exceptions.MyEntityNotFoundException;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.security.Hasher;

import java.util.List;

@Stateless
public class AdministratorBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String username, String password, String name, String email) throws MyEntityExistsException {
        if (entityManager.find(Administrator.class, username) != null) {
            throw new MyEntityExistsException(
                    "Administrator with username '" + username + "' already exists");
        }
        try{
            Administrator administrator = new Administrator(username, Hasher.hash(password), email, name);
            entityManager.persist(administrator);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Administrator find(String username) throws MyEntityNotFoundException {
        var administrator = entityManager.find(Administrator.class, username);
        if(administrator == null) throw new MyEntityNotFoundException("Not found");
        return administrator;
    }

    public List<Administrator> findAll(){
        return entityManager.createNamedQuery("getAllAdministrators", Administrator.class).getResultList();
    }

    public void delete(String username) throws MyEntityNotFoundException {
        Administrator administrator = find(username);
        entityManager.remove(administrator);
    }

    public void update(String username, String password, String name, String email) {
        Administrator administrator = entityManager.find(Administrator.class, username);
        if (administrator == null) {
            System.err.println("ERROR_ADMINISTRATOR_NOT_FOUND: " + username);
            return;
        }
        entityManager.lock(administrator, LockModeType.OPTIMISTIC);
        administrator.setPassword(Hasher.hash(password));
        administrator.setName(name);
        administrator.setEmail(email);
    }
}
