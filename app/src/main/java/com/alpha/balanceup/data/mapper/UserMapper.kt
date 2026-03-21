package com.alpha.balanceup.data.mapper

import com.alpha.balanceup.core.model.User
import com.alpha.balanceup.data.local.entity.UserEntity

fun UserEntity.toDomain() = User(
    id,
    name,
    email,
    googleId,
    isGuest,
    synced
)

fun User.toEntity() = UserEntity(
    id,
    name,
    email,
    googleId,
    isGuest,
    synced
)