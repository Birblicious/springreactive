package com.reactive.mongo.reactiveApi.initialize;

import com.reactive.mongo.reactiveApi.document.Item;
import com.reactive.mongo.reactiveApi.repository.ItemReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

@Component
public class ItemDataInitializer implements CommandLineRunner {

    @Autowired
    ItemReactiveRepository itemReactiveRepository;

    @Override
    public void run(String... args) throws Exception {
        initialDataSetup();
    }

    public List<Item> data() {
       return Arrays.asList(
                new Item(null, "Samsung TV", 400.0),
                new Item(null, "LG TV", 420.0),
                new Item(null, "Apple Watch", 299.9),
                new Item(null, "Beats Headphones", 149.9),
                new Item("AB", "Bose Headphones", 149.9)
        );
    }

    private void initialDataSetup() {
        itemReactiveRepository.deleteAll()
                .thenMany(Flux.fromIterable(data()))
                .flatMap(itemReactiveRepository::save)
                .thenMany(itemReactiveRepository.findAll())
                .subscribe((item -> {
                    System.out.println("Item inserted from CommandLineRunner :\t" + item);
                }));
    }
}
