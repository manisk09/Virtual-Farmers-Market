package edu.newhaven.virtualfarmersmarket

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password2.*

private lateinit var reset : TextView
private lateinit var auth: FirebaseAuth
private lateinit var username: EditText

class ForgotPassword : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_forgot_password2)

    auth = FirebaseAuth.getInstance()
    reset = findViewById(R.id.b_reset)
    username = findViewById(R.id.et_email)

    b_reset.setOnClickListener{
      forgotPassword(username)
      }
    }
  
  private fun forgotPassword(username: EditText) {
    if (username.text.toString().isEmpty()) {
      Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_LONG).show()
    } else {
      auth.sendPasswordResetEmail(username.text.toString())
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            Toast.makeText(this, "Email sent", Toast.LENGTH_LONG).show()
          }else{
            Toast.makeText(this, "Error in sending password reset", Toast.LENGTH_LONG).show()
          }

          finish()
        }
    }
  }
}
