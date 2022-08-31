package com.mercadolivro.service

import com.mercadolivro.enums.BookStatus
import com.mercadolivro.enums.Errors
import com.mercadolivro.exception.NotFoundException
import com.mercadolivro.helper.buildBook
import com.mercadolivro.helper.buildCustomer
import com.mercadolivro.repository.BookRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*
import kotlin.random.Random

@ExtendWith(MockKExtension::class)
class BookServiceTest {

    @MockK
    private lateinit var bookRepository: BookRepository

    @InjectMockKs
    private lateinit var bookService: BookService

    @Test
    fun `should save book`() {
        val book = buildBook()

        every { bookRepository.save(book) } returns book
        bookService.create(book)

        verify(exactly = 1) {
            bookRepository.save(book)
        }
    }

    @Test
    fun `should find all books`() {
        val pageable = PageRequest.of(0, 10)

        val fakeBooks = PageImpl(listOf(buildBook(), buildBook()), pageable, 2)
        every { bookRepository.findAll(pageable) } returns fakeBooks

        val books = bookService.findAll(pageable)

        assertEquals(fakeBooks, books)
        assertEquals(2, books.totalElements)
        verify(exactly = 1) {
            bookRepository.findAll(pageable)
        }
    }

    @Test
    fun `should find only active books`() {
        val pageable = PageRequest.of(0, 10)

        val fakeBooks = PageImpl(
            listOf(
                buildBook(status = BookStatus.ATIVO)
            ),
            pageable,
            1
        )
        every { bookRepository.findByStatus(BookStatus.ATIVO, pageable) } returns fakeBooks

        val books = bookService.findActives(pageable)

        assertEquals(fakeBooks, books)
        assertEquals(1, books.totalElements)
        verify(exactly = 1) {
            bookRepository.findByStatus(BookStatus.ATIVO, pageable)
        }
    }

    @Test
    fun `should find book by id`() {
        val id = Random.nextInt()
        val book = buildBook(id = id)

        every { bookRepository.findById(id) } returns Optional.of(book)

        val bookResult = bookService.findById(id)

        assertEquals(book, bookResult)
        verify(exactly = 1) {
            bookRepository.findById(id)
        }
    }

    @Test
    fun `should throw if dont find book by id`() {
        val id = Random.nextInt()

        every { bookRepository.findById(id) } returns Optional.empty()

        val error = assertThrows<NotFoundException> { bookService.findById(id) }

        assertEquals("Book [${id}] not exists", error.message)
        assertEquals("ML-1001", error.errorCode)
        verify(exactly = 1) { bookRepository.findById(id) }
    }

    @Test
    fun `should delete the book`() {
        val id = Random.nextInt()
        val book = buildBook(id = id)

        every { bookRepository.findById(id) } returns Optional.of(book)
        every { bookRepository.save(book) } returns book

        bookService.delete(id)

        verify(exactly = 1) { bookRepository.save(book) }
    }

    @Test
    fun `should update the book`() {
        val book = buildBook(name = "Novo livro")

        every { bookRepository.save(book) } returns book

        bookService.update(book)

        verify(exactly = 1) { bookRepository.save(book) }
    }

    @Test
    fun `should delete the books from a customer`() {
        val customer = buildCustomer()
        val books = listOf(buildBook(), buildBook())

        every { bookRepository.findByCustomer(customer) } returns books
        every { bookRepository.saveAll(books) } returns books

        bookService.deleteByCustomer(customer)

        assertEquals(BookStatus.DELETADO, books[0].status)
        assertEquals(BookStatus.DELETADO, books[1].status)
        verify(exactly = 1) { bookRepository.findByCustomer(customer) }
        verify(exactly = 1) { bookRepository.saveAll(books) }
    }

    @Test
    fun `should return all books by a list of ids`() {
        val ids = setOf(1, 2, 3)
        val books = listOf(buildBook(), buildBook())

        every { bookRepository.findAllById(ids) } returns books

        bookService.findAllByIds(ids)

        verify(exactly = 1) { bookRepository.findAllById(ids) }
    }

    @Test
    fun `should purchase all books`() {
        val books = mutableListOf(buildBook(), buildBook())

        every { bookRepository.saveAll(books) } returns books

        bookService.purchase(books)

        assertEquals(BookStatus.VENDIDO, books[0].status)
        assertEquals(BookStatus.VENDIDO, books[1].status)
        verify(exactly = 1) { bookRepository.saveAll(books) }
    }
}