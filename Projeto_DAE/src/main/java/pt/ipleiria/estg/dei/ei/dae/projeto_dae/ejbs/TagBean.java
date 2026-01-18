package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

import java.util.List;

public class TagBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String name){
        Tag tag = new Tag();
        entityManager.persist(tag);
    }

    public List<Tag> findAll(){
        return entityManager.createNamedQuery("getAllTags", Tag.class).getResultList();
    }

    public Tag find(String name){
        var tag = entityManager.find(Tag.class, name);
        if (tag == null) {
            throw new RuntimeException("Tag " + name + " not found");
        }
        return tag;
    }

    public void subscribeUserToTag(String userName, String tagName) {
        Tag tag = entityManager.find(Tag.class, tagName);
        if(tag == null) {
            throw new RuntimeException("Tag " + tagName + " not found");
        }
        UserBean userBean = new UserBean();
        //User user = userBean.find(userName);
        User user = entityManager.find(User.class, userName);
        if (user == null) {
            throw new RuntimeException("User " + userName + " not found");
        }
        user.getSubscribedTags().add(tag);
        tag.getSubscriptions().add(user);
        entityManager.merge(user);
        entityManager.merge(tag);
    }
}
