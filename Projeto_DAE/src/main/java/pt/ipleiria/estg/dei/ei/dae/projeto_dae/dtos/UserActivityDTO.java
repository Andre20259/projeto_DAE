package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import java.io.Serializable;

public class UserActivityDTO implements Serializable {
    public long uploads;
    public long edits;
    public long ratings_given;
    public long comments_posted;
    public String last_activity;

    public UserActivityDTO() {}

    public UserActivityDTO(long uploads, long edits, long ratings_given,
                           long comments_posted, String last_activity) {
        this.uploads = uploads;
        this.edits = edits;
        this.ratings_given = ratings_given;
        this.comments_posted = comments_posted;
        this.last_activity = last_activity;
    }
}