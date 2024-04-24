package com.prgrms.nabmart.domain.item.repository;

import static com.prgrms.nabmart.domain.item.QItem.item;
import static com.prgrms.nabmart.domain.statistics.QStatistics.statistics;

import com.prgrms.nabmart.domain.category.MainCategory;
import com.prgrms.nabmart.domain.category.SubCategory;
import com.prgrms.nabmart.domain.item.Item;
import com.prgrms.nabmart.domain.item.ItemSortType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final int NEW_PRODUCT_REFERENCE_TIME = 2;
    private static final double HOT_PRODUCT_REFERENCE_RATE = 3.7;
    private static final int HOT_PRODUCT_REFERENCE_ORDERS = 10;

    @Override
    public List<Item> findNewItemsOrderBy(Pageable pageable) {
        Predicate predicate = item.createdAt.after(
            LocalDateTime.now().minusWeeks(NEW_PRODUCT_REFERENCE_TIME));

        return queryFactory
            .selectFrom(item)
            .where(predicate)
            .orderBy(item.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findHotItemsOrderBy(Pageable pageable) {

        return queryFactory
            .selectFrom(item)
            .join(item.statistics, statistics)
            .where(item.rate.gt(HOT_PRODUCT_REFERENCE_RATE)
                .and(item.statistics.orders.gt(HOT_PRODUCT_REFERENCE_ORDERS)))
            .orderBy(item.statistics.orders.desc(), item.itemId.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findByMainCategoryOrderBy(MainCategory mainCategory, ItemSortType sortType,
        Pageable pageable) {

        return queryFactory.selectFrom(item)
            .where(item.mainCategory.eq(mainCategory))
            .orderBy(createOrderSpecifier(sortType), item.itemId.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findBySubCategoryOrderBy(MainCategory mainCategory, SubCategory subCategory,
        ItemSortType sortType, Pageable pageable) {

        return queryFactory.selectFrom(item)
            .where(item.mainCategory.eq(mainCategory), item.subCategory.eq(subCategory))
            .orderBy(createOrderSpecifier(sortType), item.itemId.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private OrderSpecifier createOrderSpecifier(ItemSortType sortType) {

        return switch (sortType) {
            case NEW -> new OrderSpecifier<>(Order.DESC, item.createdAt);
            case HIGHEST_AMOUNT -> new OrderSpecifier<>(Order.DESC, item.price);
            case LOWEST_AMOUNT -> new OrderSpecifier<>(Order.ASC, item.price);
            case DISCOUNT -> new OrderSpecifier<>(Order.DESC, item.discount);
            default -> new OrderSpecifier<>(Order.DESC, item.statistics.orders);
        };
    }
}
