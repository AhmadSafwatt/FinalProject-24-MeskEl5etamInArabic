package com.homechef.AuthService.Seeder;

import com.homechef.AuthService.Models.User;
import com.homechef.AuthService.Repositories.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/seed")
public class SeedUsers {

    private final UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    public SeedUsers(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String seedUsers() {
        if (userRepository.count() > 0) {
            return "Database already seeded!";
        }
        // Check if the database is empty
        String[] uuids = {"ae73c3fd-f444-4a54-9822-fa24ab6747c3",
                "2f3dc195-587d-4a6a-8da1-b0587c4a2310",
                "1fc3a55a-960a-4c94-bf83-a0c262d4e514",
                "d01a8a5e-544b-4c03-bb2e-c146c6e9e391",
                "2c3868e2-ac2a-414f-a2b8-8c9caeb9cb56",
                "5a1d1902-8cad-4810-9621-8cb1ded5ff13",
                "e24f4ca0-d6d5-4a91-b453-45dde29067d5",
                "09895bc2-9303-460f-8c20-d8222919e941",
                "cfb72bb9-ee43-4ee4-a701-1a4c22452394",
                "e8af0c58-8bd1-4c2e-baa6-9645a3f7aa98"};

        String emailPrefix = "omar.syd.n";

        // Create and save users
        for (int i = 1, j = 1; i <= 10; i++, j++) {
            String username = ((i < 6) ? "customer" : "seller") + j;
            String password = "pass" + j;
            emailPrefix += ".";
            String email = (emailPrefix) + "@gmail.com";

            User user = new User(UUID.fromString(uuids[i-1]),username, password, email);
            user.setAndHashPassword(password);
            user.setAddress(username + "'s address");
            user.setPhoneNumber("012345678" + j);
            if (i < 6) {
                user.setRoleCustomer();
            } else {
                user.setRoleSeller();
            }
            userRepository.save(user);
            if (i == 5) {
                j = 0;
            }
        }

        // update the ids
//        for (int i = 1, j = 1; i <= 10; i++, j++) {
//            User user = userRepository.findByUsername(((i < 6) ? "customer" : "seller") + j).orElse(null);
//            user.setId(UUID.fromString(uuids[i - 1]));
//            userRepository.save(user);
//            if (i == 5) {
//                j = 0;
//            }
//        }


        return "Users seeded successfully!";
    }
}
