package com.mercadolivro.service

import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Role
import com.mercadolivro.model.CustomerModel
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.*

@ExtendWith(MockKExtension::class)
class CustomerServiceTest {

    @MockK
    private lateinit var customerRepository: CustomerRepository

    @MockK
    private lateinit var bookService: BookService

    @MockK
    private lateinit var bCrypt: BCryptPasswordEncoder

    @InjectMockKs
    private lateinit var customerService: CustomerService

    @Test
    fun `should return all customers`() {
        val pageable = PageRequest.of(0, 10)

        val fakeCustomers = PageImpl(listOf(buildCustomer(), buildCustomer()), pageable, 2)
        every { customerRepository.findAll(pageable) } returns fakeCustomers

        val customers = customerService.getAll(null, pageable)

        assertEquals(fakeCustomers, customers)
        verify(exactly = 1) {
            customerRepository.findAll(pageable)
        }
        verify(exactly = 0) {
            customerRepository.findByNameContainingIgnoreCase(any(), pageable)
        }
    }

    @Test
    fun `should return customers when name is informed`() {
        val name = UUID.randomUUID().toString()
        val pageable = PageRequest.of(0, 10)

        val fakeCustomers = PageImpl(listOf(buildCustomer(), buildCustomer()), pageable, 2)
        every { customerRepository.findByNameContainingIgnoreCase(name, pageable) } returns fakeCustomers

        val customers = customerService.getAll(name, pageable)

        assertEquals(fakeCustomers, customers)
        verify(exactly = 0) {
            customerRepository.findAll(pageable)
        }
        verify(exactly = 1) {
            customerRepository.findByNameContainingIgnoreCase(name, pageable)
        }
    }

    @Test
    fun `should create customer and encrypt password`() {
        val initialPassword = Math.random().toString()
        val fakeCustomer = buildCustomer(password = initialPassword)

        val fakePassword = UUID.randomUUID().toString()
        val fakeCustomerEncrypted = fakeCustomer.copy(password = fakePassword)

        every { customerRepository.save(fakeCustomerEncrypted) } returns fakeCustomer
        every { bCrypt.encode(initialPassword) } returns fakePassword

        customerService.create(fakeCustomer)

        verify(exactly = 1) { customerRepository.save(fakeCustomerEncrypted) }
        verify(exactly = 1) { bCrypt.encode(initialPassword) }
    }

    fun buildCustomer(
        id: Int? = null,
        name: String = "customer name",
        email: String = "${UUID.randomUUID()}@email.com",
        password: String = "password"
    ) = CustomerModel(
        id = id,
        name = name,
        email = email,
        status = CustomerStatus.ATIVO,
        password = password,
        roles = setOf(Role.CUSTOMER)
    )
}