package com.ssmu.security.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssmu.security.repository.UserRepository;
import com.ssmu.security.models.*;

@Controller
@RestController
public class UserController {
    @Autowired
    private UserRepository customerRepo;

  
    @GetMapping("/users")
    public String listAll() {
        System.out.println("controlles ok>>>");
        List<User> listCustomers = customerRepo.findAll();
        System.out.println("users lenght: %d" + listCustomers.size());
        for (User customer : listCustomers) {

            System.out.println(customer.getName());
        }

        return "customers";
    }

    @GetMapping(value = "/test")
    public String test() {
        return "hello world";
    }

}