package com.nimbleways.springboilerplate.services.product;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ExpirableProductHandlerTests {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private ExpirableProductHandler handler;

    @Test
    void decrements_when_in_stock_and_not_expired() {
        Product product = new Product(1L, 15, 10, ProductType.EXPIRABLE, "Butter",
                LocalDate.now().plusDays(10), null, null);

        handler.handle(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository).save(product);
        verify(notificationService, never()).sendExpirationNotification(anyString(), any());
    }

    @Test
    void notifies_expiration_when_past_expiry_date() {
        LocalDate expired = LocalDate.now().minusDays(2);
        Product product = new Product(1L, 90, 6, ProductType.EXPIRABLE, "Milk", expired, null, null);

        handler.handle(product);

        assertThat(product.getAvailable()).isZero();
        verify(notificationService).sendExpirationNotification("Milk", expired);
        verify(productRepository).save(product);
    }

    @Test
    void notifies_expiration_when_out_of_stock_even_if_not_expired() {
        LocalDate future = LocalDate.now().plusDays(10);
        Product product = new Product(1L, 15, 0, ProductType.EXPIRABLE, "Butter", future, null, null);

        handler.handle(product);

        assertThat(product.getAvailable()).isZero();
        verify(notificationService).sendExpirationNotification("Butter", future);
        verify(productRepository).save(product);
    }
}
