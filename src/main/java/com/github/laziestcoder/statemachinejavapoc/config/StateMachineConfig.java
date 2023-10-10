package com.github.laziestcoder.statemachinejavapoc.config;

import com.github.laziestcoder.statemachinejavapoc.enums.PaymentEvent;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import com.github.laziestcoder.statemachinejavapoc.service.impl.PaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Random;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        try {
            states.withStates()
                    .initial(PaymentState.NEW)
                    .states(EnumSet.allOf(PaymentState.class))
                    .end(PaymentState.AUTH)
                    .end(PaymentState.PRE_AUTH)
                    .end(PaymentState.AUTH_ERROR);
        } catch (Exception ex) {
            log.error("Exception : {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        try {
            transitions
                    .withExternal().source(PaymentState.NEW).target(PaymentState.NEW).event(PaymentEvent.PRE_AUTHORIZE).and()
                    .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED).and()
                    .withExternal().source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTHORIZE);
        } catch (Exception ex) {
            log.error("Exception : {}", ex.getMessage(), ex);
            throw ex;
        }

    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        try {
            StateMachineListenerAdapter<PaymentState, PaymentEvent> adapter = new StateMachineListenerAdapter<PaymentState, PaymentEvent>() {
                @Override
                public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                    log.info("State changed from {} to {}", from, to);
                }
            };

            config.withConfiguration().listener(adapter);
        } catch (Exception ex) {
            log.error("Exception : {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    public Guard<PaymentState, PaymentEvent> paymentIdGuard() {
        return context -> {
            return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }

    public Action<PaymentState, PaymentEvent> preAuthAction() {
        return context -> {
            System.out.println("PreAuth was called!!!");

            if (new Random().nextInt(10) < 8) {
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            } else {
                System.out.println("Declined! No Credit!!!!!!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER, context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
}
