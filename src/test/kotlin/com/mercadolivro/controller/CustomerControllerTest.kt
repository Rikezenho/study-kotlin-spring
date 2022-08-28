package com.mercadolivro.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.mercadolivro.controller.request.PostCustomerRequest
import com.mercadolivro.controller.request.PutCustomerRequest
import com.mercadolivro.enums.CustomerStatus
import com.mercadolivro.enums.Role
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.CustomerRepository
import com.mercadolivro.security.UserCustomDetails
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.random.Random

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
@ActiveProfiles("test")
@WithMockUser(username = "username", password = "password", roles = ["CUSTOMER"])
class CustomerControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() = customerRepository.deleteAll()

    @AfterEach
    fun tearDown() = customerRepository.deleteAll()

    @Nested
    inner class `get all customers`() {
        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return all customers when is admin`() {
            val customer1 = customerRepository.save(buildCustomer())
            val customer2 = customerRepository.save(buildCustomer())

            mockMvc.perform(get("/customers"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].id").value(customer1.id))
                .andExpect(jsonPath("$.items[0].name").value(customer1.name))
                .andExpect(jsonPath("$.items[0].email").value(customer1.email))
                .andExpect(jsonPath("$.items[0].status").value(customer1.status.name))
                .andExpect(jsonPath("$.items[1].id").value(customer2.id))
                .andExpect(jsonPath("$.items[1].name").value(customer2.name))
                .andExpect(jsonPath("$.items[1].email").value(customer2.email))
                .andExpect(jsonPath("$.items[1].status").value(customer2.status.name))
        }

        @Test
        fun `should not return customers and return forbidden when is not admin`() {
            customerRepository.save(buildCustomer())
            customerRepository.save(buildCustomer())

            mockMvc.perform(get("/customers"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-0000"))
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should filter all customers by name when getAll`() {
            val customer1 = customerRepository.save(buildCustomer(name = "Gustavo"))
            customerRepository.save(buildCustomer(name = "Daniel"))

            mockMvc.perform(get("/customers?name=gus"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(customer1.id))
                .andExpect(jsonPath("$.items[0].name").value(customer1.name))
                .andExpect(jsonPath("$.items[0].email").value(customer1.email))
                .andExpect(jsonPath("$.items[0].status").value(customer1.status.name))
        }

        @Test
        fun `should not filter all customers and should return forbidden when is not admin`() {
            customerRepository.save(buildCustomer(name = "Gustavo"))
            customerRepository.save(buildCustomer(name = "Daniel"))

            mockMvc.perform(get("/customers?name=gus"))
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-0000"))
        }
    }

    @Nested
    inner class `create customer`() {
        @Test
        fun `should create customer`() {
            val request = PostCustomerRequest("fake name", "${Random.nextInt()}@teste.com", "123456")
            mockMvc.perform(
                post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            val customers = customerRepository.findAll().toList()
            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        fun `should throw error when create customer has invalid data`() {
            val request = PostCustomerRequest("", "${Random.nextInt()}@teste.com", "123456")
            mockMvc.perform(
                post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.httpCode").value(422))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.internalCode").value("ML-0001"))
        }
    }

    @Nested
    inner class `get user by id`() {
        @Test
        fun `should get user by id when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                get("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.name))
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should get user by id when user is admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(get("/customers/${customer.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(customer.id))
                .andExpect(jsonPath("$.name").value(customer.name))
                .andExpect(jsonPath("$.email").value(customer.email))
                .andExpect(jsonPath("$.status").value(customer.status.name))
        }

        @Test
        fun `should return forbidden when user has different id`() {
            val customer = customerRepository.save(buildCustomer())
            val otherId = Random.nextInt()

            mockMvc.perform(
                get("/customers/${otherId}")
                    .with(user(UserCustomDetails(customer)))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-0000"))
        }
    }

    @Nested
    inner class `update user`() {
        @Test
        fun `should update customer when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("Gustavo", "emailupdate@email.com")

            mockMvc.perform(
                put("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            val customers = customerRepository.findAll().toList()

            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should update customer when user is admin`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("Gustavo", "emailupdate@email.com")

            mockMvc.perform(
                put("/customers/${customer.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNoContent)

            val customers = customerRepository.findAll().toList()

            assertEquals(1, customers.size)
            assertEquals(request.name, customers[0].name)
            assertEquals(request.email, customers[0].email)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return not found when try to update customer and it doesnt exist`() {
            val request = PutCustomerRequest("Gustavo", "emailupdate@email.com")

            mockMvc.perform(
                put("/customers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Customer [1] not exists"))
                .andExpect(jsonPath("$.internalCode").value("ML-1101"))
        }

        @Test
        fun `should return forbidden when try to update customer and user dont has the same id`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("Gustavo", "emailupdate@email.com")
            val otherId = Random.nextInt()

            mockMvc.perform(
                put("/customers/${otherId}")
                    .with(user(UserCustomDetails(customer)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-0000"))
        }

        @Test
        fun `should return forbidden when try to update customer with invalid data`() {
            val customer = customerRepository.save(buildCustomer())
            val request = PutCustomerRequest("", "emailupdate@email.com")

            mockMvc.perform(
                put("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isUnprocessableEntity)
                .andExpect(jsonPath("$.httpCode").value(422))
                .andExpect(jsonPath("$.message").value("Invalid request"))
                .andExpect(jsonPath("$.internalCode").value("ML-0001"))
        }
    }

    @Nested
    inner class `delete user`() {
        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should delete customer when is admin`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(delete("/customers/${customer.id}"))
                .andExpect(status().isNoContent)

            val customerDeleted = customerRepository.findById(customer.id!!)
            assertEquals(CustomerStatus.INATIVO, customerDeleted.get().status)
        }

        @Test
        @WithMockUser(roles = ["ADMIN"])
        fun `should return not found when customer not exists`() {
            mockMvc.perform(delete("/customers/1"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Customer [1] not exists"))
                .andExpect(jsonPath("$.internalCode").value("ML-1101"))
        }

        @Test
        fun `should delete customer when user has the same id`() {
            val customer = customerRepository.save(buildCustomer())

            mockMvc.perform(
                delete("/customers/${customer.id}")
                    .with(user(UserCustomDetails(customer)))
            )
                .andExpect(status().isNoContent)

            val customerDeleted = customerRepository.findById(customer.id!!)
            assertEquals(CustomerStatus.INATIVO, customerDeleted.get().status)
        }

        @Test
        fun `should return forbidden when try to delete customer and is not an admin`() {
            val customer = customerRepository.save(buildCustomer())
            val otherId = Random.nextInt()

            mockMvc.perform(
                delete("/customers/${otherId}")
                .with(user(UserCustomDetails(customer)))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.httpCode").value(403))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.internalCode").value("ML-0000"))
        }
    }
}