package com.example.BuildTwin._0.domain.materials.service;

import com.example.BuildTwin._0.domain.materials.dto.SupplierCreateDto;
import com.example.BuildTwin._0.domain.materials.model.Supplier;

import java.util.List;

public interface SupplierService {

    Supplier createSupplier(SupplierCreateDto dto);

    List<Supplier> getAllSuppliers();

    Supplier getSupplierById(Long id);
}
