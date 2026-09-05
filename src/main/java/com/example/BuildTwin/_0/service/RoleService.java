package com.example.BuildTwin._0.service;

import com.example.BuildTwin._0.domain.identity.model.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role getRoleByName(String name);
}
