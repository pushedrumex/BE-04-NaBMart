package com.prgrms.nabmart.domain.item.service.request;

import org.springframework.data.domain.PageRequest;

public record FindHotItemsCommand(PageRequest pageRequest) {

    public static FindHotItemsCommand of(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return new FindHotItemsCommand(pageRequest);
    }
}
