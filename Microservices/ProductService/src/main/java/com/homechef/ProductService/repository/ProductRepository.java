package com.homechef.ProductService.repository;

import com.homechef.ProductService.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends MongoRepository<Product, UUID> {


    Optional<Product> findById(UUID id);

    void deleteById(UUID id);

    Product findFirstByOrderByAmountSoldDesc();


    List<Product> findByAmountSold(int amountSold);




}