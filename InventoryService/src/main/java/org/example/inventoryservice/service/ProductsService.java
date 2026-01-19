package org.example.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.dto.ProductsCreateDto;
import org.example.inventoryservice.dto.ProductsResponseDto;
import org.example.inventoryservice.dto.ProductsUpdateDto;
import org.example.inventoryservice.entity.Products;
import org.example.inventoryservice.exception.ProductsNotFoundException;
import org.example.inventoryservice.mapper.ProductsMapperDto;
import org.example.inventoryservice.repository.ProductsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final ProductsMapperDto productsMapper;

    @Transactional(readOnly = true)
    public Page<ProductsResponseDto> findAllProducts(Pageable pageable) {
        log.info("Fetching all products with pagination: {}", pageable);

        return productsRepository.findAll(pageable)
                .map(productsMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductsResponseDto findProductsById(Long id) {
        log.info("Fetching product by id: {}", id);

        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return productsMapper.toDto(product);
    }

    @Transactional
    public ProductsResponseDto createProducts(ProductsCreateDto createDto) {
        log.info("Creating new products: {}", createDto.getName());

        if (productsRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new IllegalArgumentException("Product with " + createDto.getName() + "name already exists");
        }

        Products products = productsMapper.toEntity(createDto);
        Products savedProduct = productsRepository.save(products);

        log.info("Product created successfully with id:: {}", savedProduct.getId());
        return productsMapper.toDto(savedProduct);
    }

    @Transactional
    public ProductsResponseDto updateProducts(Long id, ProductsUpdateDto updateDto){
        log.info("Updating product with id: {}", id);

        Products existingProduct = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (updateDto.getName() != null
            && !updateDto.getName().equalsIgnoreCase(existingProduct.getName())
            && productsRepository.existsByNameIgnoreCase(updateDto.getName())) {
            throw new IllegalArgumentException("Product with name " + updateDto.getName() + " already exists");
        }

        productsMapper.updateEntityFromDto(updateDto, existingProduct);
        Products updatedProduct = productsRepository.save(existingProduct);

        log.info("Product updated successfully with id:: {}", updatedProduct.getId());
        return productsMapper.toDto(updatedProduct);
    }

    @Transactional
    public void deleteProducts(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productsRepository.existsById(id)) {
            throw new ProductsNotFoundException("Product not found with id: " + id);
        }

        productsRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    /**
     * Зарезервировать товар
     * Метод для gRPC
     * */
    @Transactional
    public ProductsResponseDto reserveProduct(Long productId, Long quantity) {
        log.info("Reserving {} units of product with id: {}",quantity, productId);

        Products products = productsRepository.findById(productId)
                .orElseThrow(() -> new ProductsNotFoundException("Product not found with id: " + productId));

        products.decreaseQuantity(quantity);

        Products updatedProduct = productsRepository.save(products);
        log.info("Product reserved successfully. Remaining quantity: {}", updatedProduct.getQuantity());

        return productsMapper.toDto(updatedProduct);
    }

    /**
     * Вернуть товар на склад
     * Метод при отмене заказа
     */
    @Transactional
    public ProductsResponseDto restoreProduct(Long productId, Long quantity) {
        log.info("Restoring {} units of product with id: {}", quantity, productId);

        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new ProductsNotFoundException("Product not found with id: " + productId));

        product.increaseQuantity(quantity);

        Products updatedProduct = productsRepository.save(product);
        log.info("Product restored successfully. New quantity: {}", updatedProduct.getQuantity());

        return productsMapper.toDto(updatedProduct);
    }

    /**
     * Проверить доступность товара
     * Метод для gRPC
     */
    @Transactional(readOnly = true)
    public ProductsResponseDto checkAvailability(Long productId) {
        log.info("Checking availability for product with id: {}", productId);

        Products product = productsRepository.findAvailableById(productId)
                .orElseThrow(() -> new ProductsNotFoundException("Product not available or not found with id: " + productId));

        return productsMapper.toDto(product);
    }
}
