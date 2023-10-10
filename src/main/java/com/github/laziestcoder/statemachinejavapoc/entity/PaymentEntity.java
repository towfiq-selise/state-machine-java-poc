package com.github.laziestcoder.statemachinejavapoc.entity;

import com.github.laziestcoder.statemachinejavapoc.enums.PaymentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "payment")
public class PaymentEntity {

    @Id
    @Column(name = "payment_id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private PaymentState paymentState;

    private BigDecimal amount;

}
