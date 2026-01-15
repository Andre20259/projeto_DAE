package pt.ipleiria.estg.dei.ei.dae.projeto_dae.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class SuperUser {
    @Id
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @Version
    private int version;

    public SuperUser() {
    }

    public SuperUser(String username, String password, String email, String name) {
        setUsername(username);
        setPassword(password);
        setEmail(email);
        setName(name);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
}
