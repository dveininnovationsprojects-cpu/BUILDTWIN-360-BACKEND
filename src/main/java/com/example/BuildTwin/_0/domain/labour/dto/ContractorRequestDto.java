package com.example.BuildTwin._0.domain.labour.dto;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO for creating or updating a Contractor")
public class ContractorRequestDto {

    @NotBlank(message = "Contractor code is required")
    @Schema(description = "Unique code for the contractor", example = "CON-101")
    private String contractorCode;

    @NotBlank(message = "Contractor name is required")
    @Schema(description = "Full name of contractor", example = "Ramesh Kumar")
    private String name;

    @NotBlank(message = "Company name is required")
    @Schema(description = "Name of contractor agency/firm", example = "Ramesh Electricals & Civil")
    private String companyName;

    @NotNull(message = "Trade specialization is required")
    @Schema(description = "Trade category specialization", example = "ELECTRICIAN")
    private TradeCategory tradeSpecialization;

    @NotBlank(message = "Contact number is required")
    @Schema(description = "Primary phone number", example = "+919876543210")
    private String contactNumber;

    @Email(message = "Invalid email format")
    @Schema(description = "Email address", example = "ramesh@electricals.com")
    private String email;

    @Schema(description = "Office/permanent address", example = "Padur, OMR, Chennai")
    private String address;

    @Schema(description = "Contractor type: MAIN_CONTRACTOR or SUBCONTRACTOR", example = "MAIN_CONTRACTOR")
    private com.example.BuildTwin._0.domain.labour.enums.ContractorType contractorType;

    @Schema(description = "Parent contractor ID if this is a subcontractor", example = "1")
    private Long parentContractorId;

    @Schema(description = "Status of contractor", example = "ACTIVE")
    private String status;
}
