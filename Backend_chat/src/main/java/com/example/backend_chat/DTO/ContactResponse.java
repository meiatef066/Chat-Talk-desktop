package com.example.backend_chat.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactResponse {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("contact")
    private SimpleUserDTO contact;
    @JsonProperty("status")
    private String status;

//    private Long id;
////    private SimpleUserDTO user;
//    private SimpleUserDTO contact;
//    private String status;
////    private LocalDateTime createdAt;
}
