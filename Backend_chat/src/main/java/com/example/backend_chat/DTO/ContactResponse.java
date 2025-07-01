package com.example.backend_chat.DTO;

import com.example.backend_chat.model.ENUM.ContactStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
//    @JsonSerialize(using = ToStringSerializer.class)
    private ContactStatus status;

}
