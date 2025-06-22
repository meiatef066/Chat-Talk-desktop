package com.example.chat_frontend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
public class ContactResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("contact")
    private SimpleUserDTO contact;
    @JsonProperty("status")
    private String status;
}