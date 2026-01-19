package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Tag;

import java.util.List;
import java.util.stream.Collectors;

public class TagDTO {
    private String name;
    private boolean visible;

    public TagDTO() {}

    public TagDTO(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public static TagDTO from(Tag tag){
        return new TagDTO(
                tag.getName(),
                tag.isVisible()
        );
    }

    public static List<TagDTO> from(List<Tag> tags){
        return tags.stream().map(TagDTO::from).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
