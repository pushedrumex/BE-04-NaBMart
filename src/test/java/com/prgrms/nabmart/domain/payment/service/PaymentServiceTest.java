package com.prgrms.nabmart.domain.payment.service;

import static com.prgrms.nabmart.domain.order.support.OrderFixture.deliveringOrder;
import static com.prgrms.nabmart.domain.order.support.OrderFixture.payingOrder;
import static com.prgrms.nabmart.domain.order.support.OrderFixture.pendingOrder;
import static com.prgrms.nabmart.domain.order.support.OrderFixture.pendingOrderWithCoupon;
import static com.prgrms.nabmart.domain.payment.support.PaymentDtoFixture.paymentRequestResponse;
import static com.prgrms.nabmart.domain.payment.support.PaymentDtoFixture.paymentResponseWithSuccess;
import static com.prgrms.nabmart.domain.payment.support.PaymentFixture.canceledPayment;
import static com.prgrms.nabmart.domain.payment.support.PaymentFixture.pendingPayment;
import static com.prgrms.nabmart.domain.user.support.UserFixture.userWithUserId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prgrms.nabmart.domain.coupon.exception.InvalidUsedCouponException;
import com.prgrms.nabmart.domain.order.Order;
import com.prgrms.nabmart.domain.order.exception.NotPayingOrderException;
import com.prgrms.nabmart.domain.order.service.OrderCancelService;
import com.prgrms.nabmart.domain.order.service.OrderService;
import com.prgrms.nabmart.domain.payment.Payment;
import com.prgrms.nabmart.domain.payment.PaymentStatus;
import com.prgrms.nabmart.domain.payment.exception.DuplicatePayException;
import com.prgrms.nabmart.domain.payment.exception.NotFoundPaymentException;
import com.prgrms.nabmart.domain.payment.exception.PaymentAmountMismatchException;
import com.prgrms.nabmart.domain.payment.repository.PaymentRepository;
import com.prgrms.nabmart.domain.payment.service.response.PaymentRequestResponse;
import com.prgrms.nabmart.domain.payment.service.response.PaymentResponse;
import com.prgrms.nabmart.domain.user.User;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    PaymentService paymentService;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderService orderService;

    @Mock
    OrderCancelService orderCancelService;

    @Value("${payment.toss.success_url}")
    private String successCallBackUrl;

    @Value("${payment.toss.fail_url}")
    private String failCallBackUrl;

    @Value("${payment.toss.confirm-url}")
    private String confirmUrl;

    @Nested
    @DisplayName("pay 메서드 실행 시")
    class payTest {

        @Test
        @DisplayName("성공: 유효한 order 일 경우, PaymentResponse 를 반환")
        void pay() {
            // given
            User user = userWithUserId();
            Order order = pendingOrder(1L, user);

            PaymentRequestResponse expected = paymentRequestResponse(order, successCallBackUrl,
                failCallBackUrl);

            when(orderService.getOrderByOrderIdAndUserId(order.getOrderId(), user.getUserId()))
                .thenReturn(order);

            // when
            PaymentRequestResponse result = paymentService.pay(order.getOrderId(),
                user.getUserId());

            // then
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);
            verify(paymentRepository, times(1)).save(any());

        }

        @Test
        @DisplayName("예외: order 가 Pending 상태가 아닐 경우, DuplicatePayException 발생")
        void throwExceptionWhenNotPendingOrder() {
            // given
            User user = userWithUserId();
            Order order = deliveringOrder(1L, user);

            when(orderService.getOrderByOrderIdAndUserId(order.getOrderId(), user.getUserId()))
                .thenReturn(order);

            // when
            Exception exception = catchException(
                () -> paymentService.pay(order.getOrderId(), user.getUserId()));

            // then
            assertThat(exception).isInstanceOf(DuplicatePayException.class);
        }

        @Test
        @DisplayName("예외: coupon 이 이미 사용되었을 경우, AlreadyUsedCouponException 발생")
        void throwExceptionWhenAlreadyUsedCoupon() {
            // given
            User user = userWithUserId();

            Order order = pendingOrderWithCoupon(1L, user);
            order.useCoupon();

            when(orderService.getOrderByOrderIdAndUserId(order.getOrderId(), user.getUserId()))
                .thenReturn(order);

            // when
            Exception exception = catchException(
                () -> paymentService.pay(order.getOrderId(), user.getUserId()));

            // then
            assertThat(exception).isInstanceOf(InvalidUsedCouponException.class);
        }
    }

    @Nested
    @DisplayName("confirmPayment 메서드 실행 시")
    class ConfirmPaymentTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = userWithUserId();
            Order order = payingOrder(1L, user);
            Payment payment = pendingPayment(user, order);
            String mockPaymentKey = "mockPaymentKey";
            int amount = order.getPrice();
            PaymentResponse expected = paymentResponseWithSuccess();

            when(paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(),
                user.getUserId()))
                .thenReturn(Optional.of(payment));

            when(orderService.getOrderByUuidAndUserId(order.getUuid(), user.getUserId()))
                .thenReturn(order);

            // when
            PaymentResponse result = paymentService.processSuccessPayment(user.getUserId(),
                order.getUuid(), mockPaymentKey,
                amount);

            // then
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);

        }

        @Test
        @DisplayName("예외: 결제가 존재하지 않을 경우, NotFoundPaymentException 발생")
        void throwExceptionWhenNotFoundPayment() {
            // given
            User user = userWithUserId();
            Order order = payingOrder(1L, user);
            String mockPaymentKey = "mockPaymentKey";
            int amount = order.getPrice();

            when(paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(),
                user.getUserId()))
                .thenReturn(Optional.empty());

            // when
            Exception exception = catchException(
                () -> paymentService.processSuccessPayment(user.getUserId(),
                    order.getUuid(), mockPaymentKey,
                    amount));

            // then
            assertThat(exception).isInstanceOf(NotFoundPaymentException.class);

        }

        @Test
        @DisplayName("예외: 결제가 Pending 상태가 아닐 경우, DuplicatePayException 발생")
        void throwDuplicatePayException() {
            // given
            User user = userWithUserId();
            Order order = payingOrder(1L, user);
            Payment canceledPayment = canceledPayment(user, order);
            String mockPaymentKey = "mockPaymentKey";
            int amount = order.getPrice();

            when(paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(),
                user.getUserId()))
                .thenReturn(Optional.of(canceledPayment));

            // when
            Exception exception = catchException(
                () -> paymentService.processSuccessPayment(user.getUserId(),
                    order.getUuid(), mockPaymentKey,
                    amount));

            // then
            assertThat(exception).isInstanceOf(DuplicatePayException.class);
        }

        @Test
        @DisplayName("예외: 결제 금액이 일치하지 않는 경우, PaymentAmountMismatchException 발생")
        void throwExceptionWhenMismatchPayAmount() {
            // given
            User user = userWithUserId();
            Order order = payingOrder(1L, user);
            Payment payment = pendingPayment(user, order);
            String mockPaymentKey = "mockPaymentKey";
            int amount = 123;

            when(paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(),
                user.getUserId()))
                .thenReturn(Optional.of(payment));

            // when
            Exception exception = catchException(
                () -> paymentService.processSuccessPayment(user.getUserId(),
                    order.getUuid(), mockPaymentKey,
                    amount));

            // then
            assertThat(exception).isInstanceOf(PaymentAmountMismatchException.class);
        }

        @Test
        @DisplayName("예외: 주문이 Paying 상태가 아닐 경우, NotPayingOrderException 발생")
        void throwExceptionWhenNotPayingOrder() {
            // given
            User user = userWithUserId();
            Order order = pendingOrder(1L, user);
            Payment payment = pendingPayment(user, order);
            String mockPaymentKey = "mockPaymentKey";
            int amount = order.getPrice();

            when(paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(),
                user.getUserId()))
                .thenReturn(Optional.of(payment));

            when(orderService.getOrderByUuidAndUserId(order.getUuid(), user.getUserId()))
                .thenReturn(order);

            // when
            Exception exception = catchException(
                () -> paymentService.processSuccessPayment(user.getUserId(),
                    order.getUuid(), mockPaymentKey,
                    amount));

            // then
            assertThat(exception).isInstanceOf(NotPayingOrderException.class);
        }
    }

    @Nested
    @DisplayName("processFailPayment 메서드 실행 시")
    class CancelPaymentTest {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            User user = userWithUserId();
            Order order = payingOrder(1L, user);
            Payment payment = pendingPayment(user, order);
            String errorMessage = "errorMessage";

            PaymentResponse expected = new PaymentResponse(PaymentStatus.FAILED.toString(),
                errorMessage);

            when(orderService.getOrderByUuidAndUserId(order.getUuid(), user.getUserId()))
                .thenReturn(order);
            when(
                paymentRepository.findByOrder_UuidAndUser_UserId(order.getUuid(), user.getUserId()))
                .thenReturn(Optional.of(payment));

            // when
            PaymentResponse result = paymentService.processFailPayment(user.getUserId(),
                order.getUuid(), errorMessage);

            // then
            assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result).usingRecursiveComparison().isEqualTo(expected);

            verify(orderCancelService, times(1)).cancelOrder(order);

        }
    }
}
