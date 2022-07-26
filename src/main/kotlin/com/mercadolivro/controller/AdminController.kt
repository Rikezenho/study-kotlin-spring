package com.mercadolivro.controller

import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("admin")
class AdminController {
    @GetMapping("/report")
    fun get(): String {
        return "This is a report"
    }
}