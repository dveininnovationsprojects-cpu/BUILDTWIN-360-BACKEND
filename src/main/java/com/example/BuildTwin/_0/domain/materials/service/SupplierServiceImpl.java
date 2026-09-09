package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;
import com.example.BuildTwin._0.domain.materials.repository.SupplierRepository;
import com.example.BuildTwin._0.exception.DuplicateResourceException;
import com.example.BuildTwin._0.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .address(dto.getAddress())
                .status(dto.getStatus() != null ? dto.getStatus() : "ACTIVE")
                .build();
        return supplierRepository.save(supplier);
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
    public Supplier updateSupplier(Long id, SupplierCreateDto dto) {
        Supplier supplier = getSupplierById(id);
        if (dto.getName() != null) supplier.setName(dto.getName());
        if (dto.getContactPerson() != null) supplier.setContactPerson(dto.getContactPerson());
        if (dto.getPhone() != null) supplier.setPhone(dto.getPhone());
        if (dto.getEmail() != null) supplier.setEmail(dto.getEmail());
        if (dto.getGstin() != null) supplier.setGstin(dto.getGstin());
        if (dto.getAddress() != null) supplier.setAddress(dto.getAddress());
        if (dto.getStatus() != null) supplier.setStatus(dto.getStatus());
        return supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = getSupplierById(id);
        supplierRepository.delete(supplier);
    }
}
