package com.dicoding.mommymunch.ui.fragment.account

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dicoding.mommymunch.data.dataclass.User
import com.dicoding.mommymunch.databinding.FragmentAccountBinding
import com.dicoding.mommymunch.ui.ui_activity.main.MainActivity
import com.dicoding.mommymunch.ui.utils.getImageUri
import com.dicoding.mommymunch.ui.utils.reduceFileImage
import com.dicoding.mommymunch.ui.utils.uriToFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class AccountFragment : Fragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private var currentImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        user?.let {
            binding.tvUsername.text = it.displayName
            binding.tvEmail.text = it.email
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users")

        val uid = user?.uid
        uid?.let {
            showLoading(true)
            databaseReference.child(it).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userInfo = snapshot.getValue(User::class.java)
                    userInfo?.profilePicUrl?.let { url ->
                        if (isAdded) {
                            Glide.with(this@AccountFragment)
                                .load(url)
                                .circleCrop()
                                .into(binding.ivProfileAccount)
                            showLoading(false)
                        }
                    }
                } else {
                    if (isAdded) {
                        showLoading(false)
                    }
                }
            }.addOnFailureListener {
                if (isAdded) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.ivBtnSaveEdit.setOnClickListener {
            currentImageUri?.let { uri ->
                uploadProfilePic(uri)
            } ?: run {
                Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivEditAccount.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile clicked", Toast.LENGTH_SHORT).show()
            showEditProfileDialog()
        }

        binding.btnAccountLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return root
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                (activity as? MainActivity)?.signOutFromFragment()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        _binding?.let { binding ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun showCheckbox(isCheckbox: Boolean){
        _binding?.let{binding ->
            binding.ivBtnSaveEdit.visibility = if (isCheckbox) View.VISIBLE else View.GONE
        }
    }

    private fun uploadProfilePic(uri: Uri) {
        showLoading(true)

        val compressedFile = compressImage(requireContext(), uri)

        storageReference = FirebaseStorage.getInstance().getReference("users/${auth.currentUser?.uid}/profile.jpg")
        val uploadTask = storageReference.putFile(Uri.fromFile(compressedFile))

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnCompleteListener { task ->
            if (isAdded) {
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    saveUserToDatabase(downloadUri.toString())
                    Glide.with(this@AccountFragment)
                        .load(downloadUri)
                        .circleCrop()
                        .into(binding.ivProfileAccount)
                    showLoading(false)
                    showCheckbox(false)
                    Toast.makeText(requireContext(), "Profile successfully updated", Toast.LENGTH_SHORT).show()
                } else {
                    showLoading(false)
                    showCheckbox(false)
                    Toast.makeText(requireContext(), "Failed to upload profile", Toast.LENGTH_SHORT).show()
                }
            }
        }.addOnFailureListener {
            if (isAdded) {
                showLoading(false)
                Toast.makeText(requireContext(), "Failed to upload profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveUserToDatabase(profilePicUrl: String) {
        val user = auth.currentUser
        val uid = user?.uid
        val userInfo = User(user?.displayName, user?.email, profilePicUrl)

        uid?.let {
            databaseReference.child(it).setValue(userInfo).addOnCompleteListener {
                if (isAdded) {
                    showLoading(false)
                    if (it.isSuccessful) {
                        Toast.makeText(requireContext(), "User data saved", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                if (isAdded) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Failed to save user data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showEditProfileDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    startNormalCamera()
                }
                options[item] == "Choose from Gallery" -> {
                    startGallery()
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun startNormalCamera() {
        currentImageUri = getImageUri(requireContext())
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let { uri ->
                Glide.with(this@AccountFragment)
                    .load(uri)
                    .circleCrop()
                    .into(binding.ivProfileAccount)
                showCheckbox(true)
            }
        }
    }

    private fun startGallery() {
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.ivProfileAccount.setImageURI(it)
            Glide.with(this@AccountFragment)
                .load(it)
                .circleCrop()
                .into(binding.ivProfileAccount)
            showCheckbox(true)
            currentImageUri = it
        } ?: Log.d("Photo Picker", "No media selected")
    }

    private fun compressImage(context: Context, imageUri: Uri): File {
        val file = uriToFile(imageUri, context)
        return file.reduceFileImage()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}