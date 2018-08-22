# Crud-Spring-Reactive-Rest-API

Spring ha liberado el Marco de trabajo web reactivo, y es uno de los tópicos más activos en el mundo Spring. Nuestros controladores, ahora corren en el motor reactivo spring. Spring Framework 5 abrasa Flujos Reactivos (Reactive Strings) y Reactor para su propio uso reactivo como también en muchas de su API’s principales. Vamos a hablar un poco de programación reactiva y escribir una API Rest CRUD con Spring Web-Flux.

Spring Framework 5 incluye un nuevo módulo, **spring-webflux**. El módulo contiene soporte para HTTP reactivo y clientes web socket, así también para aplicaciones web reactivas REST, navegador HTML y interrelaciones estilo Websocket. Spring web-flux soporta dos diferentes estilos de programación:

- En base a anotaciones, con `@Controller` y las otras anotaciones soportadas también en Spring MVC
- Funcional, estilo Java 8 Lambda, enrutamiento y manejo.

Abajo  es la imagen que demuestra los aspectos del lado del servidor, de ambos modelos de programación.  

![webflux-overview](/images/webflux-overview.png "Webflux Visión general")

Vamos a implementar, un servicio Rest CRUD el cual hará todas las operaciones relacionadas al objeto User. y entender el modulo reactivo Spring un poco más!

## Tecnologías usadas

- Spring 5
- Spring Boot 2
- JDK 1.8

## Dependencias del Proyecto

- Spring boot 2.0
- Spring Webflux
- Spring Reactive Data MongoDb.

### Crear el proyecto

