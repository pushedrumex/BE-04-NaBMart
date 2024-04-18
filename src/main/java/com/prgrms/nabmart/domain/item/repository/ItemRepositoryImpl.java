package com.prgrms.nabmart.domain.item.repository;

import static com.prgrms.nabmart.domain.item.QItem.item;
import static com.prgrms.nabmart.domain.order.QOrderItem.orderItem;
import static com.prgrms.nabmart.domain.statistics.QStatistics.statistics;

import com.prgrms.nabmart.domain.category.MainCategory;
import com.prgrms.nabmart.domain.category.SubCategory;
import com.prgrms.nabmart.domain.item.Item;
import com.prgrms.nabmart.domain.item.ItemSortType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public List<Item> findNewItemsOrderBy(Long lastIdx, Long lastItemId, ItemSortType sortType,
        Pageable pageable) {
        OrderSpecifier orderSpecifier = createOrderSpecifier(sortType);
        Predicate predicate = item.createdAt.after(
            LocalDateTime.now().minus(NEW_PRODUCT_REFERENCE_TIME, ChronoUnit.WEEKS));

        return queryFactory
            .selectFrom(item)
            .join(item.statistics, statistics)
            .where(predicate)
            .groupBy(item)
            .having(
                getCondition(lastIdx, lastItemId, sortType)
            )
            .orderBy(orderSpecifier, item.itemId.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findHotItemsOrderBy(Long lastIdx, Long lastItemId, ItemSortType sortType,
        Pageable pageable) {
        OrderSpecifier orderSpecifier = createOrderSpecifier(sortType);
        Predicate predicate = item.rate.gt(HOT_PRODUCT_REFERENCE_RATE);
        Predicate orderCondition = JPAExpressions.select(orderItem.quantity.sum().coalesce(0))
            .from(orderItem)
            .where(orderItem.itemId.eq(item.itemId))
            .gt(HOT_PRODUCT_REFERENCE_ORDERS);

        return queryFactory
            .selectFrom(item)
            .join(item.statistics, statistics)
            .where(predicate, getCondition(lastIdx, lastItemId, sortType))
            .groupBy(item)
            .having(
                getCondition(lastIdx, lastItemId, sortType),
                orderCondition
            )
            .orderBy(orderSpecifier, item.itemId.asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findByMainCategoryOrderBy(MainCategory mainCategory, Long lastIdx,
        Long lastItemId, ItemSortType sortType, Pageable pageable) {

        Predicate mainCategoryCondition = item.mainCategory.eq(mainCategory);
        OrderSpecifier orderSpecifier = createOrderSpecifier(sortType);

        return queryFactory.selectFrom(item)
            .where(mainCategoryCondition, getCondition(lastIdx, lastItemId, sortType))
            .orderBy(orderSpecifier, item.itemId.desc())
            .limit(pageable.getPageSize())
            .fetch();
    }

    @Override
    public List<Item> findBySubCategoryOrderBy(MainCategory mainCategory, SubCategory subCategory,
        Long lastIdx, Long lastItemId, ItemSortType sortType, Pageable pageable) {

        Predicate mainCategoryCondition = item.mainCategory.eq(mainCategory);
        Predicate subCategoryCondition = item.subCategory.eq(subCategory);
        OrderSpecifier orderSpecifier = createOrderSpecifier(sortType);

        return queryFactory.selectFrom(item)
            .where(mainCategoryCondition, subCategoryCondition,
                getCondition(lastIdx, lastItemId, sortType))
            .orderBy(orderSpecifier, item.itemId.desc())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Predicate getCondition(Long lastIdx, Long lastItemId, ItemSortType sortType) {
        return switch (sortType) {
            case NEW -> item.itemId.lt(lastIdx);
            case HIGHEST_AMOUNT -> item.price.lt(lastIdx)
                .or(item.price.eq(lastIdx.intValue()).and(item.itemId.lt(lastItemId)));
            case LOWEST_AMOUNT -> item.price.gt(lastIdx)
                .or(item.price.eq(lastIdx.intValue()).and(item.itemId.lt(lastItemId)));
            case DISCOUNT -> item.discount.lt(lastIdx)
                .or(item.discount.eq(lastIdx.intValue()).and(item.itemId.lt(lastItemId)));
            default -> item.statistics.orders.lt(lastIdx);
        };
    }

    private OrderSpecifier createOrderSpecifier(ItemSortType sortType) {

        return switch (sortType) {
            case NEW -> new OrderSpecifier<>(Order.DESC, item.itemId);
            case HIGHEST_AMOUNT -> new OrderSpecifier<>(Order.DESC, item.price);
            case LOWEST_AMOUNT -> new OrderSpecifier<>(Order.ASC, item.price);
            case DISCOUNT -> new OrderSpecifier<>(Order.DESC, item.discount);
            default -> new OrderSpecifier<>(Order.DESC, item.statistics.orders);
        };
    }
}
