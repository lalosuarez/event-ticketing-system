package com.eventtickets.orders.graphql;

import com.eventtickets.orders.exception.InvalidOrderException;
import com.eventtickets.orders.exception.OrderException;
import com.eventtickets.orders.exception.OrderNotFoundException;
import com.eventtickets.orders.exception.InvalidTicketException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetchingEnvironment;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
class GraphqlExceptionHandler extends DataFetcherExceptionResolverAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlExceptionHandler.class);

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        if (ex instanceof OrderNotFoundException) {
            logger.error("Order not found", ex);
            return toGraphQLError(ex.getMessage(), getResultPath(env), getSourceLocation(env), ErrorType.NOT_FOUND);
        }
        if (ex instanceof InvalidOrderException || ex instanceof InvalidTicketException) {
            logger.error("Invalid order", ex);
            return toGraphQLError(ex.getMessage(), getResultPath(env), getSourceLocation(env), ErrorType.BAD_REQUEST);
        }
        if (ex instanceof OrderException) {
            logger.error("Order error", ex);
            return toGraphQLError(ex.getMessage(), getResultPath(env), getSourceLocation(env), ErrorType.INTERNAL_ERROR);
        }
        if (ex instanceof DataIntegrityViolationException) {
            logger.error("Data integrity violation", ex);
            return toGraphQLError("Data Integrity Violation", getResultPath(env), getSourceLocation(env),
                    ErrorType.INTERNAL_ERROR);
        }

        logger.error("Unknown error", ex);
        return null; // Defer to other resolvers or default handling
    }

    private GraphQLError toGraphQLError(String message, ResultPath resultPath, SourceLocation sourceLocation,
                                        ErrorType errorType) {
        return GraphqlErrorBuilder.newError()
                .errorType(errorType)
                .message(message)
                .path(resultPath) // Adds the field path
                .location(sourceLocation) // Sets the location
                .build();
    }

    private ResultPath getResultPath(DataFetchingEnvironment env) {
        return env.getExecutionStepInfo().getPath();
    }

    private SourceLocation getSourceLocation(DataFetchingEnvironment env) {
        return env.getMergedField().getSingleField().getSourceLocation();
    }
}

