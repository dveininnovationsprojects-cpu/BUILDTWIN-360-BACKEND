package com.example.BuildTwin._0.domain.labour.model;

import com.example.BuildTwin._0.domain.labour.enums.TradeCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "contractors", indexes = {
        @Index(name = "idx_contractor_code", columnList = "contractor_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Contractor code is required")
    @Column(name = "contractor_code", nullable = false, unique = true, length = 50)
    private String contractorCode;

    @NotBlank(message = "Contractor name is required")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @NotBlank(message = "Company name is required")
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @NotNull(message = "Trade specialization is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "trade_specialization", nullable = false, length = 50)
    private TradeCategory tradeSpecialization;

    @NotBlank(message = "Contact number is required")
    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Email(message = "Invalid email format")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
