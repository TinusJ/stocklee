package com.tinusj.stocklee.web;

import com.tinusj.stocklee.dto.StockDto;
import com.tinusj.stocklee.entity.Stock;
import com.tinusj.stocklee.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

/**
 * Web controller for Stock entity Thymeleaf views.
 */
@Controller
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockWebController {

    private final StockService stockService;

    /**
     * List all stocks
     */
    @GetMapping
    public String listStocks(Model model) {
        List<Stock> stocks = stockService.findAll();
        model.addAttribute("stocks", stocks);
        return "stock/list";
    }

    /**
     * Show stock details
     */
    @GetMapping("/{id}")
    public String showStock(@PathVariable UUID id, Model model) {
        Stock stock = stockService.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
        model.addAttribute("stock", stock);
        return "stock/details";
    }

    /**
     * Show create stock form
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("stockDto", new StockDto());
        model.addAttribute("marketTypes", Stock.MarketType.values());
        return "stock/form";
    }

    /**
     * Create new stock
     */
    @PostMapping
    public String createStock(@Valid @ModelAttribute StockDto stockDto, 
                             BindingResult result, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("marketTypes", Stock.MarketType.values());
            return "stock/form";
        }

        Stock stock = convertToEntity(stockDto);
        Stock savedStock = stockService.save(stock);
        
        redirectAttributes.addFlashAttribute("successMessage", "Stock created successfully!");
        return "redirect:/stocks/" + savedStock.getId();
    }

    /**
     * Show edit stock form
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable UUID id, Model model) {
        Stock stock = stockService.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
        
        StockDto stockDto = convertToDto(stock);
        model.addAttribute("stockDto", stockDto);
        model.addAttribute("marketTypes", Stock.MarketType.values());
        model.addAttribute("editMode", true);
        return "stock/form";
    }

    /**
     * Update stock
     */
    @PostMapping("/{id}")
    public String updateStock(@PathVariable UUID id, 
                             @Valid @ModelAttribute StockDto stockDto, 
                             BindingResult result, 
                             Model model, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("marketTypes", Stock.MarketType.values());
            model.addAttribute("editMode", true);
            return "stock/form";
        }

        Stock stock = convertToEntity(stockDto);
        stock.setId(id);
        stockService.save(stock);
        
        redirectAttributes.addFlashAttribute("successMessage", "Stock updated successfully!");
        return "redirect:/stocks/" + id;
    }

    /**
     * Delete stock
     */
    @PostMapping("/{id}/delete")
    public String deleteStock(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        stockService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Stock deleted successfully!");
        return "redirect:/stocks";
    }

    private StockDto convertToDto(Stock stock) {
        StockDto dto = new StockDto();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setName(stock.getName());
        dto.setCurrentPrice(stock.getCurrentPrice());
        dto.setDescription(stock.getDescription());
        dto.setMarket(stock.getMarket());
        dto.setCreatedAt(stock.getCreatedAt());
        dto.setUpdatedAt(stock.getUpdatedAt());
        return dto;
    }

    private Stock convertToEntity(StockDto dto) {
        Stock stock = new Stock();
        stock.setSymbol(dto.getSymbol());
        stock.setName(dto.getName());
        stock.setCurrentPrice(dto.getCurrentPrice());
        stock.setDescription(dto.getDescription());
        stock.setMarket(dto.getMarket());
        return stock;
    }
}