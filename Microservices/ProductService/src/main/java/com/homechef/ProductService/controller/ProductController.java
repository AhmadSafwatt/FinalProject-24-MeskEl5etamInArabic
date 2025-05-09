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
        return productService.createProduct(type, name, sellerId, price, amountSold,description,discount,request);
    }
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @GetMapping("/ids")
    public List<Product> getProductsById(@RequestBody List<String> ids) {
        return productService.getProductsById(ids);
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
        return  productService.updateProduct(id,request);
    }


    @PutMapping("/discount/{id}")
    public Double applyDiscount(@PathVariable String id, @RequestParam Double discount){
        return  productService.applyDiscount(id,discount);
    }


    @PutMapping("/incrementAmountSold/{id}")
    public Product incrementAmountSold(@PathVariable String id, @RequestParam int amount) {

        return productService.incrementAmountSold(id, amount);

    }



    @PutMapping("/{id}/decrement")
    public Product decrementAmountSold(@PathVariable String id, @RequestParam int amount) {
       return productService.decrementAmountSold(id, amount);
    }





}
