package com.example.frontend_chat.DTO;

import com.example.frontend_chat.DTO.Enum.ContactStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("contact")
    private SimpleUserDTO contact;
    @JsonProperty("status")
    private ContactStatus status;

}
