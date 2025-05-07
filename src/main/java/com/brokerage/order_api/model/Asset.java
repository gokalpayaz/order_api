package com.brokerage.order_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal size;

    private BigDecimal  usableSize;

    @ManyToOne
    @JoinColumn(name= "customerId")
    private Customer customer;
}
