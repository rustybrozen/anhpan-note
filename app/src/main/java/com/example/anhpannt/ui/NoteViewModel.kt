package com.example.anhpannt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.anhpannt.data.Note
import com.example.anhpannt.data.NoteRepository
import com.example.anhpannt.data.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val repository: NoteRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val notes: StateFlow<List<Note>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isOnboardingCompleted: StateFlow<Boolean?> = userPreferences.isOnboardingCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val selectedLanguage: StateFlow<String> = userPreferences.selectedLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "vi"
        )

    val isAutoSync: StateFlow<Boolean> = userPreferences.isAutoSync
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val userEmail: StateFlow<String?> = userPreferences.userEmail
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val userId: StateFlow<String?> = userPreferences.userId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val isLoggedIn: StateFlow<Boolean> = userPreferences.userEmail
        .map { !it.isNullOrBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        viewModelScope.launch {
            if (isAutoSync.value && !userId.value.isNullOrBlank()) {
                syncNotes()
            }
        }
    }




    fun saveNote(id: String?, title: String, content: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.saveNote(id = id, title = title, content = content)
            onFinished()
            if (isAutoSync.value && !userId.value.isNullOrBlank()) {
                syncNotes()
            }
        }
    }




    fun deleteNotes(ids: List<String>) {
        viewModelScope.launch {
            repository.deleteNotes(ids)
            if (isAutoSync.value && !userId.value.isNullOrBlank()) {
                syncNotes()
            }
        }
    }




    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val uid = repository.signIn(email, password)
                userPreferences.saveUserSession(uid, email.trim())
                onSuccess()
                syncNotes()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Dang nhap that bai"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }




    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val uid = repository.signUp(email, password)
                userPreferences.saveUserSession(uid, email.trim())
                onSuccess()
                syncNotes()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Dang ky that bai"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }




    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            userPreferences.clearUserSession()
        }
    }




    fun clearAuthError() {
        _authError.value = null
    }




    fun syncNotes(onRequireAuth: (() -> Unit)? = null) {
        val currentUid = userId.value
        if (currentUid.isNullOrBlank()) {
            onRequireAuth?.invoke()
            return
        }
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.syncAllNotes(currentUid)
            } catch (e: Exception) {
                // Keep local changes intact when offline or network fails
            } finally {
                _isSyncing.value = false
            }
        }
    }




    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(completed)
        }
    }




    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(languageCode)
        }
    }




    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAutoSync(enabled)
        }
    }
}




class NoteViewModelFactory(
    private val repository: NoteRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
