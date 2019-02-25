package com.example;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;

public interface MsgRepository extends R2dbcRepository<Msg, Long> {

    @SqlFile
    Flux<Msg> findAllMsgs();
}
