package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

import java.util.List;

@Stateless
public class TagBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String name){
        Tag existing = entityManager.find(Tag.class, name);
        if (existing != null) {
            throw new RuntimeException("Tag " + name + " already exists");
        }
        Tag tag = new Tag(name, true);
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
        if (!tag.isVisible()) {
            throw new IllegalArgumentException("Tag not available: " + name);
        }
        return tag;
    }

    public Tag findAny(String name){
        Tag tag = entityManager.find(Tag.class, name);
        if (tag == null) {
            throw new RuntimeException("Tag " + name + " not found");
        }
        return tag;
    }

    public List<Tag> findVisible() {
        return entityManager
                .createQuery("SELECT t FROM Tag t WHERE t.visible = true", Tag.class)
                .getResultList();
    }


    public void delete(String name){
        Tag tag = findAny(name);

        for (Publication pub : tag.getPublications()) {
            pub.getTags().remove(tag);
            entityManager.merge(pub);
        }
        tag.getPublications().clear();

        for (User user : tag.getSubscriptions()) {
            user.getSubscribedTags().remove(tag);
            entityManager.merge(user);
        }
        tag.getSubscriptions().clear();

        entityManager.remove(tag);
    }

    public Tag setVisible(String tagName, boolean visible) {
        Tag tag = findAny(tagName);

        if (!visible) {
            for (Publication pub : tag.getPublications()) {
                pub.getTags().remove(tag);
                entityManager.merge(pub);
            }
            tag.getPublications().clear();

            for (User user : tag.getSubscriptions()) {
                user.getSubscribedTags().remove(tag);
                entityManager.merge(user);
            }
            tag.getSubscriptions().clear();
        }

        tag.setVisible(visible);
        return tag;
    }

    public List<Tag> findHidden() {
        return entityManager.createQuery(
                "SELECT t FROM Tag t WHERE t.visible = false",
                Tag.class
        ).getResultList();
    }

    public void subscribeUserToTag(String userName, String tagName) {
        //Tag tag = entityManager.find(Tag.class, tagName);
        Tag tag = find(tagName);
        if(tag == null) {
            throw new RuntimeException("Tag " + tagName + " not found");
        }
        //UserBean userBean = new UserBean();
        //User user = userBean.find(userName);
        User user = entityManager.find(User.class, userName);
        if (user == null) {
            throw new RuntimeException("User " + userName + " not found");
        }
        if(!user.getSubscribedTags().contains(tag)) {
            user.getSubscribedTags().add(tag);
            tag.getSubscriptions().add(user);
            entityManager.merge(user);
            entityManager.merge(tag);
        }
    }

    public void unsubscribeUserFromTag(String userName, String tagName) {
        //Tag tag = entityManager.find(Tag.class, tagName);
        Tag tag = find(tagName);
        if(tag == null) {
            throw new RuntimeException("Tag " + tagName + " not found");
        }
        //UserBean userBean = new UserBean();
        //User user = userBean.find(userName);
        User user = entityManager.find(User.class, userName);
        if (user == null) {
            throw new RuntimeException("User " + userName + " not found");
        }
        if(user.getSubscribedTags().contains(tag)) {
            user.getSubscribedTags().remove(tag);
            tag.getSubscriptions().remove(user);
            entityManager.merge(user);
            entityManager.merge(tag);
        }
    }


}
