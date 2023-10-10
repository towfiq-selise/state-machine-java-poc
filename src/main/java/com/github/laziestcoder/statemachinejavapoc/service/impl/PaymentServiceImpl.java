package com.github.laziestcoder.statemachinejavapoc.service.impl;

import com.github.laziestcoder.statemachinejavapoc.component.PaymentStateChangeInterceptor;
import com.github.laziestcoder.statemachinejavapoc.entity.PaymentEntity;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentEvent;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import com.github.laziestcoder.statemachinejavapoc.repository.PaymentRepository;
import com.github.laziestcoder.statemachinejavapoc.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static String PAYMENT_ID_HEADER = "payment_id";

    private final PaymentRepository paymentRepository;
    private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
    private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;

    @Override
    public PaymentEntity newPayment(PaymentEntity payment) {
        payment.setPaymentState(PaymentState.NEW);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> preAuth(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.PRE_AUTH_APPROVED);
        return sm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> authorizePayment(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_APPROVED);
        return sm;
    }

    @Override
    @Transactional
    public StateMachine<PaymentState, PaymentEvent> declineAuth(UUID paymentId) {
        StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);
        sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
        return sm;
    }

    /**
     * We can also retrieve the state machine from the database. The build() method in serviceimpl class achieve the same.
     */

    private StateMachine<PaymentState, PaymentEvent> build(UUID paymentId) {
        Optional<PaymentEntity> payment = paymentRepository.findById(paymentId);
        if (!payment.isPresent()) {
            String msg = String.format("Exception: Payment not found with id %s", paymentId);
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(paymentId);

        sm.stop();

        sm.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.resetStateMachine(new DefaultStateMachineContext<>(payment.get().getPaymentState(), null, null, null));
                });

        sm.start();

        return sm;
    }

    private void sendEvent(UUID paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event) {
        Message msg = MessageBuilder.withPayload(event)
                .setHeader(PAYMENT_ID_HEADER, paymentId)
                .build();

        sm.sendEvent(msg);
    }

}
