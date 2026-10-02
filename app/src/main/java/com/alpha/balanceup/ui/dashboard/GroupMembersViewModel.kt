package com.alpha.balanceup.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alpha.balanceup.data.local.entity.GroupMemberEntity
import com.alpha.balanceup.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupMembersViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _groupId = MutableStateFlow<Long?>(null)
    val groupId: StateFlow<Long?> = _groupId.asStateFlow()

    val members: Flow<List<GroupMemberEntity>> = _groupId.flatMapLatest { id ->
        if (id != null) repository.getMembersForGroup(id) else flowOf(emptyList())
    }

    fun setGroupId(id: Long) {
        _groupId.value = id
    }

    fun deleteGroup() {
        val id = _groupId.value ?: return
        viewModelScope.launch {
            repository.deleteGroup(id)
        }
    }
}
