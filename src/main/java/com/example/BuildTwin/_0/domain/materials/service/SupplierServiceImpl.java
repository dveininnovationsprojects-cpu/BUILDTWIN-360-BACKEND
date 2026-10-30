package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.repository.SupplierRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public Supplier createSupplier(SupplierCreateDto dto) {
        if (supplierRepository.existsBySupplierCode(dto.getSupplierCode())) {
            throw new DuplicateResourceException("Supplier with code " + dto.getSupplierCode() + " already exists");
        }
        Supplier supplier = Supplier.builder()
                .supplierCode(dto.getSupplierCode())
                .name(dto.getName())
                .contactPerson(dto.getContactPerson())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .gstin(dto.getGstin())
                .panNumber(dto.getPanNumber())
                .bankAccountNumber(dto.getBankAccountNumber())
                .bankName(dto.getBankName())
                .bankIfscCode(dto.getBankIfscCode())
                .bankBranch(dto.getBankBranch())
                .categorySpecialization(dto.getCategorySpecialization())
                .address(dto.getAddress())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();
        Supplier saved = supplierRepository.save(supplier);
        log.info("Registered new supplier profile - Code: {}, Name: '{}', Categories: '{}', Status: {}",
                saved.getSupplierCode(), saved.getName(), saved.getCategorySpecialization(), saved.getStatus());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getSupplierById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Supplier getSupplierByCode(String code) {
        return supplierRepository.findBySupplierCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with code: " + code));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Supplier> getSuppliersByStatus(String status) {
        return supplierRepository.findByStatus(status);
    }

    @Override
    public Supplier updateSupplier(Long id, SupplierCreateDto dto) {
        Supplier supplier = getSupplierById(id);
        if (dto.getName() != null) supplier.setName(dto.getName());
        if (dto.getContactPerson() != null) supplier.setContactPerson(dto.getContactPerson());
        if (dto.getPhone() != null) supplier.setPhone(dto.getPhone());
        if (dto.getEmail() != null) supplier.setEmail(dto.getEmail());
        if (dto.getGstin() != null) supplier.setGstin(dto.getGstin());
        if (dto.getPanNumber() != null) supplier.setPanNumber(dto.getPanNumber());
        if (dto.getBankAccountNumber() != null) supplier.setBankAccountNumber(dto.getBankAccountNumber());
        if (dto.getBankName() != null) supplier.setBankName(dto.getBankName());
        if (dto.getBankIfscCode() != null) supplier.setBankIfscCode(dto.getBankIfscCode());
        if (dto.getBankBranch() != null) supplier.setBankBranch(dto.getBankBranch());
        if (dto.getCategorySpecialization() != null) supplier.setCategorySpecialization(dto.getCategorySpecialization());
        if (dto.getAddress() != null) supplier.setAddress(dto.getAddress());
        if (dto.getStatus() != null) supplier.setStatus(dto.getStatus());
        Supplier updated = supplierRepository.save(supplier);
        log.info("Updated supplier profile ID: {} - Code: {}, Name: '{}', Status: {}",
                id, updated.getSupplierCode(), updated.getName(), updated.getStatus());
        return updated;
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplier.setStatus("BLACKLISTED");
        supplierRepository.save(supplier);
        log.warn("Deactivated and blacklisted vendor profile ID: {} - Code: {}, Name: '{}'",
                id, supplier.getSupplierCode(), supplier.getName());
    }
}
