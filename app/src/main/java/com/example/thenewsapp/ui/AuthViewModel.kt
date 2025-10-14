package com.example.thenewsapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.thenewsapp.repository.FirebaseAuthRepository
import com.example.thenewsapp.repository.RemoteUser
import kotlinx.coroutines.launch

class AuthViewModel(app: Application): AndroidViewModel(app) {
    private val repo = FirebaseAuthRepository()

    private val _currentUser = MutableLiveData<RemoteUser?>()
    val currentUser: LiveData<RemoteUser?> = _currentUser

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        viewModelScope.launch {
            _currentUser.postValue(repo.currentUser())
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            repo.login(email, password).onSuccess {
                _currentUser.postValue(it)
                _error.postValue(null)
            }.onFailure {
                _error.postValue(it.message ?: "Login failed")
            }
        }
    }

    fun register(email: String, password: String, displayName: String?) {
        viewModelScope.launch {
            repo.register(email, password, displayName).onSuccess {
                _currentUser.postValue(it)
                _error.postValue(null)
            }.onFailure {
                _error.postValue(it.message ?: "Registration failed")
            }
        }
    }

    fun updateProfile(displayName: String?, bio: String?, photoUrl: String?) {
        viewModelScope.launch {
            repo.updateProfile(displayName, bio, photoUrl).onSuccess {
                _currentUser.postValue(it)
                _error.postValue(null)
            }.onFailure {
                _error.postValue(it.message ?: "Update failed")
            }
        }
    }

    fun uploadPhotoAndUpdate(uri: android.net.Uri) {
        viewModelScope.launch {
            repo.uploadProfilePhoto(uri).onSuccess { url ->
                val u = _currentUser.value
                updateProfile(u?.displayName, u?.bio, url)
            }.onFailure {
                _error.postValue(it.message ?: "Upload failed")
            }
        }
    }

    fun logout() {
        repo.logout()
        _currentUser.postValue(null)
    }
}