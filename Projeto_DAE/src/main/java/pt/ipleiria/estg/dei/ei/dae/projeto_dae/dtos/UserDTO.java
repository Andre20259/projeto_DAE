package pt.ipleiria.estg.dei.ei.dae.projeto_dae.dtos;

import pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities.User;

import java.io.Serializable;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private String username;
    private String name;
    private String email;
    private String role;

    public UserDTO() {
    }
    public UserDTO(String username, String name, String email, String role) {
        this.username = username;
        this.name = name;
        this.email = email;
        this.role = role;
    }
    public static UserDTO from(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getClass().getSimpleName()
        );
    }
    public static List<UserDTO> from(List<User> users) {
        return users.stream().map(UserDTO::from).collect(Collectors.toList());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
