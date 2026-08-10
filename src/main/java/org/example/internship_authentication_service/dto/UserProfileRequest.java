package org.example.internship_authentication_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class UserProfileRequest {

    Long authUserId;

    String name;

    String surname;

    LocalDate birthdate;

    String email;

}
