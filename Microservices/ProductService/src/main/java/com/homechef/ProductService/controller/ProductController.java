package com.homechef.ProductService.controller;


import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductService productService;


    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product createProduct(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String name = (String) request.get("name");
        Double price = ((Number) request.get("price")).doubleValue();
        UUID sellerId = UUID.fromString((String) request.get("sellerId"));
        int amountSold = ((Number) request.get("amountSold")).intValue();
        String description = request.get("description") != null ? (String) request.get("description") : "";
        Double discount = request.get("discount") != null ? ((Number) request.get("discount")).doubleValue() : 0.0;
        return productService.createProduct(type, name, sellerId, price, amountSold,description,discount);
    }
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }
    @GetMapping("/most-sold")
    public List<Product> getMostSoldProducts() {
        return productService.getMostSoldProducts();
    }
    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable String id) {
        productService.deleteProductById(id);
    }

    @PutMapping("/{id}")
    public Optional<Product> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> request){
        String name = (String) request.get("name");
        Double price = ((Number) request.get("price")).doubleValue();
        int amountSold = ((Number) request.get("amountSold")).intValue();
        System.out.println(name + price + amountSold);
        return  productService.updateProduct(id,name,price,amountSold);
    }
    @PutMapping("/{id}/incrementAmountSold")
    public void incrementAmountSold(@PathVariable String id, @RequestParam int amount) {
        productService.incrementAmountSold(id, amount);
    }

}
