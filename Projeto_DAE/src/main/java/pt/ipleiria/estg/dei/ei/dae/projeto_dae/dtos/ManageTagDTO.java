package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

public class ManageTagDTO {
    private String name;
    private String action;

    public ManageTagDTO() {
    }

    public ManageTagDTO(String name, String action) {
        this.name = name;
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
