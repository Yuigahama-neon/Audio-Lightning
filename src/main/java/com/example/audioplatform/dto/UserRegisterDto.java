package com.example.audioplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegisterDto {

    @NotBlank(message = "Введите имя пользователя")
    @Size(max = 80, message = "Имя пользователя слишком длинное")
    private String username;

    @NotBlank(message = "Введите email")
    @Email(message = "Введите корректный email")
    @Size(max = 160, message = "Email слишком длинный")
    private String email;

    @NotBlank(message = "Введите пароль")
    @Size(min = 6, message = "Пароль должен быть не короче 6 символов")
    private String password;

    @NotBlank(message = "Повторите пароль")
    private String repeatPassword;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
