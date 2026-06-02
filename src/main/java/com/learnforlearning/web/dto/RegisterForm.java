package com.learnforlearning.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterForm {

    @NotBlank(message = "Meg kell adnod a neved!")
    private String name;

    @NotBlank(message = "Meg kell adnod az e-mail címed!")
    @Email(message = "Érvényes e-mail címet adj meg!")
    private String email;

    @NotBlank(message = "Meg kell adnod egy jelszót!")
    @Size(min = 8, message = "A jelszónak legalább 8 karakter hosszúnak kell lennie!")
    private String password;

    @NotBlank(message = "Meg kell erősítened a jelszót!")
    private String passwordConfirmation;

    public boolean passwordsMatch() {
        return password != null && password.equals(passwordConfirmation);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }
}
