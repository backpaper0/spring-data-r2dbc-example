package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.function.ReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.function.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.repository.query.R2dbcQueryMethod;
import org.springframework.data.r2dbc.repository.query.StringBasedR2dbcQuery;
import org.springframework.data.r2dbc.repository.support.R2dbcRepositoryFactory;
import org.springframework.data.relational.core.conversion.BasicRelationalConverter;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class MyR2dbcRepositoryFactory extends R2dbcRepositoryFactory {

    private final DatabaseClient databaseClient;
    private final MappingR2dbcConverter converter;

    public MyR2dbcRepositoryFactory(final DatabaseClient databaseClient,
            final MappingContext<? extends RelationalPersistentEntity<?>, RelationalPersistentProperty> mappingContext,
            final ReactiveDataAccessStrategy dataAccessStrategy) {
        super(databaseClient, mappingContext, dataAccessStrategy);
        this.databaseClient = databaseClient;
        this.converter = new MappingR2dbcConverter(new BasicRelationalConverter(mappingContext));
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(final Key key,
            final QueryMethodEvaluationContextProvider evaluationContextProvider) {
        final var original = super.getQueryLookupStrategy(key, evaluationContextProvider).get();
        return Optional.of(new MyQueryLookupStrategy(original, evaluationContextProvider));
    }

    private class MyQueryLookupStrategy implements QueryLookupStrategy {

        private final SpelExpressionParser expressionParser = new SpelExpressionParser();
        private final QueryLookupStrategy queryLookupStrategy;
        private final QueryMethodEvaluationContextProvider evaluationContextProvider;

        public MyQueryLookupStrategy(final QueryLookupStrategy queryLookupStrategy,
                final QueryMethodEvaluationContextProvider evaluationContextProvider) {
            this.queryLookupStrategy = queryLookupStrategy;
            this.evaluationContextProvider = evaluationContextProvider;
        }

        @Override
        public RepositoryQuery resolveQuery(final Method method, final RepositoryMetadata metadata,
                final ProjectionFactory factory, final NamedQueries namedQueries) {
            if (AnnotationUtils.findAnnotation(method, SqlFile.class) != null) {
                final var query = readSqlFile(method);
                final var queryMethod = new R2dbcQueryMethod(method, metadata, factory,
                        converter.getMappingContext());
                return new StringBasedR2dbcQuery(query, queryMethod, databaseClient, converter,
                        expressionParser, evaluationContextProvider);
            }
            return queryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        }

        private String readSqlFile(final Method method) {
            final String sqlFile = method.getDeclaringClass().getSimpleName() + "_"
                    + method.getName() + ".sql";
            try (InputStream in = getClass().getClassLoader().getResourceAsStream(sqlFile)) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
