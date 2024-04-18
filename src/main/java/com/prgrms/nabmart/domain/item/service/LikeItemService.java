package com.prgrms.nabmart.domain.item.service;

import com.prgrms.nabmart.domain.item.Item;
import com.prgrms.nabmart.domain.item.LikeItem;
import com.prgrms.nabmart.domain.item.exception.DuplicateLikeItemException;
import com.prgrms.nabmart.domain.item.exception.NotFoundItemException;
import com.prgrms.nabmart.domain.item.exception.NotFoundLikeItemException;
import com.prgrms.nabmart.domain.item.exception.UnauthorizedLikeItemException;
import com.prgrms.nabmart.domain.item.repository.ItemRepository;
import com.prgrms.nabmart.domain.item.repository.LikeItemRepository;
import com.prgrms.nabmart.domain.item.service.request.DeleteLikeItemCommand;
import com.prgrms.nabmart.domain.item.service.request.FindLikeItemsCommand;
import com.prgrms.nabmart.domain.item.service.request.RegisterLikeItemCommand;
import com.prgrms.nabmart.domain.item.service.response.FindLikeItemsResponse;
import com.prgrms.nabmart.domain.statistics.StatisticsRepository;
import com.prgrms.nabmart.domain.user.User;
import com.prgrms.nabmart.domain.user.exception.NotFoundUserException;
import com.prgrms.nabmart.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final LikeItemRepository likeItemRepository;
    private final StatisticsRepository statisticsRepository;

    @Transactional
    public Long registerLikeItem(RegisterLikeItemCommand registerLikeItemCommand) {
        User user = findUserByUserId(registerLikeItemCommand.userId());
        Item item = findItemByItemId(registerLikeItemCommand.itemId());
        checkDuplicateLikedItem(user, item);
        LikeItem likeItem = new LikeItem(user, item);
        likeItemRepository.save(likeItem);
        statisticsRepository.increaseLikes(item.getItemId());
        return likeItem.getLikeItemId();
    }

    @Transactional
    public void deleteLikeItem(DeleteLikeItemCommand deleteLikeItemCommand) {
        LikeItem likeItem = findLikeItemByLikeItemId(deleteLikeItemCommand);
        if (!likeItem.isSameUser(deleteLikeItemCommand.userId())) {
            throw new UnauthorizedLikeItemException("권한이 없습니다.");
        }
        likeItemRepository.delete(likeItem);
        statisticsRepository.decreaseLikes(likeItem.getItem().getItemId());
    }

    private void checkDuplicateLikedItem(final User user, final Item item) {
        if (likeItemRepository.existsByUserAndItem(user, item)) {
            throw new DuplicateLikeItemException("이미 찜한 상품입니다.");
        }
    }

    @Transactional(readOnly = true)
    public FindLikeItemsResponse findLikeItems(FindLikeItemsCommand findLikeItemsCommand) {
        User user = findUserByUserId(findLikeItemsCommand.userId());
        Page<LikeItem> findLikeItemsPage
            = likeItemRepository.findByUserWithItem(user, findLikeItemsCommand.pageable());
        return FindLikeItemsResponse.from(findLikeItemsPage);
    }

    private User findUserByUserId(final Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundUserException("존재하지 않는 유저입니다."));
    }

    private Item findItemByItemId(final Long itemId) {
        return itemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundItemException("존재하지 않는 상품입니다."));
    }

    private LikeItem findLikeItemByLikeItemId(DeleteLikeItemCommand deleteLikeItemCommand) {
        return likeItemRepository.findById(deleteLikeItemCommand.likeItemId())
            .orElseThrow(() -> new NotFoundLikeItemException("존재하지 않는 찜 상품입니다."));
    }
}
