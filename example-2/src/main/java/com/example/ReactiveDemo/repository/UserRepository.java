package com.example.ReactiveDemo.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.ReactiveDemo.entity.Users;

import reactor.core.publisher.Flux;

public interface UserRepository extends ReactiveMongoRepository<Users, Integer> {
     Flux<Users> findByName(String name);
}