package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ejbs;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos.PublicationCreateDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    private TagBean tagBean;

    @EJB
    private PublicationBean publicationBean;

    @EJB
    private RatingBean ratingBean;

    @EJB
    private CommentBean commentBean;



    private static final Logger logger = Logger.getLogger("ejbs.ConfigBean");

    @PostConstruct
    public void populateDB(){
        System.out.println("Hello!!");

        try{
            administratorBean.create("admin", "admin", "admin", "admin1@mail.pt");
            colaboratorBean.create("colab", "colab", "colab", "colab@mail.pt");
            responsibleBean.create("resp", "resp", "resp", "resp@mail.pt");
            colaboratorBean.create("deleted_user", "password", "Deletec User", "deleted@mail.pt");
            colaboratorBean.create("johndoe123", "password123", "JohnDoe", "test@mail.pt");
            colaboratorBean.create("maria", "password123", "Maria", "maria@mail.pt");
            colaboratorBean.create("manel", "password123", "Manel", "manel@mail.pt");
            colaboratorBean.create("raquel", "password123", "Raquel", "raquel@mail.pt");

            logger.info("Default users created.");

            tagBean.create("AI");
            tagBean.create("Technology");
            tagBean.create("Engineering");
            tagBean.create("Science");
            tagBean.create("HiddenTag");
            var hiddenTag = tagBean.find("HiddenTag");
            hiddenTag.setVisible(false);

            PublicationCreateDTO publicationCreateDTO = new PublicationCreateDTO();
            publicationCreateDTO.setTitle("Teste 2");
            publicationCreateDTO.setDescription("Descrição teste");
            publicationCreateDTO.setArea("AI");
            publicationCreateDTO.setAuthors(List.of("admin"));
            publicationCreateDTO.setTags(List.of("AI"));

            // If your PublicationBean create method expects multipart-like content,
            // pass a small dummy file stream (similar to your HTTP request).
            InputStream fileStream = new ByteArrayInputStream("contents of the file".getBytes(StandardCharsets.UTF_8));
            String filename = "file1.txt";
            publicationBean.create(filename, fileStream, publicationCreateDTO);

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

            publicationBean.create("file.txt", dummyFile, dto);
            var hiddenPub = publicationBean.create("hidden.txt", hiddenFile, hiddenDto);
            hiddenPub.setVisible(false);

            commentBean.create("johndoe123", 1L, "Good publication");
            commentBean.create("raquel", 1L, "Very informative");
            var hiddenComment =
                    commentBean.create("johndoe123", 1L, "Comentário oculto");
            hiddenComment.setVisible(false);

            commentBean.create("colab",1L,"Great publication!");
            ratingBean.create("colab",1L,5);
        }
        catch (Exception e){
            logger.severe(e.getMessage());
        }

    }
}
