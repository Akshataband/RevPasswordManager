package com.RevPasswordManager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Master password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String masterPassword;

    private List<QuestionAnswer> securityQuestions;
}