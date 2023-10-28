package com.example.healthcareaccess

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.example.healthcareaccess.method.FirebaseMethod
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot


class login_page : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var letMeInBtn: Button
    private lateinit var progressBar: ProgressBar
    private var phoneNumber: String = ""
    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)
        supportActionBar?.hide()

        usernameInput = findViewById(R.id.login_username)
        letMeInBtn = findViewById(R.id.login_let_me_in_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        phoneNumber = intent.extras?.getString("phone") ?: ""
        getUsername()

        letMeInBtn.setOnClickListener {
            setUsername()
        }
    }

    private fun setUsername() {
        val username = usernameInput.text.toString()
        if (username.isEmpty() || username.length < 3) {
            usernameInput.error = "Username length should be at least 3 chars"
            return
        }
        setInProgress(true)
        if (userModel != null) {
            userModel?.username = username
        } else {
            userModel = FirebaseMethod.currentUserId()
                ?.let { UserModel(phoneNumber, username, Timestamp.now(), it) }
        }

        FirebaseMethod.currentUserDetails().set(userModel!!)
            .addOnCompleteListener { task: Task<Void?> ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val intent = Intent(this@login_page, MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
    }
    private fun getUsername() {
        setInProgress(true)
        FirebaseMethod.currentUserDetails().get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
            setInProgress(false)
            if (task.isSuccessful) {
                userModel = task.result?.toObject(UserModel::class.java)
                userModel?.let {
                    usernameInput.setText(it.username)
                }

            }
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            letMeInBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            letMeInBtn.visibility = View.VISIBLE
        }
    }
}
