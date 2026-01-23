package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.Rating;

import java.util.List;
import java.util.stream.Collectors;

public class RoleDTO {
    public String role;

    public RoleDTO() {
    }

    public RoleDTO(String role) {
        this.role = role;
    }
    public static RoleDTO from(String role) {
        return new RoleDTO(role);
    }
    public static List<RoleDTO> from(List<String> roles) {
        return roles.stream().map(RoleDTO::from).collect(Collectors.toList());
    }
}
