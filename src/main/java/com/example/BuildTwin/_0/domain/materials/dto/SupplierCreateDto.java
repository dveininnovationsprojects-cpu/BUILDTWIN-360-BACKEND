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

    @Schema(description = "PAN card number", example = "ABCDE1234F")
    private String panNumber;

    @Schema(description = "Bank account number", example = "50200012345678")
    private String bankAccountNumber;

    @Schema(description = "Bank name", example = "HDFC Bank")
    private String bankName;

    @Schema(description = "Bank IFSC code", example = "HDFC0001234")
    private String bankIfscCode;

    @Schema(description = "Bank branch name", example = "Sholinganallur, Chennai")
    private String bankBranch;

    @Schema(description = "Category specialization mapping (e.g. CEMENT, STEEL, AGGREGATES, RMC)", example = "CEMENT, RMC")
    private String categorySpecialization;

    @Schema(description = "Registered business address", example = "OMR Road, Sholinganallur, Chennai")
    private String address;

    @Schema(description = "Status of supplier (ACTIVE, PENDING_VERIFICATION, BLACKLISTED)", example = "ACTIVE")
    private String status;
}
