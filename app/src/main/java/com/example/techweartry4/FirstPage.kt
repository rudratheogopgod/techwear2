package com.example.techweartry4

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FirstPage : AppCompatActivity() {
    lateinit var usebtn : Button
    lateinit var organizationbtn : Button
    lateinit var helpbtn : Button
    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_first_page)

        helpbtn = findViewById<Button>(R.id.helpbtn)
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
}