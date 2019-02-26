package com.example;

import java.util.Map;
import java.util.stream.Stream;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class App {

    public static void main(final String[] args) {

        final ConnectionFactory cf = new H2ConnectionFactory(
                H2ConnectionConfiguration.builder()
                        .inMemory("example-db")
                        .option("DB_CLOSE_DELAY=-1")
                        .build());

        Mono.from(cf.create()).map(con -> con
                .createStatement("CREATE TABLE msg (id IDENTITY, txt VARCHAR(100))").execute())
                .flatMap(Mono::from).block();

        //INSERT

        final Mono<?> inserted1 = Mono.from(cf.create())
                .map(con -> con.createStatement("INSERT INTO msg (txt) VALUES ($1)")
                        .bind("$1", "foo").execute())
                .flatMap(Mono::from);
        final Mono<?> inserted2 = Mono.from(cf.create())
                .map(con -> con.createStatement("INSERT INTO msg (txt) VALUES ($1)")
                        .bind("$1", "bar").execute())
                .flatMap(Mono::from);

        //TRANSACTION
        final Mono<?> inserted3 = Mono.from(cf.create())
                .flatMap(con -> Mono.from(con.beginTransaction())
                        .then(Mono.from(con.createStatement("INSERT INTO msg (txt) VALUES ($1)")
                                .bind("$1", "baz").execute())
                                .delayUntil(result -> con.commitTransaction())));

        Stream.of(inserted1, inserted2, inserted3).forEach(Mono::block);

        //SELECT

        final Flux<Map<String, Object>> records = Mono.from(cf.create())
                .flatMapMany(con -> con.createStatement("SELECT id, txt FROM msg").execute())
                .flatMap(result -> result.map((row, meta) -> Map.of(
                        "id", row.get("id", Long.class),
                        "txt", row.get("txt", String.class))));

        System.out.println(records.collectList().block());
    }
}
