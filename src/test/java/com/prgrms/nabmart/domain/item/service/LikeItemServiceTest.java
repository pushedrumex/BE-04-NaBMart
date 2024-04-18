package com.prgrms.nabmart.domain.item.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.prgrms.nabmart.domain.category.MainCategory;
import com.prgrms.nabmart.domain.category.SubCategory;
import com.prgrms.nabmart.domain.category.fixture.CategoryFixture;
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
import com.prgrms.nabmart.domain.item.service.response.FindLikeItemsResponse.FindLikeItemResponse;
import com.prgrms.nabmart.domain.item.support.ItemFixture;
import com.prgrms.nabmart.domain.statistics.StatisticsRepository;
import com.prgrms.nabmart.domain.user.User;
import com.prgrms.nabmart.domain.user.exception.NotFoundUserException;
import com.prgrms.nabmart.domain.user.repository.UserRepository;
import com.prgrms.nabmart.domain.user.support.UserFixture;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LikeItemServiceTest {

    @InjectMocks
    LikeItemService likeItemService;

    @Mock
    UserRepository userRepository;

    @Mock
    StatisticsRepository statisticsRepository;

    @Mock
    ItemRepository itemRepository;

    @Mock
    LikeItemRepository likeItemRepository;

    MainCategory mainCategory = CategoryFixture.mainCategory();
    SubCategory subCategory = CategoryFixture.subCategory(mainCategory);
    User user = UserFixture.user();
    Item item = ItemFixture.item(mainCategory, subCategory);
    LikeItem likeItem = ItemFixture.likeItem(user, item);

    @Nested
    @DisplayName("registerLikeItem 메서드 실행 시")
    class RegisterLikeItemTest {

        RegisterLikeItemCommand registerLikeItemCommand
            = RegisterLikeItemCommand.of(1L, 1L);

        @Test
        @DisplayName("성공")
        void success() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
            given(itemRepository.findById(any())).willReturn(Optional.ofNullable(item));
            given(likeItemRepository.existsByUserAndItem(any(), any()))
                .willReturn(false);

            //when
            likeItemService.registerLikeItem(registerLikeItemCommand);

            //then
            then(likeItemRepository).should().save(any());
            verify(statisticsRepository, times(1)).increaseLikes(likeItem.getItem().getItemId());
        }

        @Test
        @DisplayName("예외: 존재하지 않는 User")
        void throwExceptionWhenNotFoundUser() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> likeItemService.registerLikeItem(registerLikeItemCommand))
                .isInstanceOf(NotFoundUserException.class);
        }

        @Test
        @DisplayName("예외: 존재하지 않는 Item")
        void throwExceptionWhenNotFoundItem() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
            given(itemRepository.findById(any())).willReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> likeItemService.registerLikeItem(registerLikeItemCommand))
                .isInstanceOf(NotFoundItemException.class);
        }

        @Test
        @DisplayName("예외: 이미 찜한 Item")
        void throwExceptionWhenAlreadyLikedItem() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
            given(itemRepository.findById(any())).willReturn(Optional.ofNullable(item));
            given(likeItemRepository.existsByUserAndItem(any(), any()))
                .willReturn(true);

            //when
            //then
            assertThatThrownBy(() -> likeItemService.registerLikeItem(registerLikeItemCommand))
                .isInstanceOf(DuplicateLikeItemException.class);
        }
    }

    @Nested
    @DisplayName("deleteLikeItem 메서드 실행 시")
    class DeleteLikeItemTest {

        DeleteLikeItemCommand deleteLikeItemCommand = ItemFixture.deleteLikeItemCommand();

        @Test
        @DisplayName("성공")
        void success() {
            //given
            ReflectionTestUtils.setField(user, "userId", 1L);

            given(likeItemRepository.findById(any())).willReturn(Optional.ofNullable(likeItem));

            //when
            likeItemService.deleteLikeItem(deleteLikeItemCommand);

            //then
            then(likeItemRepository).should().delete(any());
            verify(statisticsRepository, times(1)).decreaseLikes(likeItem.getItem().getItemId());
        }

        @Test
        @DisplayName("예외: 존재하지 않는 LikeItem")
        void throwExceptionWhenNotFoundLikeItem() {
            //given
            given(likeItemRepository.findById(any())).willReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> likeItemService.deleteLikeItem(deleteLikeItemCommand))
                .isInstanceOf(NotFoundLikeItemException.class);
        }

        @Test
        @DisplayName("예외: LikeItem의 User와 일치하지 않는 userId")
        void throwExceptionWhenNotEqualsUser() {
            //given
            Long userId = 1L;
            Long notEqualsUserId = 2L;
            ReflectionTestUtils.setField(user, "userId", userId);
            DeleteLikeItemCommand notEqualsUserIdCommand
                = new DeleteLikeItemCommand(notEqualsUserId, 1L);

            given(likeItemRepository.findById(any())).willReturn(Optional.ofNullable(likeItem));

            //when
            //then
            assertThatThrownBy(() -> likeItemService.deleteLikeItem(notEqualsUserIdCommand))
                .isInstanceOf(UnauthorizedLikeItemException.class);
        }
    }

    @Nested
    @DisplayName("FindLikeItems 메서드 실행 시")
    class FindLikeItemsTest {

        FindLikeItemsCommand findLikeItemsCommand = ItemFixture.findLikeItemsCommand();

        private List<LikeItem> createLikeItems(int end) {
            List<LikeItem> likeItems = new ArrayList<>();
            for (int i = 0; i < end; i++) {
                Item item = ItemFixture.item(mainCategory, subCategory);
                LikeItem likeItem = ItemFixture.likeItem(user, item);
                likeItems.add(likeItem);
            }
            return likeItems;
        }

        List<LikeItem> likeItems = createLikeItems(3);
        PageImpl<LikeItem> likeItemsPage = new PageImpl<>(likeItems);

        @Test
        @DisplayName("성공: 동일한 요소 개수, 페이지, 요소 총 개수")
        void successValidFindLikeItemsResponse() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
            given(likeItemRepository.findByUserWithItem(any(), any())).willReturn(likeItemsPage);

            //when
            FindLikeItemsResponse findLikeItemsResponse
                = likeItemService.findLikeItems(findLikeItemsCommand);

            //then
            assertThat(findLikeItemsResponse.items()).hasSize(3);
            assertThat(findLikeItemsResponse.page()).isEqualTo(0);
            assertThat(findLikeItemsResponse.totalElements()).isEqualTo(3);
        }

        @Test
        @DisplayName("성공: 동일한 단일 요소 값")
        void successValidFindLikeItemResponse() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.ofNullable(user));
            given(likeItemRepository.findByUserWithItem(any(), any())).willReturn(likeItemsPage);

            //when
            FindLikeItemsResponse findLikeItemsResponse
                = likeItemService.findLikeItems(findLikeItemsCommand);

            //then
            List<FindLikeItemResponse> findLikeItemResponses = findLikeItemsResponse.items();
            FindLikeItemResponse itemResponse = findLikeItemResponses.get(0);
            LikeItem findLikeItem = likeItems.get(0);
            assertThat(itemResponse.name()).isEqualTo(findLikeItem.getItem().getName());
            assertThat(itemResponse.price()).isEqualTo(findLikeItem.getItem().getPrice());
            assertThat(itemResponse.discount()).isEqualTo(findLikeItem.getItem().getDiscount());
            assertThat(itemResponse.rate()).isEqualTo(findLikeItem.getItem().getRate());
        }

        @Test
        @DisplayName("예외: 존재하지 않는 User")
        void throwExceptionWhenNotFoundUser() {
            //given
            given(userRepository.findById(any())).willReturn(Optional.empty());

            //when
            //then
            assertThatThrownBy(() -> likeItemService.findLikeItems(findLikeItemsCommand))
                .isInstanceOf(NotFoundUserException.class);
        }
    }
}