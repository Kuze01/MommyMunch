package com.dicoding.mommymunch.ui.fragment.detection.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.mommymunch.databinding.ActivityResultBinding
import com.dicoding.mommymunch.ui.ui_activity.main.MainActivity

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imageUri: Uri

    private var classPredictedResult: String? = null
    private var nutritionInfoResult: String? = null
    private var contentResult: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uriString = intent.getStringExtra(THIS_IMAGE_URI)
        if (uriString != null) {
            imageUri = Uri.parse(uriString)
            binding.ivImageClassifySource.setImageURI(imageUri)
        } else {
            Log.e("ResultActivity", "Error: Image URI is null")
            showToast("Error: Image URI is null")
        }

        getResult()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getResult() {
        classPredictedResult = intent.getStringExtra(THIS_RESULT_CLASS)
        nutritionInfoResult = intent.getStringExtra(THIS_RESULT_NUTRITION)
        contentResult = intent.getStringExtra(THIS_RESULT_CONTENT)

        binding.classPredictedText.text = classPredictedResult ?: "No data"
        binding.nutritionInfoText.text = nutritionInfoResult ?: "No data"
        binding.contentText.text = contentResult ?: "No data"
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    companion object {
        const val THIS_IMAGE_URI = "this_image_uri"
        const val THIS_RESULT_CLASS = "this_result_class"
        const val THIS_RESULT_NUTRITION = "this_result_nutrition"
        const val THIS_RESULT_CONTENT = "this_result_content"
    }
}
