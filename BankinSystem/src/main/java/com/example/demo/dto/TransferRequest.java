package com.example.demo.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
    @NotBlank String sourceAccount,
    @NotBlank String destinationAccount,
    @Positive(message = "amount must be positive") Double amount
) {}
