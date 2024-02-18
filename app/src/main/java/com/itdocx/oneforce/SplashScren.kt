package com.itdocx.oneforce

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScren : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_scren)


        Handler().postDelayed({

            navigateToMainFragment()

        }, 3000)


    }


    private fun navigateToMainFragment() {
        // Replace this with your navigation logic
        // For example, using Navigation component:
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, navigate to the main screen
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // User is not logged in, navigate to the login screen
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Finish this activity to prevent the user from coming back to it when pressing back from the next screen
        finish()
    }

}