package com.prgrms.nabmart.domain.cart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.nabmart.domain.cart.service.CartItemService;
import com.prgrms.nabmart.domain.cart.service.request.RegisterCartItemCommand;
import com.prgrms.nabmart.global.fixture.AuthFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class CartItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartItemService cartItemService;

    @BeforeEach
    void setUp() {
        Authentication authentication = AuthFixture.usernamePasswordAuthenticationToken();
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("장바구니 상품 API 실행 시")
    class cartItemTest {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            // given
            RegisterCartItemCommand registerCartItemCommand = RegisterCartItemCommand.of(1L, 1L, 5);
            given(cartItemService.registerCartItem(any())).willReturn(1L);
            // when

            // then
            mockMvc.perform(post("/api/v1/cart-items")
                    .content(objectMapper.writeValueAsString(registerCartItemCommand))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/cart-items/1"))
                .andDo(print())
                .andDo(document("Create cart-item",
                        preprocessRequest(
                            prettyPrint()
                        ),
                        requestFields(
                            fieldWithPath("userId").type(JsonFieldType.NUMBER).description("userId"),
                            fieldWithPath("itemId").type(JsonFieldType.NUMBER).description("itemId"),
                            fieldWithPath("quantity").type(JsonFieldType.NUMBER).description("quantity")
                        )
                    )
                );
        }
    }
}
