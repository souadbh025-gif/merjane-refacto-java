package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.ProductType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductHandlerRegistry {

    private final Map<ProductType, ProductHandler> handlersByType;

    public ProductHandlerRegistry(List<ProductHandler> handlers) {
        this.handlersByType = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(ProductHandler::getType, Function.identity()));
    }

    public ProductHandler forType(ProductType type) {
        ProductHandler handler = handlersByType.get(type);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for product type: " + type);
        }
        return handler;
    }
}
