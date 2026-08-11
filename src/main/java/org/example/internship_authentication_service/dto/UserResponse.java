package org.example.internship_authentication_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.internship_authentication_service.entity.Role;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String login;
    private Role role;
    private Boolean enabled;
}
