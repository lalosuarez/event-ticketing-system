package com.eventtickets.order.graphql;

import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
class GraphqlValidationExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlValidationExceptionHandler.class);

    @GraphQlExceptionHandler(ConstraintViolationException.class)
    List<GraphQLError> handleValidationException(
            ConstraintViolationException ex, DataFetchingEnvironment env) {
        logger.error("Data constraint violation", ex);

        return ex.getConstraintViolations().stream()
                .map(violation -> GraphQLError.newError()
                        .errorType(ErrorType.BAD_REQUEST)
                        .message(violation.getMessage())
                        .path(env.getExecutionStepInfo().getPath()) // Adds the field path
                        .location(env.getMergedField().getSingleField().getSourceLocation()) // Sets the location
                        .build())
                .collect(Collectors.toList());
    }
}

