package com.homechef.ProductService.controller;


import com.homechef.ProductService.config.JwtUtil;
import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.homechef.ProductService.config.JwtUtil.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductService productService;
    private JwtUtil jwtUtil = JwtUtil.getInstance();

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product createProduct(@RequestBody Map<String, Object> request, @RequestHeader("Authorization") String authHeader) {
        String type = (String) request.get("type");
        String name = (String) request.get("name");
        Double price = ((Number) request.get("price")).doubleValue();
        //UUID sellerId = UUID.fromString((String) request.get("sellerId"));
        String jwt = authHeader.replace("Bearer ", "");
        UUID sellerId = UUID.fromString(jwtUtil.getUserClaims(jwt).get("id").toString());
        int amountSold = ((Number) request.get("amountSold")).intValue();
        String description = request.get("description") != null ? (String) request.get("description") : "";
        Double discount = request.get("discount") != null ? ((Number) request.get("discount")).doubleValue() : 0.0;
        return productService.createProduct(type, name, sellerId, price, amountSold,description,discount,request);
    }
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PostMapping("/ids")
    public List<Product> getProductsById(@RequestBody List<String> ids) {
        return productService.getProductsById(ids);
    }

    @GetMapping("/most-sold")
    public List<Product> getMostSoldProducts() {
        return productService.getMostSoldProducts();
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable String id , @RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.replace("Bearer ", "");
        UUID sellerId = UUID.fromString(jwtUtil.getUserClaims(jwt).get("id").toString());
        productService.deleteProductById(id, sellerId);
    }

    @PutMapping("/{id}")
    public Optional<Product> updateProduct(@PathVariable String id, @RequestBody Map<String, Object> request,@RequestHeader("Authorization") String authHeader){
        String jwt = authHeader.replace("Bearer ", "");
        UUID sellerId = UUID.fromString(jwtUtil.getUserClaims(jwt).get("id").toString());
        return  productService.updateProduct(id,request,sellerId);
    }

    @PutMapping("/discount/{id}")
    public Double applyDiscount(@PathVariable String id, @RequestParam Double discount,@RequestHeader("Authorization") String authHeader){
        String jwt = authHeader.replace("Bearer ", "");
        UUID sellerId = UUID.fromString(jwtUtil.getUserClaims(jwt).get("id").toString());
        return  productService.applyDiscount(id,discount,sellerId);
    }
//
//    @PutMapping("/incrementAmountSold/{id}")
//    public Product incrementAmountSold(@PathVariable String id, @RequestParam int amount) {
//        return productService.incrementAmountSold(id, amount);
//    }
//
//    @PutMapping("/{id}/decrement")
//    public Product decrementAmountSold(@PathVariable String id, @RequestParam int amount) {
//       return productService.decrementAmountSold(id, amount);
//    }
}
