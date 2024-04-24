package com.prgrms.nabmart.domain.item.service.request;

import org.springframework.data.domain.PageRequest;

public record FindNewItemsCommand(PageRequest pageRequest) {

    public static FindNewItemsCommand of(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return new FindNewItemsCommand(pageRequest);
    }
}
