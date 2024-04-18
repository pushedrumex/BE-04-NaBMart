package com.prgrms.nabmart.domain.order.repository;

import com.prgrms.nabmart.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
