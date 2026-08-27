package com.example.BuildTwin._0.domain.materials.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for registering a new Supplier master profile")
public class SupplierCreateDto {

    @NotBlank(message = "Supplier code is required")
    @Schema(description = "Unique supplier code", example = "SUP-1001")
    private String supplierCode;

    @NotBlank(message = "Supplier name is required")
    @Schema(description = "Company / Trade name", example = "Coromandel Building Supplies")
    private String name;

    @Schema(description = "Contact person name", example = "Srinivasan R")
    private String contactPerson;

    @NotBlank(message = "Phone number is required")
    @Schema(description = "Primary phone number", example = "+919444012345")
    private String phone;

    @Email(message = "Invalid email format")
    @Schema(description = "Official email address", example = "sales@coromandelsupplies.com")
    private String email;

    @Schema(description = "GSTIN registration number", example = "33AAAAA0000A1Z5")
    private String gstin;

    @Schema(description = "Registered business address", example = "OMR Road, Sholinganallur, Chennai")
    private String address;

    @Schema(description = "Status of supplier", example = "ACTIVE")
    private String status;
}
