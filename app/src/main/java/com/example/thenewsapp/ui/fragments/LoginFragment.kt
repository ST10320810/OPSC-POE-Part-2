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

class LoginFragment: Fragment() {

    private val authVM: AuthViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val email = view.findViewById<EditText>(R.id.etEmail)
        val password = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        btnLogin.setOnClickListener {
            authVM.login(email.text.toString().trim(), password.text.toString())
        }

        authVM.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvStatus.text = "Logged in as ${user.email ?: user.uid}"
                Snackbar.make(view, "Welcome ${user.displayName ?: (user.email ?: "")}", Snackbar.LENGTH_SHORT).show()
            } else {
                tvStatus.text = "Not logged in"
            }
        }

        authVM.error.observe(viewLifecycleOwner) { err ->
            err?.let { Snackbar.make(view, it, Snackbar.LENGTH_LONG).show() }
        }
    }
}