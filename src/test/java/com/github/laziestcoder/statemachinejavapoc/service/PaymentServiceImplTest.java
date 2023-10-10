package com.github.laziestcoder.statemachinejavapoc.service;

import com.github.laziestcoder.statemachinejavapoc.entity.PaymentEntity;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentEvent;
import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import com.github.laziestcoder.statemachinejavapoc.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;

    @Autowired
    PaymentRepository paymentRepository;

    PaymentEntity payment;

    @BeforeEach
    void setUp() {
        payment = PaymentEntity.builder().amount(new BigDecimal("15.75")).build();
    }

    @Transactional
    @Test
    void preAuth() {
        payment.setId(UUID.randomUUID());
        PaymentEntity savedPayment = paymentService.newPayment(payment);
        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
        PaymentEntity preAuthPayment = paymentRepository.findById(savedPayment.getId()).orElse(new PaymentEntity());
        System.out.println(sm.getState().getId());
        System.out.println(preAuthPayment);
    }
}