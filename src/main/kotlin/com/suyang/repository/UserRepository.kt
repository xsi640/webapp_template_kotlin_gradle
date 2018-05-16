package com.suyang.repository

import com.suyang.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Int> {
    override fun findAll(pageable: Pageable): Page<User>
    fun countByLoginName(loginName: String): Int
}
