package com.prgrms.nabmart.domain.order.service.response;

import com.prgrms.nabmart.domain.order.Order;
import java.util.List;
import java.util.stream.Collectors;

public record FindOrdersResponse(
    List<FindOrderResponse> orders,
    Integer totalPages
) {

    public static FindOrdersResponse of(List<Order> orders, Integer totalPages) {
        return new FindOrdersResponse(
            orders.stream()
                .map(FindOrderResponse::from)
                .toList(),
            totalPages
        );
    }
}
