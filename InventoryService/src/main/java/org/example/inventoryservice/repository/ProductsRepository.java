package org.example.inventoryservice.repository;

import org.example.inventoryservice.entity.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {

    // Найти товары по имени
    List<Products> findByNameContainingIgnoreCase(String name);

    // Найти товары в наличии
    @Query("SELECT p FROM Products p WHERE p.quantity > 0")
    List<Products> findAvailableProducts();

    // Найти товары с ценой в диапазоне
    @Query("SELECT p FROM Products p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<Products> findByPriceRange(@Param("minPrice") Double minPrice,
                                    @Param("maxPrice") Double maxPrice);

    // Проверить существование по имени
    boolean existsByNameIgnoreCase(String name);

    // Найти товар по ID с проверкой наличия
    @Query("SELECT p FROM Products p WHERE p.id = :id AND p.quantity > 0")
    Optional<Products> findAvailableById(@Param("id") Long id);
}

