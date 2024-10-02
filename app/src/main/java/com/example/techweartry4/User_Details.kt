package com.example.techweartry4

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.techweartry4.databinding.ActivityUserDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class User_Details : AppCompatActivity() {

    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        val uId = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("User")
        storageReference = FirebaseStorage.getInstance().getReference("Images")
        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            binding.imgAdd.setImageURI(it)
            if (it != null) {
                uri = it
            }
        }

        if (uId != null) {
            databaseReference.child(uId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(User::class.java)
                        if (user != null && user.firstName != null && user.imgUri != null) {
                            val intent = Intent(this@User_Details, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@User_Details, "Failed to read user data", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.btnPickImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.saveBtn.setOnClickListener{

            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val bio = binding.etBio.text.toString()

            val user = User(firstName,lastName,bio)
            if (uId != null){

                databaseReference.child(uId).setValue(user).addOnCompleteListener{

                    if (it.isSuccessful){

                        uploadProfilePic()

                    }else{

                        Toast.makeText(this@User_Details , "Failed", Toast.LENGTH_SHORT).show()

                    }

                }

            }

        }







    }

    private fun uploadProfilePic() {
        if (uri != null) {
            val fileRef = storageReference.child("profile_pics/${auth.currentUser?.uid}.jpg")
            fileRef.putFile(uri!!)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        databaseReference.child(auth.currentUser?.uid!!).child("imgUri").setValue(downloadUri.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "Failed to save profile picture URL", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }
    }
