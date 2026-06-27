package com.auca.contractsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private String title;
    private String message;
    private String type; // WARNING, PENALTY, INFO
    private String contractId;
    private String studentId;
    private LocalDateTime timestamp;
}
