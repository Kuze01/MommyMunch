package com.dicoding.mommymunch.ui.ui_activity.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dicoding.mommymunch.R
import com.dicoding.mommymunch.databinding.ActivityMainBinding
import com.dicoding.mommymunch.ui.fragment.account.AccountFragment
import com.dicoding.mommymunch.ui.fragment.detection.camera.CameraActivity
import com.dicoding.mommymunch.ui.fragment.favourite.FavouriteFragment
import com.dicoding.mommymunch.ui.fragment.home.HomeFragment
import com.dicoding.mommymunch.ui.fragment.note.NoteFragment
import com.dicoding.mommymunch.ui.ui_activity.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topAppBarMain)  //activate AppBar view

        auth = Firebase.auth
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            // Not signed in, launch the Login activity
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        replaceFragment(HomeFragment())

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> replaceFragment(HomeFragment())
                //R.id.favourite -> replaceFragment(FavouriteFragment())
                //R.id.note -> replaceFragment(NoteFragment())
                R.id.account -> replaceFragment(AccountFragment())
            }
            true
        }
        binding.detection.setOnClickListener{
            val intent=Intent(this@MainActivity, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("MainActivity", "Menu item selected: ${item.itemId}")
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                Log.d("MainActivity", "Sign out menu selected")
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun signOutFromFragment() {
        signOut()
    }

    private fun signOut() {
        lifecycleScope.launch {
            Log.d("MainActivity", "Signing out")
            val credentialManager = CredentialManager.create(this@MainActivity)

            auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}