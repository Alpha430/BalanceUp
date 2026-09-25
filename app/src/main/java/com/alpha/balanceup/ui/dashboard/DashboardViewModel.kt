package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import com.alpha.balanceup.data.local.entity.GroupEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val allGroups: Flow<List<GroupEntity>> = repository.getAllGroups()
}
