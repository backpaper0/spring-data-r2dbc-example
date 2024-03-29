package com.example;

import java.util.Map;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.TransactionalDatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@EnableR2dbcRepositories
@SpringBootApplication
public class App implements CommandLineRunner {

    public static void main(final String[] args) {
        SpringApplication.run(App.class, args);
    }

    private final DatabaseClient client;
    private final TransactionalDatabaseClient clientTx;
    private final MsgRepository repo;

    public App(final DatabaseClient client, final ConnectionFactory factory,
            final MsgRepository repo) {
        this.client = client;
        this.clientTx = TransactionalDatabaseClient.create(factory);
        this.repo = repo;
    }

    @Override
    public void run(final String... args) throws Exception {

        client.execute().sql("CREATE TABLE msg (id IDENTITY, txt VARCHAR(100))").then().block();

        //--------------------------------------------------
        //INSERT

        final Mono<Void> inserted1 = client.execute().sql("INSERT INTO msg (txt) VALUES ($1)")
                .bind("$1", "foo").then();

        final Mono<Msg> inserted2 = repo.save(Msg.create("bar"));

        //TRANSACTION
        final Flux<Void> inserted3 = clientTx.inTransaction(
                client -> client.insert().into(Msg.class).using(Msg.create("baz")).then());

        Stream.of(inserted1, inserted2).forEach(Mono::block);
        inserted3.collectList().block();

        //--------------------------------------------------
        //SELECT

        final Flux<Map<String, Object>> records1 = client.execute().sql("SELECT id, txt FROM msg")
                .fetch()
                .all();

        final Flux<Map<String, Object>> records2 = client.select().from("msg").fetch().all();

        final Flux<Msg> records3 = client.execute().sql("SELECT id, txt FROM msg").as(Msg.class)
                .fetch()
                .all();

        final Flux<Msg> records4 = client.select().from(Msg.class).fetch().all();

        final Flux<Msg> records5 = repo.findAll();

        final Flux<Msg> records6 = repo.findAllMsgs();

        Stream.of(records1, records2, records3, records4, records5, records6)
                .map(f -> f.collectList().block())
                .forEach(System.out::println);
    }
}
