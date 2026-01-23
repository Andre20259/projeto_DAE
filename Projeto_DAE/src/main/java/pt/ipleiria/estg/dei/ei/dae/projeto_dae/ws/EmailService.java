package pt.ipleiria.estg.dei.ei.dae.projeto_dae.ws;

import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Publication;

import java.util.List;
import java.util.Properties;

@Stateless
public class EmailService {

    private String USERNAME = "df02aa68725d91";
    private String PASSWORD = "634d7e2a34e26f";

    public void sendPublicationNotification(List<String> to, Publication publication) {
        String subject = "New publication: " + publication.getTitle();
        StringBuilder body = new StringBuilder();
        body.append("A new publication matching your subscribed tags was uploaded.\n\n");
        body.append("Title: ").append(publication.getTitle()).append("\n");
        body.append("Area: ").append(publication.getArea()).append("\n\n");
        body.append("Description:\n").append(publication.getDescription()).append("\n\n");
        body.append("View the publication in the application.");
        String bodyText = body.toString();
        for (String recipient : to) {
            sendEmail(recipient, subject, bodyText);
        }
    }

    public void sendEmail(String to, String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "2525");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        USERNAME,
                        PASSWORD
                );
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("noreply@dae.local"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
