package com.ga.capstone.repositories;


import com.ga.capstone.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Category entity CRUD and custom queries.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Check if a category with the given name already exists.
     *
     * @param name the category name to check
     * @return true if exists
     */
    boolean existsByName(String name);

    /**
     * Find a category by its name.
     *
     * @param name the category name
     * @return optional Category
     */
    Optional<Category> findByName(String name);
}
