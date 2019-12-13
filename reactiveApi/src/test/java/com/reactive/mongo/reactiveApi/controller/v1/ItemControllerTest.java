package com.reactive.mongo.reactiveApi.controller.v1;

import com.reactive.mongo.reactiveApi.constants.ItemConstants;
import com.reactive.mongo.reactiveApi.document.Item;
import com.reactive.mongo.reactiveApi.repository.ItemReactiveRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@DirtiesContext
@AutoConfigureWebTestClient
public class ItemControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    public List<Item> data() {
        return Arrays.asList(
                new Item(null, "Samsung TV", 400.0),
                new Item(null, "LG TV", 420.0),
                new Item(null, "Apple Watch", 299.9),
                new Item(null, "Beats Headphones", 149.9),
                new Item("AB", "Bose Headphones", 149.9)
        );
    }

    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemReactiveRepository::save)
                .doOnNext((item -> {
                    System.out.println("Inserted Item is:\t" + item);
                }))
                .blockLast();
    }

    @Test
    public void getAllItems() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Item.class)
                .hasSize(5)
        .consumeWith((response) -> {
            List<Item> items = response.getResponseBody();
            items.forEach((item) -> {
                assert(item.getId() != null);
            });
        });
    }

    @Test
    public void getOneItem() {
        webTestClient.get().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "AB")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", 149.9);
    }

    @Test
    public void createItem() {
        Item item = new Item(null, "IPhone X", 999.99);
        webTestClient.post().uri(ItemConstants.ITEM_END_POINT_V1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.description").isEqualTo("IPhone X")
                .jsonPath("$.price").isEqualTo(999.99);
    }

    @Test
    public void deleteItem() {
        webTestClient.delete().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "AB")
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(Void.class);
    }

    @Test
    public void updateItem() {
        double newPrice = 129.9;
        Item item = new Item(null, "Bose Headphones", newPrice);

        webTestClient.put().uri(ItemConstants.ITEM_END_POINT_V1.concat("/{id}"), "AB")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(item), Item.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.price", newPrice);

    }

}
