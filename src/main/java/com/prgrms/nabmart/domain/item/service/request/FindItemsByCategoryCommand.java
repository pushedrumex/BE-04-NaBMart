package com.prgrms.nabmart.domain.item.service.request;

import com.prgrms.nabmart.domain.category.exception.NotFoundCategoryException;
import com.prgrms.nabmart.domain.item.ItemSortType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;

@Slf4j
public record FindItemsByCategoryCommand(
    String mainCategoryName,
    String subCategoryName,
    PageRequest pageRequest,
    ItemSortType itemSortType) {

    public static FindItemsByCategoryCommand of(int page, int size, String mainCategoryName,
        String subCategoryName, String sortType
    ) {

        validateMainCategoryName(mainCategoryName);
        ItemSortType itemSortType = ItemSortType.from(sortType);
        PageRequest pageRequest = PageRequest.of(page, size);
        return new FindItemsByCategoryCommand(mainCategoryName, subCategoryName, pageRequest,
            itemSortType);
    }

    private static void validateMainCategoryName(String mainCategoryName) {
        if (mainCategoryName == null || mainCategoryName.isBlank()) {
            throw new NotFoundCategoryException("카테고리명은 필수 항목입니다.");
        }
    }
}
