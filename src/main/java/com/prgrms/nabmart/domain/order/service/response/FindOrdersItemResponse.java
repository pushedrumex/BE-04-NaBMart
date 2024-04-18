package com.prgrms.nabmart.domain.order.service.response;

import com.prgrms.nabmart.domain.order.OrderItem;

public record FindOrdersItemResponse(
    Long itemId,
    String name,
    Integer quantity,
    Integer price
) {

    public static FindOrdersItemResponse from(OrderItem orderItem) {
        return new FindOrdersItemResponse(
            orderItem.getItemId(),
            orderItem.getItemName(),
            orderItem.getQuantity(),
            orderItem.getItemPrice()
        );
    }
}
