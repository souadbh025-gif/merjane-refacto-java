package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ExpirableProductHandler implements ProductHandler {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;

    @Override
    public ProductType getType() {
        return ProductType.EXPIRABLE;
    }

    @Override
    public void handle(Product product) {
        LocalDate today = LocalDate.now();
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(today)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }
        notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
        product.setAvailable(0);
        productRepository.save(product);
    }
}
