package com.github.laziestcoder.statemachinejavapoc.service;

import com.github.laziestcoder.statemachinejavapoc.entity.PaymentEntity;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentEvent;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import org.springframework.statemachine.StateMachine;

import java.util.UUID;

public interface PaymentService {
    PaymentEntity newPayment(PaymentEntity payment);
    StateMachine<PaymentState, PaymentEvent> preAuth(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> authorizePayment(UUID paymentId);
    StateMachine<PaymentState, PaymentEvent> declineAuth(UUID paymentId);

}
