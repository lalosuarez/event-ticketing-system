package com.eventtickets.order;

interface IdempotentService<T, U, R> {

    R inProgress(T input1, U input2);

    void complete(T input, U input2, R response);

    void fail(T input, U input2);

    String computeHash(T input);
}
