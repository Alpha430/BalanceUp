package com.alpha.balanceup.core.model

data class User( val id: String,
                 val name: String,
                 val email: String?,
                 val googleId: String?,
                 val isGuest: Boolean,
                 val synced: Boolean)
