package org.example.orderservice.grpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.example.orderservice.exception.GrpcServiceUnavailableException;
import org.example.inventoryservice.grpc.generated.InventoryServiceGrpc;
import org.example.inventoryservice.grpc.generated.ProductRequest;
import org.example.inventoryservice.grpc.generated.ProductResponse;
import org.example.inventoryservice.grpc.generated.ReserveRequest;
import org.example.inventoryservice.grpc.generated.ReserveResponse;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryGrpcClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryServiceStub;

    /**
     * Проверка доступности товара
     */
    public ProductResponse checkProductAvailability(Long productId) {
        log.info("Checking availability for product {} via grpc", productId);

        try {
            ProductRequest request = ProductRequest.newBuilder()
                    .setProductId(productId)
                    .build();

            ProductResponse response = inventoryServiceStub.checkAvailability(request);

            log.debug("Product {} availability: {}", productId, response.getAvailable());

            return response;
        } catch (StatusRuntimeException e) {
            log.error("GRPC error checking product {}: {}", productId, e.getStatus());
            throw new RuntimeException(" Failed to check product availability: " + e.getMessage());
        }
    }

    /**
     * Резервация товара
     */
    public ReserveResponse reserveProduct(Long productId, Integer quantity) {
        log.info("Reserving product {} quantity {} via grpc", productId, quantity);

        try {
            ReserveRequest request = ReserveRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity.longValue())
                    .build();

            ReserveResponse response = inventoryServiceStub.reserveProduct(request);

            log.debug("Reserve result for product {}: success={}, message={}", productId, response.getSuccess(), response.getMessage());

            return response;
        } catch (StatusRuntimeException e) {
            log.error("gRPC error reserving product {}: {}", productId, e.getStatus());
            throw new GrpcServiceUnavailableException("Failed to reserve product: " + e.getMessage(), e);
        }
    }

    /**
     * Возврат товара на склад
     */
    public ReserveResponse restoreProduct(Long productId, Integer quantity) {
        log.info("Restoring product {} quantity {} via grpc", productId, quantity);

        try {
            ReserveRequest request = ReserveRequest.newBuilder()
                    .setProductId(productId)
                    .setQuantity(quantity.longValue())
                    .build();

            ReserveResponse response = inventoryServiceStub.restoreProduct(request);
            log.debug("Reserve result for product {}: success={}, message={}", productId, response.getSuccess(), response.getMessage());

            return response;
        } catch (StatusRuntimeException e) {
            log.error("GRPC error reserving product {}: {}", productId, e.getStatus());
            throw new RuntimeException(" Failed to reserve product: " + e.getMessage());
        }
    }
}
