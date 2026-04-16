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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@UnitTest
class NormalProductHandlerTests {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NormalProductHandler handler;

    @Test
    void decrements_available_when_in_stock() {
        Product product = new Product(1L, 15, 10, ProductType.NORMAL, "USB Cable", null, null, null);

        handler.handle(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void notifies_delay_when_out_of_stock_with_lead_time() {
        Product product = new Product(1L, 15, 0, ProductType.NORMAL, "USB Cable", null, null, null);

        handler.handle(product);

        assertThat(product.getAvailable()).isZero();
        verify(notificationService).sendDelayNotification(15, "USB Cable");
        verify(productRepository).save(product);
    }

    @Test
    void no_notification_when_out_of_stock_and_no_lead_time() {
        Product product = new Product(1L, 0, 0, ProductType.NORMAL, "USB Cable", null, null, null);

        handler.handle(product);

        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
        verify(productRepository, never()).save(product);
    }
}
