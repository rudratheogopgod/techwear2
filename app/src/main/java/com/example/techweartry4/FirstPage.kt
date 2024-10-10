package com.example.techweartry4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class FirstPage : AppCompatActivity() {
    lateinit var usebtn : Button
    lateinit var organizationbtn : Button
    lateinit var firebaseAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_first_page)
        firebaseAuth = FirebaseAuth.getInstance()
        usebtn = findViewById<Button>(R.id.userbtn)
        organizationbtn = findViewById<Button>(R.id.organizationbtn)

        organizationbtn.setOnClickListener{
            val intent: Intent = Intent(this, OrganizationalLogin::class.java)
            startActivity(intent)
        }
        usebtn.setOnClickListener{
            val intent: Intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null)
        {
            val intent = Intent(this, User_Details::class.java)
            startActivity(intent)
        }
    }
}