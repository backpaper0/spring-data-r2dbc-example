package com.example;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.query.Query;

import reactor.core.publisher.Flux;

public interface MsgRepository extends R2dbcRepository<Msg, Long> {

    @Query("SELECT id, txt FROM msg")
    Flux<Msg> findAllMsgs();
}
