package com.eventtickets.order;

import com.eventtickets.order.exception.InvalidOrderException;
import com.eventtickets.order.jdbc.ClientRequestEntity;
import com.eventtickets.order.jdbc.ClientRequestRepository;
import com.eventtickets.order.jdbc.ClientRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
class IdempotentCreateOrderService implements IdempotentService<CreateOrderRequest, User, OrderResponse> {
    private static final Logger logger = LoggerFactory.getLogger(IdempotentCreateOrderService.class);
    private static final int EXMPIRATION_SECONDS = 30;

    private final ClientRequestRepository clientRequestRepository;
    private final ObjectMapper objectMapper;

    IdempotentCreateOrderService(ClientRequestRepository clientRequestRepository,
                                 ObjectMapper objectMapper) {
        this.clientRequestRepository = clientRequestRepository;
        this.objectMapper = objectMapper;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderResponse inProgress(CreateOrderRequest input, User user) {
        String currentHash = computeHash(input);

        Optional<ClientRequestEntity> existing = this.clientRequestRepository.findByIdAndUserId(
                input.clientMutationId(),
                user.id()
        );

        if (existing.isPresent()) {
            ClientRequestEntity clientRequestEntity = existing.get();

            // Security check: Payload tampering detection
            if (!clientRequestEntity.requestHash().equals(currentHash)) {
                logger.error("Request has already been used with different input parameters {}",
                        clientRequestEntity);
                throw new InvalidOrderException("Request has already been used with different input parameters");
            }

            if (ClientRequestStatus.COMPLETED.equals(clientRequestEntity.status())) {
                logger.warn("Request has already been completed, returning value from cache {}",
                        clientRequestEntity);
                return objectMapper.convertValue(clientRequestEntity.responsePayload(), OrderResponse.class);
            }

            if (ClientRequestStatus.IN_PROGRESS.equals(clientRequestEntity.status())
                    && clientRequestEntity.lockedUntil().isAfter(OffsetDateTime.now())) {
                logger.error("Request is currently being processed {}", clientRequestEntity);
                throw new InvalidOrderException("Request is currently being processed");
            }

            // Lock expired or retryable state: renew lock and proceed
            var renewed = this.clientRequestRepository.renew(input.clientMutationId(), user.id(),
                    OffsetDateTime.now().plusSeconds(EXMPIRATION_SECONDS));
            if (renewed == 0) {
                // Another thread grabbed the expired lock first
                throw new InvalidOrderException("Request is currently being processed");
            }
            logger.info("Request renewed {}", clientRequestEntity);
            return null;
        }

        // First attempt: insert lock with calculated hash
        var createdAt = OffsetDateTime.now();
        var saved = this.clientRequestRepository.save(new ClientRequestEntity(
                input.clientMutationId(),
                user.id(),
                currentHash,
                ClientRequestStatus.IN_PROGRESS,
                createdAt.plusSeconds(EXMPIRATION_SECONDS))
        );

        logger.info("Request saved {}", saved);
        return null;
    }

    @Override
    public String computeHash(CreateOrderRequest createOrderRequest) {
        // Concatenate deterministic values defining the transaction
        String canonicalString = String.join("|", createOrderRequest.ticketId());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalString.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(CreateOrderRequest input, User user, OrderResponse orderResponse) {
        this.clientRequestRepository.markCompleted(input.clientMutationId(), user.id(),
                this.objectMapper.writeValueAsString(orderResponse));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(CreateOrderRequest input, User user) {
        this.clientRequestRepository.markFailed(input.clientMutationId(), user.id());
    }
}
