package com.prgrms.nabmart.domain.order.service;

import com.prgrms.nabmart.domain.item.repository.ItemRepository;
import com.prgrms.nabmart.domain.order.Order;
import com.prgrms.nabmart.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final ItemRepository itemRepository;

    @Transactional
    public void cancelOrder(Order order) {
        order.updateOrderStatus(OrderStatus.CANCELED);
        order.unUseCoupon();
        order.getOrderItems().forEach(
            orderItem -> itemRepository.increaseQuantity(orderItem.getItem().getItemId(),
                orderItem.getQuantity())
        );
    }

}
