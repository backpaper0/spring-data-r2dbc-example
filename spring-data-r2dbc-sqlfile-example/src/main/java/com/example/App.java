package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import reactor.core.publisher.Flux;

@EnableR2dbcRepositories(repositoryFactoryBeanClass = MyR2dbcRepositoryFactoryBean.class)
@SpringBootApplication
public class App implements CommandLineRunner {

    public static void main(final String[] args) {
        SpringApplication.run(App.class, args);
    }

    private final DatabaseClient client;
    private final MsgRepository repo;

    public App(final DatabaseClient client, final MsgRepository repo) {
        this.client = client;
        this.repo = repo;
    }

    @Override
    public void run(final String... args) throws Exception {

        client.execute().sql("CREATE TABLE msg (id IDENTITY, txt VARCHAR(100))").then().block();

        repo.saveAll(Flux.just("foo", "bar", "baz").map(Msg::create)).collectList().block();

        repo.findAllMsgs().toStream().forEach(System.out::println);
    }
}
