package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

public class VisibilityDTO {
    private boolean visibility;

    public VisibilityDTO() {}

    public VisibilityDTO(boolean visibility) {
        this.visibility = visibility;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