- Navega a [https://start.spring.io/](https://start.spring.io/)
- Introduce el nombre del proyecto y el nombre del paquete tal como es requerido.
- Seleciona 2.1.0 (SNAPSHOT) Spring Boot
- Selecciona **Reactive Web** y **Reactive MongoDb** como dependencias del proyecto.
- Click en generar.
- Importa el proyecto a tu IDE favorito y crea la siguiente estructura:

![webflux-overview](/images/Reactive-288x300.png "Estructura personalizada del proyecto")

### Configura tu aplicación

Vamos anotar nuestra clase spring con @EnableReactiveMongoRepositories. Cualquiera anotación en módulos spring reactivos empezaran con el predicado **Enable…** seguido por **Reactive**. Activamos los repositorios de la API de persistencia JPA MongoDB reactivos de datos spring usando la anotación @EnableReactiveMongoRepositories el cual lleva los mismos atributos como el espacio de nombre en el XML. Si el paquete base no es configurado el usará el de la clase de configuración en la cual reside.

```java
package com.frugalis.ReactiveRest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class ReactiveRestApplication {

    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(ReactiveRestApplication.class, args);
    }
}
```

Vamos a configurar mongodb, vamos a usar la solución basada en la nube MLAB. [Por Favor visita este link para entender cómo configurar MLAB](http://frugalisminds.com/spring/spring-boot-mongodb-and-mlab/). Vamos a usar la url mongo en las propiedades de la aplicación.  

`spring.data.mongodb.uri=mongodb://<user>:<password>@ds163918.mlab.com:63918/spring`

Solo vamos a usar esto como una propiedad, pero también podemos especificar la propiedades de forma separada.

Acceso a la base de datos usando MongoDB Reactivo.

Primero vamos a crear una entidad modelo la cual va a persistir en nuestra base de datos Mongodb usando llamadas a la API rest basada en spring reactivo.

```java
package com.frugalis.ReactiveRest.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "Users")
public class Users implements Serializable {
    @Id
    int id;
    String name;
    String age;
    public Users(){}


    public Users(String name, String age) {
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }
    @Override
    public String toString() {
        return "Users [id=" + id + ", name=" + name + ", age=" + age + "]";
    }

}
```

La clase User es anotada con `@Document`, indicando que es una entidad JPA (Api de persistencia de JAVA). Es asumida como una entidad que será mapeada a una colección llamada Users. La propiedad id de Users es anotada con `@Id` para JPA la reconozca como el `ID` del objeto. Vamos a crear un repositorio el cual extiende algo del núcleo de la interfaz de datos de spring para ayudarnos a no escribir solicitudes o querys y que haga todo por nosotros.

Escribimos en nuestro repositorio para acceder a la database usando el marcador de interfaz de repositorio proporcionado por datos spring. Vamos a usar `@ReactiveMongoRepository`  el cual extiende `@ReactiveCrudRepository`. Estas interfaces nos proporcionan algunas operaciones básicas necesarias tal como, `save()`, `update()`, `findAll()`, `findById()` etc.

Nosotros podemos también escribir métodos plantillas los cuales internamente generan solicitudes o querys basados en la firma del método. En el siguiente código estamos escribiendo un método `findByName()` el cual escribirá una solicitud o query para seleccionar la colección en base al nombre.

```java
package com.frugalis.ReactiveRest.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.frugalis.ReactiveRest.entity.Users;

import reactor.core.publisher.Flux;

public interface UserRepository extends ReactiveMongoRepository<Users, Integer> {
     Flux<Users> findByName(String name);
}
```

### Entendiendo Flux y Mono en un respiro

Usamos **Flux** si necesitamos retornar un flujo de conjunto de datos que pueda emitir 0 o N elements:

`Flux<String> flux = Flux.just("a", "b", "c");`

**Mono** cuando es un flujo de 0..1 elements.

`Mono mono = Mono.just("Hii");`

Cómo estamos retornando una sola persona, por lo tanto usamo **Mono**

Escribiendo el controlador:

```java
package com.frugalis.ReactiveRest.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frugalis.ReactiveRest.entity.Users;
import com.frugalis.ReactiveRest.repository.UserRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")

public class UsersOldController {

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
}
```

Usando funciones de enrutamiento.

Usamos funciones de enrutamientos para definir el restos de endpoints en una forma funcional. Vamos a entender las funciones de enrutamientos en un artículo separado en detal. Este es un simple caso de uso de una ruta reactiva.  

```java
@Bean
RouterFunction<ServerResponse> helloRouterFunction() {

    RouterFunction<ServerResponse> routerFunction =
            RouterFunctions.route(RequestPredicates.path("/"),
                    serverRequest ->
                            ServerResponse.ok().body(Mono.just("Hello World!"), String.class));

    return routerFunction;
}
```

Ahora queremos cargar alguna datos en el inicio de la aplicación. Vamos a actualizar nuestro archivo `MainApplication.java`

```java
@Bean
CommandLineRunner runner() {
    return args -> {

        System.out.println("::::::::::::::::::::::");

        Mono<Void> sss = userRepository.deleteAll();

        sss.subscribe((e) -> {

        }, Throwable::printStackTrace);

        for (int i = 0; i <= 5; i++) {
            Users user= new Users("Test"+i, "1" + i);
            user.setId(i);

            Mono<Users> data = userRepository.save(user);
            System.out.println(data);

            data.subscribe((e) -> {
                System.out.println(e.toString());
            }, Throwable::printStackTrace);
        }
    };
```

Nosotros podemos ver que luego de retornar mono desde  `userRepository`  tenemos que suscribirnos, la ejecución sólo sucederá si se subscribe,  Tal cómo debes conocer en mis previos artículos, [Creando un servicio Rest con spring boot](http://frugalisminds.com/spring/creating-rest-service-spring-boot/) vimos cómo probamos el código usando **Postman**. Porfa sigue probando usando Postman.

## Integración  de la API Rest de prueba

Ahora que hemos creado una API Rest CRUD con programación reactiva Spring Web-Flux con spring 5, es tiempo de escribir alguna prueba de integración. Nosotros vamos a usar WebTestClient.

Es un cliente reactivo sin bloqueos, para probar servidores web. Usa el reactivo WebClient internamente para ejecutar solicitudes y proporcionar una fluida API para verificar respuestas, Ejecuta esta clase de prueba usando Junit.

```java
package com.frugalis.ReactiveRest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.frugalis.ReactiveRest.entity.Users;
import com.frugalis.ReactiveRest.repository.UserRepository;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReactiveRestApplicationTests {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void saveUser() {
        Users ps=new Users("dsld", "dfdf");
        userRepository.deleteAll().subscribe();

        webTestClient.post().uri("/users")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(Mono.just(ps), Users.class)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
            .expectBody()
            .jsonPath("$.name").isEqualTo("dsld");

    }

    @Test
    public void testUser() {
        webTestClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                .expectBodyList(User.class).hasSize(1);
    }
}

```

## Qué es programación reactiva

Programación reactiva es sobre aplicaciones sin bloqueos, controladas por eventos que escalan con un mínimo número de hilos. Es a menudo conocido como programación funcional también. Spring usa flujos reactivos para implementar programación reactiva. Si  provienes de la forma tradicional, entonces la programación reactiva es un poco más como una curva de aprendizaje para ti. Spring Framework 5.0 tiene integradas características reactivas usando el núcleo reactivo y la especificación de flujos reactivos. Un uso de caso donde la programación reactiva encaja perfectamente. Piensa en un sistema donde un número grande de eventos del sistema son producidos y consumidos de forma asíncrona. Si su sistema no puede procesar un alto número de datos o estas teniendo una alta cantidad de entradas de flujos de datos, su sistema todo el tiempo terminará con recursos escasos. En el mundo de hoy, el flujo de datos es mucho más que hace 5 años atrás. Incluso cuando duermes tus teléfonos y tablets están cambiando una alta cantidad de datos a un ritmo mucho mayor. Por lo tanto este tipo de modelo de programación ayudaran a nuestras aplicaciones a escalar mucho más que de lo que era hace algunos años.

[Source](http://frugalisminds.com/spring/crud-rest-api-with-spring-web-flux-reactive-programming-with-spring-5/)
