package com.example.insta

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null){
            goPostsActivity()
        }

        btnLogin.setOnClickListener {
            btnLogin.isEnabled = false

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()){
                Toast.makeText(this,
                    "Email or Password Cannot be Empty",
                    Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }


            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    btnLogin.isEnabled = false

                    if (task.isSuccessful){
                        Toast.makeText(this,
                            "Success!",
                            Toast.LENGTH_SHORT).show()
                        goPostsActivity()
                    }
                    else{
                        Log.e(TAG, "signInWithEmail failed",
                                task.exception)

                        Toast.makeText(this,
                            task.exception!!.message.toString(),
                            Toast.LENGTH_SHORT).show()
                        btnLogin.isEnabled = true
                    }
                }
        }
    }
    private fun goPostsActivity(){
        Log.i(TAG, "goPostActivity")

        val intent = Intent(this, PostActivity::class.java)
        startActivity(intent)
        finish()
    }
}