package com.itdocx.oneapp

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.itdocx.oneapp.Dashboard.User.MyInvestmentsFrag
import com.itdocx.oneapp.Dashboard.User.UserFrag
import com.itdocx.oneapp.Dashboard.User.UserProfileFrag
import com.itdocx.oneapp.Dashboard.User.UserWalletFrag

class DashboardActivity : AppCompatActivity() {

    private lateinit var setBottomAppbar: BottomAppBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)




        setBottomAppbar = findViewById(R.id.bottom_app_bar)
        (this@DashboardActivity).setSupportActionBar(setBottomAppbar)

        onbottomAppbarClick()


    }

    private fun onbottomAppbarClick() {

        // Handle menu item 1 click
        val btnBottomHome = findViewById<LinearLayout>(R.id.btnBottomHome)
        val btnBottomWallet = findViewById<LinearLayout>(R.id.btnBottomWallet)
        val btnBottomSettings = findViewById<LinearLayout>(R.id.btnBottomSettings)
        val btnBottomProfile = findViewById<LinearLayout>(R.id.btnBottomProfle)
        val fabLogout = findViewById<FloatingActionButton>(R.id.btnLogout)

        // Initialize Firebase Authentication
        val auth = FirebaseAuth.getInstance()

        btnBottomHome.setOnClickListener {
            replaceFragment(UserFrag())
            toggleColor(btnBottomHome)
        }

        btnBottomWallet.setOnClickListener {
            replaceFragment(UserWalletFrag())
            toggleColor(btnBottomWallet)
        }

        btnBottomSettings.setOnClickListener {
            replaceFragment(MyInvestmentsFrag())
            toggleColor(btnBottomSettings)
        }

        btnBottomProfile.setOnClickListener {
            replaceFragment(UserProfileFrag())
            toggleColor(btnBottomProfile)
        }

        fabLogout.setOnClickListener {

            logoutUser(auth)

        }

        toggleColor(btnBottomHome)

    }


    private fun toggleColor(selectedButton: LinearLayout) {
        val btnBottomHome = findViewById<LinearLayout>(R.id.btnBottomHome)
        val btnBottomWallet = findViewById<LinearLayout>(R.id.btnBottomWallet)
        val btnBottomSettings = findViewById<LinearLayout>(R.id.btnBottomSettings)
        val btnBottomProfile = findViewById<LinearLayout>(R.id.btnBottomProfle)

        val ivHome = findViewById<ImageView>(R.id.ivHome)
        val ivWallet = findViewById<ImageView>(R.id.ivWallet)
        val ivSettings = findViewById<ImageView>(R.id.ivSettings)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)

        val txtHome = findViewById<TextView>(R.id.txtHome)
        val txtWallet = findViewById<TextView>(R.id.txtWallet)
        val txtSettings = findViewById<TextView>(R.id.txtSettings)
        val txtProfile = findViewById<TextView>(R.id.txtProfile)

        // Reset colors for all buttons
        btnBottomHome.setBackgroundColor(getColor(R.color.white))
        btnBottomWallet.setBackgroundColor(getColor(R.color.white))
        btnBottomSettings.setBackgroundColor(getColor(R.color.white))
        btnBottomProfile.setBackgroundColor(getColor(R.color.white))

        // Reset colors for all text and icons
        ivHome.setColorFilter(ContextCompat.getColor(this, R.color.darkBlue), PorterDuff.Mode.SRC_IN)
        ivSettings.setColorFilter(ContextCompat.getColor(this, R.color.darkBlue), PorterDuff.Mode.SRC_IN)
        ivWallet.setColorFilter(ContextCompat.getColor(this, R.color.darkBlue), PorterDuff.Mode.SRC_IN)
        ivProfile.setColorFilter(ContextCompat.getColor(this, R.color.darkBlue), PorterDuff.Mode.SRC_IN)

        txtHome.setTextColor(getColor(R.color.darkBlue))
        txtWallet.setTextColor(getColor(R.color.darkBlue))
        txtSettings.setTextColor(getColor(R.color.darkBlue))
        txtProfile.setTextColor(getColor(R.color.darkBlue))

        // Apply selected color
        selectedButton.setBackgroundColor(getColor(R.color.darkBlue))

        when (selectedButton) {
            btnBottomHome -> {
                ivHome.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
                txtHome.setTextColor(getColor(R.color.white))
            }
            btnBottomWallet -> {
                ivWallet.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
                txtWallet.setTextColor(getColor(R.color.white))
            }
            btnBottomSettings -> {
                ivSettings.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
                txtSettings.setTextColor(getColor(R.color.white))
            }
            btnBottomProfile -> {
                ivProfile.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
                txtProfile.setTextColor(getColor(R.color.white))
            }
        }
    }


    private fun logoutUser(auth: FirebaseAuth) {
        auth.signOut()
        // Redirect to login activity or perform other actions as needed
        // For example:

        val intent = Intent(
            this@DashboardActivity,
            MainActivity::class.java
        )
        // Add flags to clear the activity stack
        // Add flags to clear the activity stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        // Finish current activity
        // Finish current activity
        finish()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        onBackPressedDispatcher.addCallback(this) {
            val auth = FirebaseAuth.getInstance()
            logoutUser(auth)
            finish()
        }
    }




    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.userContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}