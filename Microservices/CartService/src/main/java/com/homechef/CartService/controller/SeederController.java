package com.homechef.CartService.controller;

import com.homechef.CartService.seed.DatabaseSeeder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("cart/api/seed")
public class SeederController {

    private final DatabaseSeeder databaseSeeder;

    public SeederController(DatabaseSeeder databaseSeeder) {
        this.databaseSeeder = databaseSeeder;
    }

    @GetMapping
    public ResponseEntity<String> seedDatabase() {
        databaseSeeder.seed();
        return ResponseEntity.ok("Database seeded successfully");
    }
}