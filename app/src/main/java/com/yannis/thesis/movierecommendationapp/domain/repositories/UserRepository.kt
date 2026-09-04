package com.yannis.thesis.movierecommendationapp.domain.repositories

import com.yannis.thesis.movierecommendationapp.data.local.User

interface UserRepository {
    fun getAll(): List<User>
    fun findByEmail(email: String): User?
    fun findByPassword(password: String): User?
    fun findAllExcept(id: String): List<User>
    fun insert(user: User)
}
