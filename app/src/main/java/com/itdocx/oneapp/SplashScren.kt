package com.itdocx.oneapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.itdocx.oneapp.Fragments.GetStartedFrag

class SplashScren : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_scren)

        val ivSplash = findViewById<ImageView>(R.id.ivSplashgif)

        Glide.with(this).asGif().load(R.drawable.anime_splash).into(ivSplash)

        Handler().postDelayed({

            navigateToMainFragment()

        }, 3000)


    }


    private fun navigateToMainFragment() {
        // Replace this with your navigation logic
        // For example, using Navigation component:
        if (isonBoardingFinished()) {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.splashContainer, GetStartedFrag()).addToBackStack(null).commit()
        }
    }

    private fun isonBoardingFinished(): Boolean {

        val sharedPref = this.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("finished", false)


    }
}