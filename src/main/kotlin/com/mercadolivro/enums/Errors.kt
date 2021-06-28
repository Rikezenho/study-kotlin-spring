package com.mercadolivro.enums

enum class Errors(
    val code: String,
    val message: String
) {
    ML1001("ML-1001", "Book [%s] not exists"),
    ML1101("ML-1101", "Customer [%s] not exists")
}