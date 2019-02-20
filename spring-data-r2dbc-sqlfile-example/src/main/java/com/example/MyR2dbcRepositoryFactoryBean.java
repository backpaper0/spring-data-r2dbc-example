package com.example;

import java.io.Serializable;

import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactoryBean;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

public class MyR2dbcRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
        extends R2dbcRepositoryFactoryBean<T, S, ID> {

    private ReactiveDataAccessStrategy dataAccessStrategy;

    public MyR2dbcRepositoryFactoryBean(final Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport getFactoryInstance(final DatabaseClient client,
            final MappingContext<? extends RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext) {
        return new MyR2dbcRepositoryFactory(client, mappingContext, dataAccessStrategy);
    }

    @Override
    public void setDataAccessStrategy(final ReactiveDataAccessStrategy dataAccessStrategy) {
        super.setDataAccessStrategy(dataAccessStrategy);
        this.dataAccessStrategy = dataAccessStrategy;
    }
}
