package org.example.inventoryservice.controller;

import jakarta.validation.Valid;
import org.example.inventoryservice.dto.ProductsCreateDto;
import org.example.inventoryservice.dto.ProductsUpdateDto;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.dto.ProductsResponseDto;
import org.example.inventoryservice.service.ProductsService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    /**
     * GET /api/products?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ProductsResponseDto>> findAllProducts(@PageableDefault(size = 20) Pageable pageable) {
        log.info("GET /api/products - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<ProductsResponseDto> products = productsService.findAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductsResponseDto> findProductsById(@PathVariable Long id) {
        log.info("GET /api/products/{}", id);

        ProductsResponseDto products = productsService.findProductsById(id);
        return ResponseEntity.ok(products);
    }

    /**
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductsResponseDto> createProducts(@Valid @RequestBody ProductsCreateDto createDto) {
        log.info("POST /api/products - name: {}", createDto.getName());

        ProductsResponseDto cretedProduct = productsService.createProducts(createDto);
        return ResponseEntity.ok(cretedProduct);
    }

    /**
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductsResponseDto> updateProducts(@PathVariable Long id,
                                                              @Valid @RequestBody ProductsUpdateDto updateDto) {
        log.info("PUT /api/products/{}", id);

        ProductsResponseDto updatedProduct = productsService.updateProducts(id, updateDto);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductsResponseDto> deleteProducts(@PathVariable Long id) {
        log.info("DELETE /api/products/{}", id);

        productsService.deleteProducts(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/products/{id}/reserve?quantity=5
     * Зарезервировать товар (уменьшить quantity)
     */
    @Profile("dev")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<ProductsResponseDto> reserveProducts(@PathVariable Long id,
                                                               @RequestParam Long quantity) {
        log.info("POST /api/products/{}/reserve - quantity: {}", id, quantity);

        ProductsResponseDto products = productsService.reserveProduct(id, quantity);
        return ResponseEntity.ok(products);
    }

    /**
     * POST /api/products/{id}/restore?quantity=5
     * Вернуть товар на склад (увеличить quantity)
     */

    @PostMapping("/{id}/restore")
    public ResponseEntity<ProductsResponseDto> restoreProducts(@PathVariable Long id,
                                                               @RequestParam Long quantity) {
        log.info("POST /api/products/{}/restore - quantity: {}", id, quantity);

        ProductsResponseDto products = productsService.restoreProduct(id, quantity);
        return ResponseEntity.ok(products);
    }
}

