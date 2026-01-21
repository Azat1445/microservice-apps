package org.example.inventoryservice.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.example.inventoryservice.dto.ProductsResponseDto;
import org.example.inventoryservice.exception.ProductsNotFoundException;
import org.example.inventoryservice.service.ProductsService;

// Импорты сгенерированных классов
import org.example.inventoryservice.grpc.generated.InventoryServiceGrpc;
import org.example.inventoryservice.grpc.generated.ProductRequest;
import org.example.inventoryservice.grpc.generated.ProductResponse;
import org.example.inventoryservice.grpc.generated.ReserveRequest;
import org.example.inventoryservice.grpc.generated.ReserveResponse;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class InventoryGrpcService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductsService productsService;

    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        log.info("gRPC checkAvailability called for productId: {}", request.getProductId());

        try {
            ProductsResponseDto product = productsService.checkAvailability(request.getProductId());

            ProductResponse response = ProductResponse.newBuilder()
                    .setId(product.getId())
                    .setName(product.getName())
                    .setQuantity(product.getQuantity())
                    .setPrice(product.getPrice())
                    .setSale(product.getSale())
                    .setFinalPrice(product.getFinalPrice())
                    .setAvailable(product.getAvailable())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (ProductsNotFoundException e) {
            log.error("Product not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());

        } catch (Exception e) {
            log.error("Error in checkAvailability: ", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void reserveProduct(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        log.info("gRPC reserveProduct called for productId: {}, quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            ProductsResponseDto product = productsService.reserveProduct(
                    request.getProductId(),
                    request.getQuantity()
            );

            ReserveResponse response = ReserveResponse.newBuilder()
                    .setProductId(product.getId())
                    .setRemainingQuantity(product.getQuantity())
                    .setSuccess(true)
                    .setMessage("Product reserved successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (ProductsNotFoundException e) {
            log.error("Product not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());

        } catch (IllegalStateException e) {
            log.error("Insufficient stock: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());

        } catch (Exception e) {
            log.error("Error in reserveProduct: ", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }

    @Override
    public void restoreProduct(ReserveRequest request, StreamObserver<ReserveResponse> responseObserver) {
        log.info("gRPC restoreProduct called for productId: {}, quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            ProductsResponseDto product = productsService.restoreProduct(
                    request.getProductId(),
                    request.getQuantity()
            );

            ReserveResponse response = ReserveResponse.newBuilder()
                    .setProductId(product.getId())
                    .setRemainingQuantity(product.getQuantity())
                    .setSuccess(true)
                    .setMessage("Product restored successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (ProductsNotFoundException e) {
            log.error("Product not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());

        } catch (Exception e) {
            log.error("Error in restoreProduct: ", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .asRuntimeException());
        }
    }
}
