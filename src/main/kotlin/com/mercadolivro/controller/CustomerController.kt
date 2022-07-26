package com.mercadolivro.controller

import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.controller.response.CustomerResponse
import com.mercadolivro.enums.Role
import com.mercadolivro.extension.toCustomerModel
import com.mercadolivro.extension.toResponse
import com.mercadolivro.security.UserCanOnlyAccessTheirOwnResource
import com.mercadolivro.service.CustomerService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("customer")
class CustomerController(
    val customerService: CustomerService
) {
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getAll(@RequestParam name: String?, @PageableDefault(page = 0, size = 10) pageable: Pageable): Page<CustomerResponse> {
        return customerService.getAll(name, pageable).map { it.toResponse() }
    }

    @GetMapping("/{id}")
    @UserCanOnlyAccessTheirOwnResource
    fun get(@PathVariable id: Int): CustomerResponse? {
        return customerService.findById(id).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody @Valid customer: PostCustomerRequest) {
        customerService.create(customer.toCustomerModel())
    }

    @PutMapping("/{id}")
    @UserCanOnlyAccessTheirOwnResource
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun update(@PathVariable id: Int, @RequestBody @Valid customer: PutCustomerRequest) {
        val customerSaved = customerService.findById(id)
        customerService.update(customer.toCustomerModel(customerSaved))
    }

    @DeleteMapping("/{id}")
    @UserCanOnlyAccessTheirOwnResource
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@PathVariable id: Int) {
        customerService.remove(id)
    }
}