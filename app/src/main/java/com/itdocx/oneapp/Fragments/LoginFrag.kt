package com.itdocx.oneapp.Fragments

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itdocx.oneapp.AdminActivity
import com.itdocx.oneapp.DashboardActivity
import com.itdocx.oneapp.Models.dataofUser
import com.itdocx.oneapp.R
import com.itdocx.oneapp.databinding.FragmentLoginBinding


class LoginFrag : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var backPressedOnce = false
    private val doubleBackPressedInterval = 2000 // Time interval in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)


        Glide.with(this).asGif().load(R.drawable.login_sticker).into(binding.ivLoginsticker)


     binding.txtSignupinLogin.setOnClickListener {

         findNavController().navigate(R.id.action_loginFrag_to_signUpFrag)
     }

        binding.btnShowPassword.setOnClickListener {

            togglePasswordVisibility(binding.etLoginPassword, binding.btnShowPassword)
        }

        binding.btnLogin.setOnClickListener {
            if (binding.etLoginUserName.text.toString().isEmpty()) {
                binding.etLoginUserName.error = "Please enter user email"
            } else if (binding.etLoginPassword.text.toString().isEmpty()) {
                binding.etLoginPassword.error = "Please enter user Password"
            } else {
                userAuthentication()
            }
        }




        return binding.root

    }


    private fun togglePasswordVisibility(editText: EditText, imageButton: ImageButton) {
        val selection = editText.selectionEnd
        if (editText.transformationMethod == PasswordTransformationMethod.getInstance()) {
            editText.transformationMethod = null // Show password
            // Change the color when showing password
            imageButton.setColorFilter(
                resources.getColor(R.color.darkBlue, null),
                PorterDuff.Mode.SRC_IN
            )
        } else {
            editText.transformationMethod =
                PasswordTransformationMethod.getInstance() // Hide password
            // Change the color when hiding password
            imageButton.setColorFilter(
                resources.getColor(android.R.color.darker_gray, null),
                PorterDuff.Mode.SRC_IN
            )
        }
        editText.setSelection(selection)
    }

    private fun userAuthentication() {
        val auth = FirebaseAuth.getInstance()
        val email = binding.etLoginUserName.text.toString()
        val password = binding.etLoginPassword.text.toString()

        // Show the progress bar
        binding.clLogin.visibility = View.GONE
        binding.progressLogin.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                // Hide the progress bar
                binding.clLogin.visibility = View.VISIBLE
                binding.progressLogin.visibility = View.GONE

                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    // Retrieve user data from the database
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference("users").child(userId!!)

                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userData = snapshot.getValue(dataofUser::class.java)
//                            if (userData != null) {
//                                // Handle conversion for numeric fields
//                                val doubleValue = userData.walletCurrentbalance.toDoubleOrNull() ?: 0.0 // Default value if conversion fails
//                                // Now you can use 'doubleValue' as a double value
//                            }
                            if (userData != null && userData.role == "admin") {
                                // Redirect to admin panel
                                val intent = Intent(activity, AdminActivity::class.java)
                                startActivity(intent)
                                activity!!.finish()
                            } else if (userData != null && userData.role == "") {
                                // Redirect to user dashboard
                                val intent = Intent(activity, DashboardActivity::class.java)
                                intent.putExtra("userKey", "")
                                startActivity(intent)
                                activity!!.finish()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // Handle login failure
                    // task.exception?.message
                    val exception = task.exception
                    if (exception != null) {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "There was an error logging in. Please try again later.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
    }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (backPressedOnce) {
                requireActivity().finish()
            } else {
                backPressedOnce = true
                Toast.makeText(requireContext(), "Press back again to exit", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    backPressedOnce = false
                }, doubleBackPressedInterval.toLong())
            }
        }
    }
}