package com.prgrms.nabmart.domain.order.service.response;

import com.prgrms.nabmart.domain.order.OrderItem;

public record FindOrderDetailItemResponse(
    Long itemId,
    String name,
    Integer quantity,
    Integer price
) {

    public static FindOrderDetailItemResponse from(final OrderItem orderItem) {
        return new FindOrderDetailItemResponse(
            orderItem.getItemId(),
            orderItem.getItemName(),
            orderItem.getQuantity(),
            orderItem.getItemPrice()
        );
    }
}
