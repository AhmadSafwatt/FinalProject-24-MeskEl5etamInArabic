package com.homechef.CartService.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.homechef.CartService.model.ProductDTO;

@FeignClient(name = "product-service", url = "http://localhost:8085/products")
public interface ProductClient {

    
    @GetMapping("/{id}")
    ProductDTO getProductById(@PathVariable String id);

    @GetMapping("/ids")
    List<ProductDTO> getProductsById(@RequestBody List<String> ids) ;

}
