package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO {
    public Long id;
    public String author;
    public String content;
    public String created_at;
    public boolean visible;


    public CommentDTO() {
    }

    public CommentDTO(Long id, String author, String content, String created_at, boolean visible) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.created_at = created_at;
        this.visible = visible;
    }

    public static CommentDTO from(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getContent(),
                comment.getCreated_at().toString(),
                comment.isVisible());
    }

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream().map(CommentDTO::from).collect(Collectors.toList());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
