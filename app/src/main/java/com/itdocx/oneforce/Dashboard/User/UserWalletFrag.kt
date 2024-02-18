package com.itdocx.oneforce.Dashboard.User

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.itdocx.oneforce.Models.UserRequests
import com.itdocx.oneforce.Models.dataofUser
import com.itdocx.oneforce.Models.withdrawReq
import com.itdocx.oneforce.R
import com.itdocx.oneforce.databinding.FragmentUserWalletBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


class UserWalletFrag : Fragment() {
    private var requestSent = false
    private var imageSelected = false
    private var username: String? = null
    private var userEmail: String? = null


    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private var imageUrl: String? = null
    }


    private var _binding: FragmentUserWalletBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUserWalletBinding.inflate(inflater, container, false)

        // Load user wallet data from Firebase
        //loadUserWalletData()
        //User Credentials
        val userid: String = FirebaseAuth.getInstance().currentUser!!.uid
        fetchUpdatedUserBalanceData(userid)
        //Display Date Func
        onDisplayDate()

        //Btn Toggle
        onToggleButtons(binding.btnDepositFunds)


        val withdrawEmailshow = FirebaseAuth.getInstance().currentUser!!.email

        binding.txtWithdrawEmail.text = withdrawEmailshow.toString()



        binding.btnCopyTrc.setOnClickListener {

            onCopyButtonClick(binding.txtTrcId.text.toString())

        }
        binding.btnShareTrc.setOnClickListener {
            shareTrc(binding.txtTrcId.text.toString())
        }
        binding.ivUploadProof.setOnClickListener {
            selectImage()
        }

        binding.btnUploadProof.setOnClickListener {
            // Call uploadImageToFirebase to upload the image and obtain its URL
            binding.requestProg.visibility = View.VISIBLE

            // Call uploadImageToFirebase to upload the image and obtain its URL
            uploadImageToFirebase { imageURL ->
                // Once the image is uploaded, imageURL is available
                // You can perform any actions with the imageURL here, if needed
                imageUrl = imageURL
                // Hide progress bar once upload is completed
                binding.requestProg.visibility = View.GONE
                // Once the image is uploaded, imageURL is available
                // You can perform any actions with the imageURL here, if needed
            }
        }

        binding.btnReuqestRelease.setOnClickListener {
            binding.requestProg.visibility = View.VISIBLE

            // Replace with actual user name
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val userId = currentUser.uid
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("users").child(userId)

                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Check if the user data exists
                        if (dataSnapshot.exists()) {
                            val user = dataSnapshot.getValue(dataofUser::class.java)
                            // Do something with the user object
                            username = user!!.name ?: "No Name Available"
                            userEmail = user.email.toString()

                            val requestId = "${userId}_${UUID.randomUUID()}"
                            val currentBalanceforReq = binding.txtwalletBalance.text.toString()
                            val balanceReq =
                                currentBalanceforReq.replace(Regex("[^0-9.]"), "").toDoubleOrNull()

                            if (imageUrl != null) {
                                // imageURL is already available, use it to send the request
                                uploadDepositRequest(
                                    imageUrl!!,
                                    username!!,
                                    userEmail!!,
                                    reqId = requestId,
                                    balanceReq.toString(),
                                    userId
                                )
                                binding.requestProg.visibility = View.GONE
                                binding.btnReuqestRelease.isEnabled = false

                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Request Denied please upload payment proof first",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.requestProg.visibility = View.GONE
                                enableRequestButton()
                            }

                        } else {
                            // Handle case where user data doesn't exist
                            username = "User Data Not Found"
                            enableRequestButton()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                        println("Error: ${databaseError.message}")
                    }
                }

                // Attach the listener to the database reference
                myRef.addListenerForSingleValueEvent(valueEventListener)
            }
        }


        binding.btnwithdawFunds.setOnClickListener {

            onToggleButtons(binding.btnwithdawFunds)
        }
        binding.btnDepositFunds.setOnClickListener {
            onToggleButtons(binding.btnDepositFunds)

        }

        binding.checkWithdraw.setOnClickListener {
            if (binding.checkWithdraw.isChecked) {
                onWithdrawDialogueShow()
            }
        }



        binding.btnSubmitWithdraw.setOnClickListener {
            val etWamount = binding.etWithdrawamount.text.toString().replace(Regex("[^0-9.]"), "")
                .toDoubleOrNull() ?: 0.0
            val balancetoCompare =
                binding.txtwalletBalance.text.toString().replace(Regex("[^0-9.]"), "")
                    .toDoubleOrNull() ?: 0.0
            val emailRef = FirebaseAuth.getInstance().currentUser!!.email

            val accName = binding.etWithdrawName.text.toString()
            val accIban = binding.etWithdrawAccNo.text.toString()
            val accBank = binding.etWithdrawBank.text.toString()

            if (accName.isNotEmpty() && accIban.isNotEmpty() && accBank.isNotEmpty()) {

                if (binding.checkWithdraw.isChecked && accIban.length >= 14 && etWamount >= 10 && etWamount <= balancetoCompare) {


                    onWithdrawBtnClick(
                        accName,
                        emailRef.toString(),
                        accIban,
                        accBank,
                        etWamount.toString(),
                    )

                    //onWithdrawDialogueShow()
                }
            } else if (accName.isNullOrBlank()) {
                binding.etWithdrawName.setError("Account Title Required")
            } else if (accIban.isNullOrBlank()) {

                binding.etWithdrawAccNo.setError("Must provide account number")
            } else if (accBank.isNullOrBlank()) {

                binding.etWithdrawBank.setError("Must provide Bank Name")
            }
            if (binding.checkWithdraw.isChecked == false) {

                Toast.makeText(
                    requireContext(),
                    "Please confirm the details to continue",
                    Toast.LENGTH_SHORT
                ).show()
            }
            if (accIban.length < 14) {

                binding.etWithdrawAccNo.error = "Account number must contain at least 14 digits"
            } else {
                Toast.makeText(
                    requireContext(),
                    "Insufficient balance to make a Transaction",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        return binding.root
    }

    private fun onToggleButtons(btnColor: Button) {


        // Reset colors for all buttons
        binding.btnDepositFunds.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_toggle))
        binding.btnwithdawFunds.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn_toggle))


        // Reset colors for all text and icons


        binding.btnDepositFunds.setTextColor(Color.BLACK)
        binding.btnwithdawFunds.setTextColor(Color.BLACK)


        // Apply selected color
        btnColor.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_btn))
        btnColor.setTextColor(resources.getColor(R.color.white))

        when (btnColor) {
            binding.btnDepositFunds -> {
                binding.llWithdrawCredentials.visibility = View.GONE
                binding.lletCustomAmount.visibility = View.VISIBLE


            }

            binding.btnwithdawFunds -> {
                binding.llWithdrawCredentials.visibility = View.VISIBLE
                binding.lletCustomAmount.visibility = View.GONE
            }
        }
    }

    private fun onWithdrawDialogueShow() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Withdraw Success")
        builder.setMessage("Please note that your withdraw will be added to your account in 4-5 Working days")
        builder.setPositiveButton("Confirm") { dialog, _ ->
            // You can handle the positive button click here
            binding.checkWithdraw.isChecked = true
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialoge, _ ->
            binding.checkWithdraw.isChecked = false
            dialoge.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }


    private fun onCopyButtonClick(text: String) {


        val clipboardManager =
            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)

        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareTrc(text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(Intent.createChooser(intent, "Share via"))
    }


    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
        // Set the flag to indicate that an image selection process has started
        imageSelected = true

        binding.btnReuqestRelease.isEnabled = true
    }

    private fun uploadImageToFirebase(callback: (String) -> Unit) {
        if (!imageSelected) {
            Toast.makeText(requireContext(), "Please Select an Image to Uplaod", Toast.LENGTH_SHORT)
                .show()
            binding.requestProg.visibility = View.GONE
            return
        } else {
            binding.btnUploadProof.isEnabled = false
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")

            // Get the drawable from the image view
            val drawable = binding.ivUploadProof.drawable

            // Convert the drawable to a bitmap
            val bitmap = drawable.toBitmap()

            // Convert the bitmap to a byte array
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    imageUrl = uri.toString()
                    saveImageURLToDatabase(imageUrl!!)
                    // Once the image is uploaded and imageURL is obtained, hide progress and show success message
                    binding.requestProg.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    binding.btnUploadProof.isEnabled = false

                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    binding.requestProg.visibility = View.GONE
                    binding.btnUploadProof.isEnabled = true

                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                binding.requestProg.visibility = View.GONE
                binding.btnUploadProof.isEnabled = true
            }
        }
    }

    private fun enableRequestButton() {
        if (!requestSent && imageSelected) {
            // Enable the request button only if request hasn't been sent and image is selected
            binding.btnReuqestRelease.isEnabled = true
        }
    }

    private fun saveImageURLToDatabase(imageURL: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("Deposit Request")
        val key = databaseRef.push().key
        if (key != null) {
            databaseRef.child(key).setValue(imageURL)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data!!
            binding.ivUploadProof.setImageURI(imageUri)
            enableRequestButton()
        }
    }


    private fun onDisplayDate() {

        val currentDate = Date()

        // Define the format
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")

        // Format the date and set it to the TextView
        val formattedDate = dateFormat.format(currentDate)

        binding.txtdateofLastTransaction.text = formattedDate.toString()


    }


    private fun uploadDepositRequest(
        imageURL: String,
        name: String,
        email: String,
        reqId: String,
        currentBalance: String,
        userId: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val depositRequestsRef = FirebaseDatabase.getInstance().getReference("DepositRequests")

            // Generate a unique request ID using user ID and a random UUID

            // Create a deposit request object
            val depositRequest = UserRequests(
                imageURL,
                userName = name,
                email = email,
                requestId = reqId,
                currenBalance = currentBalance,
                uid = userId,

                )

            // Upload the deposit request to the database
            depositRequestsRef.child(reqId).setValue(depositRequest)
                .addOnSuccessListener {
                    // Request uploaded successfully
                    // You can perform any additional actions here if needed
                    Toast.makeText(
                        requireContext(),
                        "Deposit request uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    // Failed to upload request
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload deposit request: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun fetchUpdatedUserBalanceData(userId: String) {
        val balanceRef = FirebaseDatabase.getInstance().getReference("users")
        val query = balanceRef.orderByChild("uid").equalTo(userId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val balanceData = snapshot.getValue(dataofUser::class.java)
                        if (balanceData != null) {
                            // Handle the retrieved balance data here
                            // You can update your UI or perform any other actions
                            // Example: Log the retrieved balance data
                            binding.txtwalletUserName.text = "Hi ${balanceData.name}"
                            val balanceTobeDisplay = "$${balanceData.walletCurrentbalance}"
                            binding.txtwalletBalance.text = balanceTobeDisplay


                            Log.d(
                                "BalanceData",
                                "Name: ${balanceData.name}, Email: ${balanceData.email}, Balance: ${balanceData.walletCurrentbalance}"
                            )
                        }
                    }
                } else {
                    // Handle case where balance data for the user doesn't exist
                    Log.d("BalanceData", "Balance data for the user does not exist")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                Log.e("BalanceData", "Database error: ${databaseError.message}")
            }
        })
    }


    private fun uploadWithdrawRequest(
        Wname: String,
        Wemail: String,
        WreqId: String,
        WcurrentBalance: String,
        withdrawAmount: String,
        withdrawAccount: String,
        withdrawBank: String,
        userId: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val depositRequestsRef =
                FirebaseDatabase.getInstance().getReference("Withdraw-Requests")

            // Generate a unique request ID using user ID and a random UUID

            // Create a deposit request object
            val Wreq = withdrawReq(

                Wname,
                Wemail,
                WreqId,
                WcurrentBalance,
                withdrawAmount,
                withdrawAccount,
                withdrawBank,
                "Pending",
                userId,

                )

            // Upload the deposit request to the database
            depositRequestsRef.child(WreqId).setValue(Wreq)
                .addOnSuccessListener {
                    // Request uploaded successfully
                    // You can perform any additional actions here if needed
                    Toast.makeText(
                        requireContext(),
                        "Withdraw request sent successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    // Failed to upload request
                    Toast.makeText(
                        requireContext(),
                        "Failed to upload deposit request: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun onWithdrawBtnClick(
        name: String,
        email: String,
        accNumber: String,
        bank: String,
        amount: String
    ) {

        binding.requestProg.visibility = View.VISIBLE
        // Replace with actual user name
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users").child(userId)

            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Check if the user data exists
                    if (dataSnapshot.exists()) {
                        val user = dataSnapshot.getValue(withdrawReq::class.java)
                        // Do something with the user object
                        val withdraw = binding.etWithdrawamount.text.toString()
                        val balanceWithdraw = binding.txtwalletBalance.text.toString()
                        val withdrawId = "${userId}_${UUID.randomUUID()}"

                        // Deduct investment amount from current balance
                        val balance =
                            balanceWithdraw.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.00
                        val withdrawAmount =
                            withdraw.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.00
                        val newBalance = balance - withdrawAmount

                        if (newBalance >= 0) {
                            // Update balance in the database
                            val updatedBalance = newBalance.toString()

                            myRef.child("walletCurrentbalance").setValue(updatedBalance)


                            uploadWithdrawRequest(

                                name,
                                email,
                                withdrawId,
                                balance.toString(),
                                amount,
                                accNumber,
                                bank,
                                userId
                            )

                            // Once the image is uploaded, imageURL is available, use it to send the request
//                            Toast.makeText(
//                                requireContext(),
//                                "Investment created successfully!",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "Insufficient balance to make this request!",
//                                Toast.LENGTH_SHORT
//                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong please try again later!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    binding.requestProg.visibility = View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    binding.requestProg.visibility = View.GONE
                }
            }

            // Attach the listener to the database reference
            myRef.addListenerForSingleValueEvent(valueEventListener)
        }
    }

    override fun onResume() {
        super.onResume()
        enableRequestButton()
    }
}
