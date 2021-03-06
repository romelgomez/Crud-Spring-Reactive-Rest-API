package com.example.ReactiveDemo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.example.ReactiveDemo.entity.Users;
import com.example.ReactiveDemo.repository.UserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UsersNewController {

    @Autowired
    UserRepository userRepository;

    @GetMapping
    public Flux<Users> getAllUser() {
        return userRepository.findAll();
    }

    @PostMapping
    public Mono<Users> createUser( @RequestBody Users user) {
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Users>> getUserById(@PathVariable(value = "id") int userId) {
        return userRepository.findById(userId)
                .map(savedTweet -> ResponseEntity.ok(savedTweet))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // @Bean
    // RouterFunction<ServerResponse> helloRouterFunction() {
    //     RouterFunction<ServerResponse> routerFunction =
    //             RouterFunctions.route(RequestPredicates.path("/"), serverRequest -> ServerResponse.ok().body(Mono.just("Hello World!"), String.class));
    //     return routerFunction;
    // }
 
}