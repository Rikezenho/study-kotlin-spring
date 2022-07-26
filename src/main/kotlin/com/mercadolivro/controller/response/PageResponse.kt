package com.mercadolivro.controller.response

class PageResponse<T>(
    var items: List<T>,
    var totalPages: Long,
    var totalItems: Int,
    var currentPage: Int,
)