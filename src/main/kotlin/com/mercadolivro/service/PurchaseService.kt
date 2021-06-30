package com.mercadolivro.service

import com.mercadolivro.model.PurchaseModel
import com.mercadolivro.repository.PurchaseRepository
import org.springframework.stereotype.Service

@Service
class PurchaseService(
    private val purchaseRepository: PurchaseRepository
) {
    fun create(purchaseModel: PurchaseModel) {
        purchaseRepository.save(purchaseModel)
    }
}
