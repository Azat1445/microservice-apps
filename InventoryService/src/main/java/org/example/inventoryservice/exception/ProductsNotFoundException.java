package org.example.inventoryservice.exception;

public class ProductsNotFoundException extends Exception {
    public ProductsNotFoundException(String msg) {
        super(msg);
    }
}
