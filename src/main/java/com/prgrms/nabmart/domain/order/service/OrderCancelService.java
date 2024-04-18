package com.prgrms.nabmart.domain.order.service;

import com.prgrms.nabmart.domain.item.repository.ItemRepository;
import com.prgrms.nabmart.domain.order.Order;
import com.prgrms.nabmart.domain.order.OrderItem;
import com.prgrms.nabmart.domain.order.OrderStatus;
import com.prgrms.nabmart.domain.statistics.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final ItemRepository itemRepository;
    private final StatisticsRepository statisticsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelOrder(Order order) {
        order.updateOrderStatus(OrderStatus.CANCELED);
        order.unUseCoupon();
        for (OrderItem orderItem : order.getOrderItems()) {
            itemRepository.increaseQuantity(orderItem.getItemId(), orderItem.getQuantity());
            statisticsRepository.decreaseOrders(orderItem.getItemId(), orderItem.getQuantity());
        }
    }

}
