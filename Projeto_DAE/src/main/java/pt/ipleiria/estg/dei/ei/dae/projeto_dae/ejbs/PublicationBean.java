package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class PublicationBean {
    @PersistenceContext
    private EntityManager entityManager;
}
