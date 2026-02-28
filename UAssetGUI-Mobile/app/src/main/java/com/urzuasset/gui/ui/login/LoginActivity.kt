package com.urzuasset.gui.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.urzuasset.gui.data.SessionManager
import com.urzuasset.gui.databinding.ActivityLoginBinding
import com.urzuasset.gui.network.KeyPanelApi
import com.urzuasset.gui.ui.main.MainActivity
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val keyPanelApi = KeyPanelApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        sessionManager.getSessionKey()?.let {
            goMain()
            return
        }

        binding.loginButton.setOnClickListener {
            val key = binding.keyInput.text?.toString().orEmpty().trim()
            if (key.isEmpty()) {
                binding.keyInputLayout.error = "Geçerli bir key girin"
                return@setOnClickListener
            }
            binding.keyInputLayout.error = null
            validateKey(key)
        }
    }

    private fun validateKey(key: String) {
        setLoading(true)
        thread {
            val result = keyPanelApi.validateKey(key)
            runOnUiThread {
                setLoading(false)
                result.onSuccess { payload ->
                    if (payload.valid) {
                        sessionManager.saveSessionKey(key)
                        goMain()
                    } else {
                        Snackbar.make(binding.root, "Key doğrulanamadı", Snackbar.LENGTH_LONG).show()
                    }
                }.onFailure {
                    Snackbar.make(binding.root, "Panel bağlantı hatası: ${it.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.loginButton.isEnabled = !loading
        binding.loginProgress.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
