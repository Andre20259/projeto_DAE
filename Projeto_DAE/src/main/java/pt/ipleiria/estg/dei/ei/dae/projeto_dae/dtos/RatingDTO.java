package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Rating;

import java.util.List;
import java.util.stream.Collectors;

public class RatingDTO {
    public int score;

    public RatingDTO() {
    }

    public RatingDTO(int score) {
        this.score = score;
    }

    public static RatingDTO from(Rating rating) {
        return new RatingDTO(rating.getScore());
    }

    public static List<RatingDTO> from(List<Rating> ratings) {
        return ratings.stream().map(RatingDTO::from).collect(Collectors.toList());
    }
}
