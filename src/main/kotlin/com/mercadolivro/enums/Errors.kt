package com.mercadolivro.enums

enum class Errors(
    val code: String,
    val message: String
) {
    ML0000("ML-0000", "Unauthorized"),
    ML0001("ML-0001", "Invalid request"),
    ML1001("ML-1001", "Book [%s] not exists"),
    ML1002("ML-1002", "Cannot update book with status [%s]"),
    ML1101("ML-1101", "Customer [%s] not exists"),
    ML2001("ML-2001", "Fail to authenticate"),
    ML2002("ML-2002", "User not found"),
    ML2003("ML-2003", "Invalid token")
}