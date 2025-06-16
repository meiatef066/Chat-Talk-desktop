package com.example.chat_frontend.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@Data
public class SimpleUserDTO extends ContactResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private Boolean isOnline;
}