package com.prgrms.nabmart.domain.statistics;

import com.prgrms.nabmart.domain.item.Item;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statisticsId;

    @Column(nullable = false)
    private Long likes = 0L;

    @Column(nullable = false)
    private Long reviews = 0L;

    @Column(nullable = false)
    private Long orders = 0L;

    @OneToOne
    @JoinColumn(name = "item_id")
    private Item item;

    public Statistics(Item item) {
        this.item = item;
    }
}
