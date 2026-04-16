package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.services.product.ProductHandler;
import com.nimbleways.springboilerplate.services.product.ProductHandlerRegistry;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
class OrderProcessingServiceTests {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductHandler normalHandler;
    @Mock
    private ProductHandler seasonalHandler;

    private OrderProcessingService service;

    @BeforeEach
    void setUp() {
        when(normalHandler.getType()).thenReturn(ProductType.NORMAL);
        when(seasonalHandler.getType()).thenReturn(ProductType.SEASONAL);
        ProductHandlerRegistry registry = new ProductHandlerRegistry(List.of(normalHandler, seasonalHandler));
        service = new OrderProcessingService(orderRepository, registry);
    }

    @Test
    void dispatches_each_product_to_its_handler() {
        Product normal = new Product(1L, 5, 10, ProductType.NORMAL, "A", null, null, null);
        Product seasonal = new Product(2L, 5, 10, ProductType.SEASONAL, "B", null, null, null);
        Order order = new Order(42L, Set.of(normal, seasonal));
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

        Long result = service.processOrder(42L);

        assertThat(result).isEqualTo(42L);
        verify(normalHandler).handle(normal);
        verify(seasonalHandler).handle(seasonal);
    }

    @Test
    void throws_when_order_not_found() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processOrder(99L))
                .isInstanceOf(NoSuchElementException.class);
    }
}
