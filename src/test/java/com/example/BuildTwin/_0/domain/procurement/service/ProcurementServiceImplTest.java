package com.example.BuildTwin._0.domain.procurement.service;

import com.example.BuildTwin._0.domain.materials.dto.ProjectedShortageDto;
import com.example.BuildTwin._0.domain.materials.enums.MaterialUnit;
import com.example.BuildTwin._0.domain.materials.model.Material;
import com.example.BuildTwin._0.domain.materials.repository.MaterialRepository;
import com.example.BuildTwin._0.domain.materials.service.StockLedgerService;
import com.example.BuildTwin._0.domain.procurement.dto.MaterialRequestApprovalDto;
import com.example.BuildTwin._0.domain.procurement.model.Grn;
import com.example.BuildTwin._0.domain.procurement.model.MaterialRequest;
import com.example.BuildTwin._0.domain.procurement.repository.GrnRepository;
import com.example.BuildTwin._0.domain.procurement.repository.MaterialRequestRepository;
import com.example.BuildTwin._0.exception.BadRequestException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcurementServiceImplTest {

    @Mock
    private MaterialRequestRepository materialRequestRepository;

    @Mock
    private GrnRepository grnRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private StockLedgerService stockLedgerService;

    @InjectMocks
    private ProcurementServiceImpl procurementService;

    private Material sampleMaterial;
    private MaterialRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleMaterial = Material.builder()
                .id(1L)
                .materialCode("MAT-CEM-001")
                .name("Portland Cement 50kg")
                .category("CEMENT")
                .unit(MaterialUnit.BAGS)
                .standardRate(new BigDecimal("380.00"))
                .reorderLevel(new BigDecimal("50.00"))
                .currentStock(new BigDecimal("100.00"))
                .build();

        sampleRequest = MaterialRequest.builder()
                .id(10L)
                .projectId(100L)
                .materialId(1L)
                .requiredQty(new BigDecimal("300.00"))
                .status("PENDING")
                .build();
    }

    @Test
    @DisplayName("Should create material request with PENDING status by default")
    void testCreateMaterialRequest() {
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequest result = procurementService.createMaterialRequest(sampleRequest);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        verify(materialRequestRepository).save(sampleRequest);
    }

    @Test
    @DisplayName("Should approve material request successfully")
    void testApproveMaterialRequest() {
        MaterialRequestApprovalDto approvalDto = MaterialRequestApprovalDto.builder()
                .status("APPROVED")
                .approvedBy("PM_Selvam")
                .remarks("Approved for foundation pouring")
                .build();

        when(materialRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequest result = procurementService.updateMaterialRequestStatus(10L, approvalDto);

        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        assertEquals("PM_Selvam", result.getApprovedBy());
        verify(materialRequestRepository).save(sampleRequest);
    }

    @Test
    @DisplayName("Should reject material request with rejection reason")
    void testRejectMaterialRequest() {
        MaterialRequestApprovalDto approvalDto = MaterialRequestApprovalDto.builder()
                .status("REJECTED")
                .approvedBy("PM_Selvam")
                .rejectionReason("Exceeds allocated budget")
                .build();

        when(materialRequestRepository.findById(10L)).thenReturn(Optional.of(sampleRequest));
        when(materialRequestRepository.save(any(MaterialRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        MaterialRequest result = procurementService.updateMaterialRequestStatus(10L, approvalDto);

        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        assertEquals("Exceeds allocated budget", result.getRejectionReason());
    }

    @Test
    @DisplayName("Should process GRN with accepted/rejected quantity breakdown and update stock")
    void testCreateGrnSuccess() {
        Grn grn = Grn.builder()
                .poId(50L)
                .projectId(100L)
                .materialId(1L)
                .receivedQty(new BigDecimal("500.00"))
                .acceptedQty(new BigDecimal("480.00"))
                .rejectedQty(new BigDecimal("20.00"))
                .rejectionReason("20 bags torn in transit")
                .build();

        when(grnRepository.save(any(Grn.class))).thenAnswer(inv -> {
            Grn saved = inv.getArgument(0);
            saved.setId(101L);
            return saved;
        });

        Grn result = procurementService.createGrn(grn);

        assertNotNull(result);
        assertEquals(new BigDecimal("480.00"), result.getAcceptedQty());
        assertEquals(new BigDecimal("20.00"), result.getRejectedQty());
        verify(grnRepository).save(grn);
        verify(stockLedgerService).recordTransaction(any());
    }

    @Test
    @DisplayName("Should throw BadRequestException when accepted + rejected exceeds received quantity")
    void testCreateGrnInvalidQuantities() {
        Grn grn = Grn.builder()
                .poId(50L)
                .materialId(1L)
                .receivedQty(new BigDecimal("100.00"))
                .acceptedQty(new BigDecimal("80.00"))
                .rejectedQty(new BigDecimal("30.00")) // Sum 110 > 100
                .build();

        assertThrows(BadRequestException.class, () -> procurementService.createGrn(grn));
        verify(grnRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should detect projected shortage correctly based on pending/approved requests vs stock")
    void testDetectProjectedShortage() {
        when(materialRequestRepository.findByProjectIdAndStatusIn(eq(100L), anyList()))
                .thenReturn(List.of(sampleRequest)); // 300.00 requested
        when(materialRepository.findById(1L)).thenReturn(Optional.of(sampleMaterial)); // Stock 100.00

        List<ProjectedShortageDto> shortages = procurementService.detectProjectedShortage(100L);

        assertFalse(shortages.isEmpty());
        ProjectedShortageDto dto = shortages.get(0);
        assertEquals(1L, dto.getMaterialId());
        assertEquals(new BigDecimal("100.00"), dto.getCurrentStock());
        assertEquals(new BigDecimal("300.00"), dto.getTotalRequestedQty());
        assertEquals(new BigDecimal("200.00"), dto.getProjectedShortage());
        assertEquals("SHORTAGE", dto.getStatus());
    }
}
