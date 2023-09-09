package com.prgrms.nabmart.domain.user.service;

import static java.util.stream.Collectors.groupingBy;

import com.prgrms.nabmart.domain.user.UserGrade;
import com.prgrms.nabmart.domain.user.repository.UserRepository;
import com.prgrms.nabmart.domain.user.repository.response.UserOrderCount;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableSchedulerLock(defaultLockAtMostFor = "PT60S")
public class GradeService {

    private static final int ONE = 1;
    private static final int PAGE_SIZE = 1000;

    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Async
    @Scheduled(cron = "0 0 5 1 * *")
    @SchedulerLock(name = "Update_User_Grade", lockAtLeastFor = "PT10S")
    @Transactional
    public void updateUserGrade() {
        long totalUserCount = userRepository.count();
        long totalPage = totalUserCount / PAGE_SIZE;
        LocalDateTime startTimeOfPreviousMonth = getStartTimeOfPreviousMonth();
        LocalDateTime lastTimeOfPreviousMonth = getEndTimeOfPreviousMonth();

        for (int page = 0; page <= totalPage; page++) {
            PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);
            List<UserOrderCount> userOrderCounts = userRepository.getUserOrderCount(
                startTimeOfPreviousMonth,
                lastTimeOfPreviousMonth,
                pageRequest);
            Map<UserGrade, List<UserOrderCount>> userGradeGroup = groupByUserGrade(userOrderCounts);
            userGradeGroup.forEach(((userGrade, userOrderCountGroup) ->
                userRepository.updateUserGrade(userGrade, extractUserIds(userOrderCountGroup))));

            entityManager.clear();
        }
    }

    private LocalDateTime getStartTimeOfPreviousMonth() {
        return YearMonth.now()
            .minusMonths(ONE)
            .atDay(ONE)
            .atStartOfDay();
    }

    private LocalDateTime getEndTimeOfPreviousMonth() {
        return YearMonth.now()
            .atDay(ONE)
            .atStartOfDay()
            .minusNanos(ONE);
    }

    private Map<UserGrade, List<UserOrderCount>> groupByUserGrade(
        List<UserOrderCount> userOrderCounts) {
        return userOrderCounts.stream()
            .collect(groupingBy(userOrderCount ->
                UserGrade.calculateUserGrade(userOrderCount.getOrderCount())));
    }

    private List<Long> extractUserIds(List<UserOrderCount> userOrderCountGroup) {
        return userOrderCountGroup.stream()
            .map(UserOrderCount::getUserId)
            .toList();
    }
}
