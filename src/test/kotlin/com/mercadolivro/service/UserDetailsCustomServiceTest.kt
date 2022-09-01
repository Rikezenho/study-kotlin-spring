package com.mercadolivro.service

import com.mercadolivro.exception.AuthenticationException
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.mercadolivro.enums.Errors
import java.util.Optional
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class UserDetailsCustomServiceTest {
    @MockK
    private lateinit var customerRepository: CustomerRepository

    @InjectMockKs
    private lateinit var userDetailsCustomService: UserDetailsCustomService

    @Test
    fun `should load user by username`() {
        val id = Random.nextInt()
        val customer = buildCustomer(id = id)

        every { customerRepository.findById(id) } returns Optional.of(customer)

        userDetailsCustomService.loadUserByUsername(id.toString())

        verify(exactly = 1) { customerRepository.findById(id) }
    }

    @Test
    fun `should throw exception when dont find user by username`() {
        val id = Random.nextInt()

        every { customerRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<AuthenticationException> { userDetailsCustomService.loadUserByUsername(id.toString()) }

        assertEquals(Errors.ML2002.message, error.message)
        assertEquals(Errors.ML2002.code, error.errorCode)
        verify(exactly = 1) { customerRepository.findById(id) }
    }

}