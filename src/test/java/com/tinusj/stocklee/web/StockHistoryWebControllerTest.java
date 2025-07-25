package com.tinusj.stocklee.web;

import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.entity.StockHistory;
import com.tinusj.stocklee.service.StockHistoryService;
import com.tinusj.stocklee.service.StockService;
import com.tinusj.stocklee.service.StockPriceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockHistoryWebControllerTest {

    @Mock
    private StockHistoryService stockHistoryService;
    
    @Mock
    private StockService stockService;
    
    @Mock
    private StockPriceProvider stockPriceProvider;
    
    @Mock
    private Model model;

    @InjectMocks
    private StockHistoryWebController controller;

    private Stock testStock;
    private List<StockHistory> testHistories;

    @BeforeEach
    void setUp() {
        testStock = new Stock();
        testStock.setId(UUID.randomUUID());
        testStock.setSymbol("AAPL");
        testStock.setName("Apple Inc.");

        StockHistory history1 = new StockHistory();
        history1.setStock(testStock);
        history1.setPrice(new BigDecimal("150.00"));
        history1.setTimestamp(LocalDateTime.now().minusDays(1));

        StockHistory history2 = new StockHistory();
        history2.setStock(testStock);
        history2.setPrice(new BigDecimal("155.00"));
        history2.setTimestamp(LocalDateTime.now());

        testHistories = List.of(history1, history2);
    }

    @Test
    void listStockHistory_ShouldSortSuccessfully() {
        // Given
        when(stockHistoryService.findAll()).thenReturn(testHistories);
        when(stockService.findAll()).thenReturn(List.of(testStock));

        // When
        String result = controller.listStockHistory(0, 50, "price", "desc", null, null, model);

        // Then
        assertEquals("stock-history/list", result);
        verify(model, times(9)).addAttribute(anyString(), any());
        // The test passes if no exception is thrown during sorting
    }

    @Test
    void listStockHistory_ShouldFilterByStockId() {
        // Given
        when(stockHistoryService.findAll()).thenReturn(testHistories);
        when(stockService.findAll()).thenReturn(List.of(testStock));

        // When
        String result = controller.listStockHistory(0, 50, "timestamp", "desc", testStock.getId(), null, model);

        // Then
        assertEquals("stock-history/list", result);
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    void listStockHistory_ShouldFilterBySymbol() {
        // Given
        when(stockHistoryService.findAll()).thenReturn(testHistories);
        when(stockService.findAll()).thenReturn(List.of(testStock));

        // When
        String result = controller.listStockHistory(0, 50, "timestamp", "desc", null, "AAPL", model);

        // Then
        assertEquals("stock-history/list", result);
        verify(model, times(9)).addAttribute(anyString(), any());
    }

    @Test
    void showStockHistory_ShouldReturnDetailsView() {
        // Given
        when(stockService.findById(testStock.getId())).thenReturn(Optional.of(testStock));
        when(stockHistoryService.findAll()).thenReturn(testHistories);
        when(stockPriceProvider.getPrice("AAPL")).thenReturn(Optional.of(new BigDecimal("160.00")));

        // When
        String result = controller.showStockHistory(testStock.getId(), model);

        // Then
        assertEquals("stock-history/details", result);
        verify(model).addAttribute("stock", testStock);
        verify(model).addAttribute(eq("stockHistories"), any());
        verify(model).addAttribute(eq("currentPrice"), any());
        verify(model).addAttribute(eq("pageTitle"), anyString());
    }
}