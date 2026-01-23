package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationCreateDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
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

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private CommentBean commentBean;

    @EJB
    private RatingBean ratingBean;

    @EJB
    private TagBean tagBean;

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
        try{
            colaboratorBean.create("deleted_user", "password", "Deletec User", "deleted@mail.pt");
            colaboratorBean.create("johndoe123", "password123", "JohnDoe", "test@mail.pt");
            colaboratorBean.create("maria", "password123", "Maria", "maria@mail.pt");
            colaboratorBean.create("manel", "password123", "Manel", "manel@mail.pt");
            colaboratorBean.create("raquel", "password123", "Raquel", "raquel@mail.pt");
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }
        try{
            responsibleBean.create("resp", "password123", "Responsavel", "resp1@mail.pt");
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

        try{
            tagBean.create("Science");
            tagBean.create("AI");
            tagBean.create("HiddenTag");
            var hiddenTag = tagBean.find("HiddenTag");
            hiddenTag.setVisible(false);
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

        PublicationCreateDTO dto = new PublicationCreateDTO();
        dto.setTitle("Primeira Publicação");
        dto.setDescription("Publicação inicial");
        dto.setArea("Software");
        dto.setAuthors(List.of("admin"));
        dto.setTags(List.of("Science"));

        InputStream dummyFile =
                new ByteArrayInputStream("conteudo fake".getBytes());

        PublicationCreateDTO hiddenDto = new PublicationCreateDTO();
        hiddenDto.setTitle("Publicação Oculta");
        hiddenDto.setDescription("Não deve aparecer no GET público");
        hiddenDto.setArea("Software");
        hiddenDto.setAuthors(List.of("admin"));
        hiddenDto.setTags(List.of("AI"));

        InputStream hiddenFile =
                new ByteArrayInputStream("conteudo oculto".getBytes());

        try{
            publicationBean.create("file.txt", dummyFile, dto);
            var hiddenPub = publicationBean.create("hidden.txt", hiddenFile, hiddenDto);
            hiddenPub.setVisible(false);
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

        try{
            commentBean.create("johndoe123", 1L, "Good publication");
            commentBean.create("raquel", 1L, "Very informative");
            var hiddenComment =
                    commentBean.create("johndoe123", 1L, "Comentário oculto");
            hiddenComment.setVisible(false);
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

        try{
            ratingBean.create("johndoe123", 1L, 5);
            ratingBean.create("manel", 1L, 5);

        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

    }
}
