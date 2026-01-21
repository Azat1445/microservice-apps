package org.example.inventoryservice.exception;

public class ProductsNotFoundException extends RuntimeException {
    public ProductsNotFoundException(String msg) {
        super(msg);
    }
}
