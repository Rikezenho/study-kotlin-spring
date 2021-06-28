package com.mercadolivro.model

import com.mercadolivro.enums.CustomerStatus
import javax.persistence.*

@Entity(name = "customer")
data class CustomerModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(name = "name")
    var name: String,

    @Column(name = "email")
    var email: String,

    @Column
    @Enumerated(EnumType.STRING)
    var status: CustomerStatus
)