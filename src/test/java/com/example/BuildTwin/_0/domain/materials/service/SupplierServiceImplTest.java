package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.repository.SupplierRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier sampleSupplier;
    private SupplierCreateDto sampleDto;

    @BeforeEach
    void setUp() {
        sampleSupplier = Supplier.builder()
                .id(1L)
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .contactPerson("Ravi Kumar")
                .phone("9876543210")
                .email("sales@ultratech.com")
                .gstin("33AAAAA0000A1Z5")
                .panNumber("ABCDE1234F")
                .bankAccountNumber("50200012345678")
                .bankName("HDFC Bank")
                .bankIfscCode("HDFC0001234")
                .bankBranch("Sholinganallur, Chennai")
                .categorySpecialization("CEMENT, RMC")
                .address("OMR Road, Chennai")
                .status("ACTIVE")
                .build();

        sampleDto = SupplierCreateDto.builder()
                .supplierCode("SUP-101")
                .name("UltraTech Cements")
                .contactPerson("Ravi Kumar")
                .phone("9876543210")
                .email("sales@ultratech.com")
                .gstin("33AAAAA0000A1Z5")
                .panNumber("ABCDE1234F")
                .bankAccountNumber("50200012345678")
                .bankName("HDFC Bank")
                .bankIfscCode("HDFC0001234")
                .bankBranch("Sholinganallur, Chennai")
                .categorySpecialization("CEMENT, RMC")
                .address("OMR Road, Chennai")
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Should create vendor profile with bank details and category specialization successfully")
    void testCreateSupplier_Success() {
        when(supplierRepository.existsBySupplierCode("SUP-101")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        Supplier created = supplierService.createSupplier(sampleDto);

        assertNotNull(created);
        assertEquals("SUP-101", created.getSupplierCode());
        assertEquals("UltraTech Cements", created.getName());
        assertEquals("ABCDE1234F", created.getPanNumber());
        assertEquals("50200012345678", created.getBankAccountNumber());
        assertEquals("HDFC Bank", created.getBankName());
        assertEquals("CEMENT, RMC", created.getCategorySpecialization());
        assertEquals("ACTIVE", created.getStatus());
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when supplier code already exists")
    void testCreateSupplier_DuplicateCodeThrows() {
        when(supplierRepository.existsBySupplierCode("SUP-101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> supplierService.createSupplier(sampleDto));
        verify(supplierRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve supplier details by ID")
    void testGetSupplierById_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));

        Supplier found = supplierService.getSupplierById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
        assertEquals("SUP-101", found.getSupplierCode());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when supplier ID does not exist")
    void testGetSupplierById_NotFoundThrows() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> supplierService.getSupplierById(99L));
    }

    @Test
    @DisplayName("Should update supplier profile details successfully")
    void testUpdateSupplier_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        SupplierCreateDto updateDto = SupplierCreateDto.builder()
                .name("UltraTech Cement Ltd")
                .categorySpecialization("CEMENT, RMC, AGGREGATES")
                .bankBranch("Adyar, Chennai")
                .build();

        Supplier updated = supplierService.updateSupplier(1L, updateDto);

        assertNotNull(updated);
        assertEquals("UltraTech Cement Ltd", updated.getName());
        assertEquals("CEMENT, RMC, AGGREGATES", updated.getCategorySpecialization());
        assertEquals("Adyar, Chennai", updated.getBankBranch());
        verify(supplierRepository).save(sampleSupplier);
    }

    @Test
    @DisplayName("Should deactivate and blacklist vendor upon delete")
    void testDeleteSupplier_BlacklistsVendor() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(sampleSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> inv.getArgument(0));

        supplierService.deleteSupplier(1L);

        assertEquals("BLACKLISTED", sampleSupplier.getStatus());
        verify(supplierRepository).save(sampleSupplier);
    }
}
