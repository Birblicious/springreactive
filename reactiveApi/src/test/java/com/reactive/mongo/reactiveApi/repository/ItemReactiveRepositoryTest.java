package com.reactive.mongo.reactiveApi.repository;

import com.reactive.mongo.reactiveApi.document.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

@DataMongoTest
@RunWith(SpringRunner.class)
public class ItemReactiveRepositoryTest {
    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    List<Item> itemList = Arrays.asList(
            new Item(null, "Samsung TV", 400.0),
            new Item(null, "LG TV", 420.0),
            new Item(null, "Apple Watch", 299.9),
            new Item(null, "Beats Headphones", 149.9),
            new Item("AB", "Bose Headphones", 149.9));

    @Before
    public void setUp() {
        itemReactiveRepository.deleteAll()
        .thenMany(Flux.fromIterable(itemList))
        .flatMap(itemReactiveRepository::save)
        .doOnNext((item -> {
            System.out.println("Inserted item:\t" + item);
        })).blockLast(); //never use in actual code
    }

    @Test
    public void getAllItems() {
        StepVerifier.create(itemReactiveRepository.findAll())
        .expectSubscription()
        .expectNextCount(4)
        .verifyComplete();
    }

    @Test
    public void getItemByID() {
        StepVerifier.create(itemReactiveRepository.findById("AB"))
                .expectSubscription()
                .expectNextMatches(item -> item.getDescription().equals("Bose Headphones"))
                .verifyComplete();
    }

    @Test
    public void findItemByDescription() {
        StepVerifier.create(itemReactiveRepository.findByDescription("Bose Headphones"))
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    public void saveItem() {
        Item item = new Item("DE", "Google Home Mini",  30.00);
        Mono<Item> savedItem = itemReactiveRepository.save(item);

        StepVerifier.create(savedItem)
                .expectSubscription()
                .expectNextMatches(items -> (items.getId() != null && items.getDescription().equals("Google Home Mini")))
                .verifyComplete();
    }

    @Test
    public void updateItem() {
        double newPrice = 520.0;
        Mono<Item> upgradedItem = itemReactiveRepository.findByDescription("LG TV")
                .map(item -> {
                    item.setPrice(newPrice); //set new price
                    return item;
                })
                .flatMap(item -> {
                    return itemReactiveRepository.save(item); //saving the item with new price
                });
        StepVerifier.create(upgradedItem)
                .expectSubscription()
                .expectNextMatches(item -> item.getPrice() == newPrice)
                .verifyComplete();
    }

    @Test
    public void deleteItemById() {
       Mono<Void> deletedItem = itemReactiveRepository.findById("AB")
        .map(Item::getId)
        .flatMap((id) -> {
            return itemReactiveRepository.deleteById(id);
        });

       StepVerifier.create(deletedItem)
               .expectSubscription()
               .verifyComplete();

       StepVerifier.create(itemReactiveRepository.findAll())
               .expectSubscription()
               .expectNextCount(4)
               .verifyComplete();
    }
}
