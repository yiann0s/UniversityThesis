package com.yannis.thesis.movierecommendationapp.data.repositories

import com.yannis.thesis.movierecommendationapp.data.local.UserDao
import com.yannis.thesis.movierecommendationapp.domain.repositories.UserRepository
import com.yannis.thesis.movierecommendationapp.data.local.User

class RoomUserRepository(private val dao: UserDao) : UserRepository {
    override fun getAll() = dao.getAll()
    override fun findByEmail(email: String) = dao.findByEmail(email)
    override fun findByPassword(password: String) = dao.findByPassword(password)
    override fun findAllExcept(id: String) = dao.findAllExcept(id)
    override fun insert(user: User) = dao.insert(user)
}
