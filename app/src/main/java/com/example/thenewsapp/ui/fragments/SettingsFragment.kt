package com.example.thenewsapp.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.thenewsapp.R
import com.example.thenewsapp.ui.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private val authVM: AuthViewModel by activityViewModels()
    private var pickedPhotoUri: Uri? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                pickedPhotoUri = uri
                view?.findViewById<ImageView>(R.id.ivAvatar)?.setImageURI(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Views
        val etName = view.findViewById<EditText>(R.id.etDisplayName)
        val etBio = view.findViewById<EditText>(R.id.etBio)
        val ivAvatar = view.findViewById<ImageView>(R.id.ivAvatar)
        val btnPick = view.findViewById<Button>(R.id.btnPickPhoto)
        val btnSave = view.findViewById<Button>(R.id.btnSaveProfile)
        val switchDark = view.findViewById<Switch>(R.id.switchDarkMode)

        // NEW: navigation buttons
        val btnGoLogin = view.findViewById<Button>(R.id.btnGoLogin)
        val btnGoRegister = view.findViewById<Button>(R.id.btnGoRegister)

        // Theme toggle
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val darkOn = prefs.getBoolean("dark_mode", false)
        switchDark.isChecked = darkOn
        AppCompatDelegate.setDefaultNightMode(
            if (darkOn) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        switchDark.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        btnPick.setOnClickListener { pickImage.launch("image/*") }

        // Observe auth state
        authVM.currentUser.observe(viewLifecycleOwner) { user ->
            val loggedIn = user != null
            // Show/hide nav buttons based on login
            btnGoLogin.isVisible = !loggedIn
            btnGoRegister.isVisible = !loggedIn
            btnSave.isEnabled = loggedIn
            btnPick.isEnabled = loggedIn
            etName.isEnabled = loggedIn
            etBio.isEnabled = loggedIn

            if (loggedIn) {
                // The ViewModel exposes either RemoteUser (Firebase) or User (local). Handle both safely:
                val displayName = try {
                    val m = user!!::class.java.getMethod("getDisplayName")
                    m.invoke(user) as String?
                } catch (_: Exception) { null }
                val bio = try {
                    val m = user!!::class.java.getMethod("getBio")
                    m.invoke(user) as String?
                } catch (_: Exception) { null }
                val photoUrl = try {
                    val m = user!!::class.java.getMethod("getPhotoUrl")
                    m.invoke(user) as String?
                } catch (_: Exception) { null }

                etName.setText(displayName ?: "")
                etBio.setText(bio ?: "")

                // If using Firebase RemoteUser, load via URL; if local, photoUri string is fine too.
                if (!photoUrl.isNullOrEmpty()) {
                    Glide.with(this).load(photoUrl).into(ivAvatar)
                } else {
                    // keep whatever is displayed (default app icon)
                }
            } else {
                etName.setText("")
                etBio.setText("")
                ivAvatar.setImageResource(R.mipmap.ic_launcher_round)
            }
        }

        // Save profile (works for both Firebase and local versions)
        btnSave.setOnClickListener {
            val u = authVM.currentUser.value
            if (u == null) {
                Snackbar.make(view, "Log in first", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val newName = etName.text.toString().ifBlank { null }
            val newBio = etBio.text.toString().ifBlank { null }

            val localUri = pickedPhotoUri
            if (localUri != null) {
                // Firebase path: upload then update; Local path: ViewModel handles update
                authVM.uploadPhotoAndUpdate(localUri) // no-op in local build if not implemented
            }
            authVM.updateProfile(newName, newBio, null) // Firebase: merges; Local: replaces
            Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show()
        }

        // NEW: navigate to Login / Register
        btnGoLogin.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
        btnGoRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }
    }
}