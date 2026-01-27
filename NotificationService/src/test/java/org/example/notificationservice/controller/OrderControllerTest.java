package org.example.notificationservice.controller;

import org.example.notificationservice.dto.OrderResponseDTO;
import org.example.notificationservice.service.OrderService;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.reflect.Array.get;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

//    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    void findAllOrdersSuccess() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(
                1L,
                "ORDER-123",
                100L,
                2,
                50.0,
                10L,
                90.0,
                5L,
                LocalDateTime.now());
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(orderService.findAllOrders(any())).thenReturn(page);

        mockMvc.perform(get("/api/orders/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value("ORDER-123"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findOrderByOrderIdSuccess() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(
                1L,
                "ORDER-123",
                100L,
                2,
                50.0,
                10L,
                90.0,
                5L,
                LocalDateTime.now());
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(dto));

        when(orderService.findOrderByOrderId(eq("ORDER-123"), any())).thenReturn(page);

        mockMvc.perform(get("/api/orders/ORDER-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderId").value("ORDER-123"));
    }

    @Test
    void findOrderByUserIdSuccess() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(
                1L,
                "ORDER-123",
                100L,
                2,
                50.0,
                10L,
                90.0,
                5L,
                LocalDateTime.now());
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(dto));

        when(orderService.findOrderByUserId(eq(5L), any())).thenReturn(page);

        mockMvc.perform(get("/api/orders/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userId").value(5));
    }

}
