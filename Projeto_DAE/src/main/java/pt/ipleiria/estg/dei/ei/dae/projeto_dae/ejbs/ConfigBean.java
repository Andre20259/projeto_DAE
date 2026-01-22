package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import java.util.logging.Logger;

@Singleton
@Startup
public class ConfigBean {

    @EJB
    private AdministratorBean administratorBean;

    @EJB
    private ColaboratorBean colaboratorBean;

    @EJB
    private ResponsibleBean responsibleBean;

    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @PostConstruct
    public void populateDB(){
       System.out.println("Hello!!");

        try{
            administratorBean.create("admin", "admin", "admin", "admin1@mail.pt");
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

    }
}
