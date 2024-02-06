package com.itdocx.oneapp.Dashboard.User

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.itdocx.oneapp.Models.UserRequests
import com.itdocx.oneapp.Models.dataofUser
import com.itdocx.oneapp.databinding.FragmentUserWalletBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


class UserWalletFrag : Fragment() {

    private var imageSelected = false
    private lateinit var username: String
    private var userEmail: String? = null


    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private lateinit var imageUrl: String
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
                                    imageUrl,
                                    username,
                                    userEmail!!,
                                    reqId = requestId,
                                    balanceReq.toString(),
                                    userId
                                )
                                binding.requestProg.visibility = View.GONE
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Request Denied please upload payment proof first",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.requestProg.visibility = View.GONE
                            }

                        } else {
                            // Handle case where user data doesn't exist
                            username = "User Data Not Found"
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


        return binding.root
    }


//    private fun loadUserWalletData() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            val userWalletDataRef = userWalletRef.child(userId)
//
//            userWalletDataRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        userWallet = snapshot.getValue(WalletBalance::class.java) ?: WalletBalance()
//                        updateUI()
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    // Handle database error
//                }
//            })
//        }
//    }
//
//    private fun updateUI() {
//        // Update UI elements with userWallet data
//        binding.txtwalletBalance.text = "Balance: ${userWallet.balance}"
//        // Update transaction history UI as well if needed
//    }

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
    }

    private fun uploadImageToFirebase(callback: (String) -> Unit) {
        if (!imageSelected) {
            Toast.makeText(requireContext(), "Please Select an Image to Uplaod", Toast.LENGTH_SHORT)
                .show()
            binding.requestProg.visibility = View.GONE
        } else {
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
                    saveImageURLToDatabase(imageUrl)
                    // Once the image is uploaded and imageURL is obtained, hide progress and show success message
                    binding.requestProg.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    binding.requestProg.visibility = View.GONE
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                binding.requestProg.visibility = View.GONE
            }
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
                            binding.txtwalletUserName.text = "Hi${balanceData.name}"
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

}
