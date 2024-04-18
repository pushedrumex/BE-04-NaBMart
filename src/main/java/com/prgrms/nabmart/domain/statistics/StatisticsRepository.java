package com.prgrms.nabmart.domain.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.likes = st.likes + 1 where st.item.itemId = :itemId")
    void increaseLikes(@Param("itemId") Long itemId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.likes = st.likes - 1 where st.item.itemId = :itemId")
    void decreaseLikes(@Param("itemId") Long itemId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.orders = st.orders + :quantity where st.item.itemId = :itemId")
    void increaseOrders(@Param("itemId") Long itemId, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.orders = st.orders - :quantity where st.item.itemId = :itemId")
    void decreaseOrders(@Param("itemId") Long itemId, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.reviews = st.reviews + 1 where st.item.itemId = :itemId")
    void increaseReviews(@Param("itemId") Long itemId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Statistics st set st.reviews = st.reviews - 1 where st.item.itemId = :itemId")
    void decreaseReviews(@Param("itemId") Long itemId);

}
