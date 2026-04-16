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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@UnitTest
class SeasonalProductHandlerTests {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private SeasonalProductHandler handler;

    @Test
    void decrements_when_in_season_and_in_stock() {
        LocalDate today = LocalDate.now();
        Product product = new Product(1L, 15, 10, ProductType.SEASONAL, "Watermelon",
                null, today.minusDays(2), today.plusDays(60));

        handler.handle(product);

        assertThat(product.getAvailable()).isEqualTo(9);
        verify(productRepository).save(product);
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
    }

    @Test
    void marks_unavailable_when_restock_would_miss_season_end() {
        LocalDate today = LocalDate.now();
        Product product = new Product(1L, 30, 0, ProductType.SEASONAL, "Watermelon",
                null, today.minusDays(10), today.plusDays(5));

        handler.handle(product);

        assertThat(product.getAvailable()).isZero();
        verify(notificationService).sendOutOfStockNotification("Watermelon");
        verify(productRepository).save(product);
    }

    @Test
    void notifies_out_of_stock_when_season_not_started_yet() {
        LocalDate today = LocalDate.now();
        Product product = new Product(1L, 15, 5, ProductType.SEASONAL, "Grapes",
                null, today.plusDays(180), today.plusDays(240));

        handler.handle(product);

        verify(notificationService).sendOutOfStockNotification("Grapes");
        verify(productRepository, never()).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void notifies_delay_when_in_season_but_out_of_stock_and_restock_fits() {
        LocalDate today = LocalDate.now();
        Product product = new Product(1L, 5, 0, ProductType.SEASONAL, "Watermelon",
                null, today.minusDays(10), today.plusDays(60));

        handler.handle(product);

        verify(notificationService).sendDelayNotification(5, "Watermelon");
        verify(productRepository, never()).save(product);
    }
}
