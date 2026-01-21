package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;

import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO {
    public String content;
    public boolean isVisible;

    public CommentDTO() {
    }

    public CommentDTO(String content, boolean isVisible) {
        this.isVisible = isVisible;
        this.content = content;
    }

    public static CommentDTO from(Comment comment) {
       return new CommentDTO(comment.getContent(), comment.isVisible());
    }

    public static List<CommentDTO> from(List<Comment> comments) {
        return comments.stream().map(CommentDTO::from).collect(Collectors.toList());
    }

    public boolean isVisible() {
        return isVisible;
    }
}
