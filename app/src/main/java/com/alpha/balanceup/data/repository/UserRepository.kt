package com.alpha.balanceup.data.repository

import com.alpha.balanceup.data.local.dao.UserDao
import com.alpha.balanceup.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUserFlow(): Flow<UserEntity?> = userDao.getUserFlow()
    
    suspend fun getUser(): UserEntity? = userDao.getUser()
}