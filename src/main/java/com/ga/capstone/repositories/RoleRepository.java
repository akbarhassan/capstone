package com.ga.capstone.repositories;


import com.ga.capstone.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    // unique role names
    boolean existsByName(String name);
}
