package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.thenewsapp.R
import com.example.thenewsapp.ui.AuthViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterFragment: Fragment() {

    private val authVM: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val email = view.findViewById<EditText>(R.id.etEmail)
        val password = view.findViewById<EditText>(R.id.etPassword)
        val displayName = view.findViewById<EditText>(R.id.etDisplayName)
        val btnCreate = view.findViewById<Button>(R.id.btnCreateAccount)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        btnCreate.setOnClickListener {
            authVM.register(email.text.toString().trim(), password.text.toString(), displayName.text.toString().ifBlank { null })
        }

        authVM.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvStatus.text = "Registered ${'$'}{user.email ?: user.uid}"
                Snackbar.make(view, "Account created", Snackbar.LENGTH_SHORT).show()
            }
        }

        authVM.error.observe(viewLifecycleOwner) { err ->
            err?.let { Snackbar.make(view, it, Snackbar.LENGTH_LONG).show() }
        }
    }
}