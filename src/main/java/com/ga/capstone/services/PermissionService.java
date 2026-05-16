package com.ga.capstone.services;

import com.ga.capstone.exceptions.ResourceAlreadyExistsException;
import com.ga.capstone.exceptions.ResourceNotFoundException;
import com.ga.capstone.models.Permission;
import com.ga.capstone.repositories.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final PermissionRepository permissionRepository;

    /**
     *
     * @param permission
     * @return
     */
    @Transactional
    public Permission createPermission(Permission permission) {
        if (permission.getAction() != null && permissionRepository.existsByAction(permission.getAction()))
            throw new ResourceAlreadyExistsException("Permission already exists");
        return permissionRepository.save(permission);
    }


    /**
     *
     * @return
     */
    public List<Permission> findAllPermissions() {
        return permissionRepository.findAll();
    }

    /**
     *
     * @param permissionId
     * @return
     */
    public Permission findPermissionById(Long permissionId) {
        return permissionRepository.findById(permissionId).orElseThrow(
                () -> new ResourceNotFoundException("Permission not found")
        );
    }

    /**
     *
     * @param id
     * @param updates
     * @return
     */
    @Transactional
    public Permission updatePermission(Long id, Permission updates) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found"));

        if (updates.getAction() != null && !updates.getAction().equals(existing.getAction())) {
            existing.setAction(updates.getAction());
        }

        try {
            return permissionRepository.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new ResourceAlreadyExistsException("Permission action must be unique");
        }
    }

    /**
     *
     * @param permissionId
     */
    public void deletePermissionById(Long permissionId) {
        if (!permissionRepository.existsById(permissionId)) {
            throw new ResourceNotFoundException("Permission not found");
        }
        permissionRepository.deleteById(permissionId);
    }


}
