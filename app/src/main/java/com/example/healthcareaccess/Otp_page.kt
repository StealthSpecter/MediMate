package com.example.healthcareaccess

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.healthcareaccess.method.AndroidUtil
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.*
import java.util.concurrent.TimeUnit

class Otp_page : AppCompatActivity() {

    private lateinit var phoneNumber: String
    private var timeoutSeconds: Long = 60 //For time to wait for OTP
    private lateinit var verificationCode: String
    private lateinit var otpInput: EditText
    private lateinit var nextBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resendOtpTextView: TextView
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken
    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_page)
        supportActionBar ?.hide()

        phoneNumber = intent.extras?.getString("phone") ?: ""
        otpInput = findViewById(R.id.login_otp)
        nextBtn = findViewById(R.id.login_next_btn)
        progressBar = findViewById(R.id.login_progress_bar)
        resendOtpTextView = findViewById(R.id.resend_otp_textview)

       //---------- for testing of database --------------------
//        val data = hashMapOf<String, String>()
//        FirebaseFirestore.getInstance().collection("test").add(data)

        sendOtp(phoneNumber, false)

        nextBtn.setOnClickListener {
            val enteredOtp = otpInput.text.toString()
            val credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp)
            signIn(credential)
        }

        resendOtpTextView.setOnClickListener {
            sendOtp(phoneNumber, true)
        }
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)
        val builder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    signIn(phoneAuthCredential)
                    setInProgress(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    AndroidUtil.showToast(applicationContext, "OTP verification failed")
                    setInProgress(false)
                }

                override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                    super.onCodeSent(s, forceResendingToken)
                    verificationCode = s
                    resendingToken = forceResendingToken
                    AndroidUtil.showToast(applicationContext, "OTP sent successfully")
                    setInProgress(false)
                }
            })

        if (isResend) {
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build())
        } else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        }
    }

    private fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            nextBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        }
    }

    private fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
                val intent = Intent(this@Otp_page, login_page::class.java)
                intent.putExtra("phone", phoneNumber)
                startActivity(intent)
            } else {
                AndroidUtil.showToast(applicationContext, "OTP verification failed")
            }
        }
    }

    private fun startResendTimer() {
        resendOtpTextView.isEnabled = false
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeoutSeconds--
                resendOtpTextView.text = "Resend OTP in $timeoutSeconds seconds"
                if (timeoutSeconds.toInt() == 0) {
                    timeoutSeconds = 60
                    timer.cancel()
                    runOnUiThread {
                        resendOtpTextView.isEnabled = true
                    }
                }
            }
        }, 0, 1000)

    }
}