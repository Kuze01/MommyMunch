package com.dicoding.mommymunch.ui.fragment.detection.captured

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.dicoding.mommymunch.data.api.ApiConfig
import com.dicoding.mommymunch.data.response.ResponseAnalyze
import com.dicoding.mommymunch.databinding.FragmentCapturedImageBinding
import com.dicoding.mommymunch.ui.fragment.detection.camera.CameraActivity
import com.dicoding.mommymunch.ui.fragment.detection.result.ResultActivity
import com.dicoding.mommymunch.ui.utils.reduceFileImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CapturedImageFragment : Fragment() {

    private var _binding: FragmentCapturedImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCapturedImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uriString = arguments?.getString(ARG_URI)
        uriString?.let { uriStr ->
            val uri = Uri.parse(uriStr)
            binding.previewImageView.setImageURI(uri)

            binding.analyze.setOnClickListener {
                analyzeImage(uri)
            }

            binding.back.setOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun analyzeImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val originalUri = uri

                val file = uriToFile(uri, requireContext()).reduceFileImage()
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val apiService = ApiConfig.getApiConfig()

                val response = apiService.uploadImage(body)

                if (response.isSuccessful) {
                    Log.d(TAG, "Upload: Success")
                    val result = response.body()
                    result?.let {
                        withContext(Dispatchers.Main) {
                            // Navigasi ke ResultActivity dengan URI asli sebelum kompresi
                            startResultActivityWithOriginalUri(originalUri, it)
                        }
                    }
                } else {
                    Log.d(TAG, "Upload: Failed")
                    withContext(Dispatchers.Main) {
                        showToast("Upload failed")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun startResultActivityWithOriginalUri(originalUri: Uri, response: ResponseAnalyze) {
        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra(ResultActivity.THIS_IMAGE_URI, originalUri.toString())
            putExtra(ResultActivity.THIS_RESULT_CLASS, response.predictedClass)
            putExtra(ResultActivity.THIS_RESULT_NUTRITION, response.nutritionInfo.toString())
            putExtra(ResultActivity.THIS_RESULT_CONTENT, response.content)

            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

   private fun uriToFile(imageUri: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
        inputStream?.let {
            val outputStream = FileOutputStream(myFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.close()
            inputStream.close()
        }
        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val timeStamp = System.currentTimeMillis()
        val filesDir = context.externalCacheDir
        return File.createTempFile("image_$timeStamp", ".jpg", filesDir)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Setelah fragmen dihapus, kembalikan visibilitas viewFinder ke VISIBLE
        (activity as? CameraActivity)?.binding?.viewFinder?.visibility = View.VISIBLE
        (activity as? CameraActivity)?.binding?.constraintCameraCapture?.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_URI = "image_uri"
        private const val TAG = "image_upload_captured_fragment"

        fun newInstance(uri: String): CapturedImageFragment {
            val fragment = CapturedImageFragment()
            val args = Bundle()
            args.putString(ARG_URI, uri)
            fragment.arguments = args
            return fragment
        }
    }
}