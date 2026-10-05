package com.eventtickets.orders.graphql;

import com.eventtickets.orders.OrderNotFoundException;
import com.eventtickets.orders.InvalidTicketException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
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

            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.NOT_FOUND)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath()) // Adds the field path
                    .location(env.getMergedField().getSingleField().getSourceLocation()) // Sets the location
                    .build();
        }
        if (ex instanceof InvalidTicketException) {
            logger.error("Invalid ticket", ex);

            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.BAD_REQUEST)
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath()) // Adds the field path
                    .location(env.getMergedField().getSingleField().getSourceLocation()) // Sets the location
                    .build();
        }
        if (ex instanceof DataIntegrityViolationException) {
            logger.error("Data integrity violation", ex);

            return GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.INTERNAL_ERROR)
                    .message("Data Integrity Violation")
                    .path(env.getExecutionStepInfo().getPath()) // Adds the field path
                    .location(env.getMergedField().getSingleField().getSourceLocation()) // Sets the location
                    .build();
        }

        logger.error("Error occured", ex);
        return null; // Defer to other resolvers or default handling
    }
}

