package com.prgrms.nabmart.domain.order.service;

import static com.prgrms.nabmart.domain.order.support.OrderFixture.pendingOrder;
import static com.prgrms.nabmart.domain.user.support.UserFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.prgrms.nabmart.domain.item.repository.ItemRepository;
import com.prgrms.nabmart.domain.order.Order;
import com.prgrms.nabmart.domain.order.OrderItem;
import com.prgrms.nabmart.domain.order.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderCancelServiceTest {

    @InjectMocks
    OrderCancelService orderCancelService;

    @Mock
    ItemRepository itemRepository;

    @Nested
    @DisplayName("cancelOrder 메서드 실행 시")
    class CancelOrderTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            Order order = pendingOrder(1L, user());
            OrderItem orderItem = order.getOrderItems().get(0);

            // when
            orderCancelService.cancelOrder(order);

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
            verify(itemRepository, times(1)).increaseQuantity(orderItem.getItem().getItemId(),
                orderItem.getQuantity());
        }
    }

}
