package com.itdocx.oneforce.Fragments

import android.app.AlertDialog
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.google.firebase.database.FirebaseDatabase
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentSignUpBinding


class SignUpFrag : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var signUpAuth: FirebaseAuth
    private lateinit var dataBaseSignup: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        //instance of database
        dataBaseSignup = FirebaseDatabase.getInstance()
        signUpAuth = FirebaseAuth.getInstance()

        Glide.with(this).asGif().load(R.drawable.login_sticker).into(binding.ivSignUpsticker)


        binding.txtlogininSignup.setOnClickListener {

            findNavController().navigate(R.id.action_signUpFrag_to_loginFrag)
        }

        binding.btnSignup.setOnClickListener {

            registerAuth()
        }
        binding.checkBoxSignUp.setOnClickListener {

            if (binding.checkBoxSignUp.isChecked) {
                dialogueforCorrectData()
            }

        }

        binding.btnconfirmshowPassword.setOnClickListener {
            togglePasswordVisibility(binding.etConfirmPassword, binding.btnconfirmshowPassword)
        }
        binding.btnsignupShowPassword.setOnClickListener {
            togglePasswordVisibility(binding.etSignupPassword, binding.btnsignupShowPassword)
        }



        return binding.root
    }

    private fun registerAuth() {

        val name = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val phonenumber = binding.etPhoneNumber.text.toString()
        val cnic = binding.etCNIC.text.toString()
        val useraddress = binding.etAddress.text.toString()
        val password = binding.etSignupPassword.text.toString()
        val cpass = binding.etConfirmPassword.text.toString()

        if (name.isEmpty() || email.isEmpty() || phonenumber.isEmpty() || useraddress.isEmpty() || password.isEmpty() || cpass.isEmpty()) {

            Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_SHORT)
                .show()

        } else if (!isValidPassword(password)) {
            Toast.makeText(
                requireContext(),
                "Password must contain at-least 6 characters and contain capital letter,small letter, number and special character",
                Toast.LENGTH_LONG
            ).show()
        } else if (cpass != password) {
            binding.etConfirmPassword.error = "Password doesn't match"
        } else if (!isValidPhoneNumber(phonenumber)) {
            binding.etPhoneNumber.error = "Invalid phone number"
        } else if (!isValidEmail(email)) {
            binding.etEmail.error = "Invalid email address"
        } else if (!binding.checkBoxSignUp.isChecked) {
            Toast.makeText(
                requireContext(),
                "Please accept our app privacy policy",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Check if the email is already registered

        else {
            createAccount(name, email, phonenumber,cnic , useraddress, cpass)
        }

    }


    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Implement your phone number validation logic here
        // For simplicity, let's assume a valid phone number is numeric and has a specific length

        // Adjust the regex according to your requirements
        return phoneNumber.matches(Regex("\\+\\d{1,3}\\d{9,}")) || phoneNumber.matches(Regex("\\d{11}"))
    }

    private fun isValidEmail(email: String): Boolean {
        // Implement email validation using a regex
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Implement password validation based on your security requirements
        // For example, requiring at least 8 characters, containing both letters and numbers
        val passwordPattern = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}\$")
        return password.matches(passwordPattern)
    }

    private fun dialogueforCorrectData() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Important Notice")
            .setMessage("Please Provide the correct information because this information will be used to send you the rewards in case you won any reward")
            .setPositiveButton("Confirm") { dialog, _ ->
                // Handle positive button click if needed
                // For example, you can perform some action or dismiss the dialog
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Handle negative button click if needed
                // For example, you can perform some action or dismiss the dialog
                dialog.dismiss()
                binding.checkBoxSignUp.isChecked = false
            }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    private fun createAccount(
        name: String,
        email: String,
        phoneNumber: String,
        cnic:String,
        address: String,
        password: String
    ) {
        // Show ProgressBar when starting user registration
        binding.signUpProgress.visibility = View.VISIBLE

        signUpAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Hide ProgressBar when the registration is complete
                binding.signUpProgress.visibility = View.GONE

                if (task.isSuccessful) {
                    // Registration successful, save user data to the database
                    val userId = signUpAuth.currentUser!!.uid
                    val dataBaseRef = dataBaseSignup.reference.child("users").child(userId)

                    val user = dataofUser(name, email, phoneNumber, cnic,address, "0.00",userId, "")

                    dataBaseRef.setValue(user)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                // User data saved successfully, navigate to the login fragment
                                findNavController().navigate(R.id.action_signUpFrag_to_loginFrag)
                            } else {
                                // Error while saving user data
                                showErrorToast()
                            }
                        }
                } else {
                    // Error in creating user account
                    showErrorToast()
                }
            }
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


    // Your other utility methods remain the same

    private fun showErrorToast() {
        Toast.makeText(
            requireContext(),
            "Something went wrong. Please try again later.",
            Toast.LENGTH_SHORT
        ).show()
    }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
        findNavController().popBackStack()
        }
        }
    }
