package com.auca.contractsystem.dto;

import lombok.Data;
import java.util.List;

@Data
public class AucaLoginResponse {
    private String username;
    private String email;
    private String role;
    private String fullName;
    private List<String> permissions;
}
