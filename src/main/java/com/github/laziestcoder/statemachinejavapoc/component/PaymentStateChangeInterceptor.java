package com.github.laziestcoder.statemachinejavapoc.component;

import com.github.laziestcoder.statemachinejavapoc.entity.PaymentEntity;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentEvent;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import com.github.laziestcoder.statemachinejavapoc.repository.PaymentRepository;
import com.github.laziestcoder.statemachinejavapoc.service.impl.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;
import org.springframework.messaging.Message;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state,
                               Message<PaymentEvent> message,
                               Transition<PaymentState, PaymentEvent> transition,
                               StateMachine<PaymentState, PaymentEvent> stateMachine,
                               StateMachine<PaymentState, PaymentEvent> rootStateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(UUID.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1L)))
                    .ifPresent(paymentId -> {
                        PaymentEntity payment = paymentRepository.getOne(paymentId);
                        payment.setPaymentState(state.getId());
                        paymentRepository.save(payment);
                    });
        });
    }
}
