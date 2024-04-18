package com.prgrms.nabmart.domain.item.service.response;

public record FindItemDetailResponse(Long itemId, String name, int price, String description,
                                     int quantity, double rate, long reviewCount, int discount,
                                     long like, int maxBuyQuantity) {

    public static FindItemDetailResponse of(final Long itemId, final String name, final int price,
        final String description,
        final int quantity,
        final double rate, final long reviewCount, final int discount, final long like,
        final int maxBuyQuantity) {
        return new FindItemDetailResponse(itemId, name, price, description, quantity, rate,
            reviewCount, discount, like, maxBuyQuantity);
    }
}
