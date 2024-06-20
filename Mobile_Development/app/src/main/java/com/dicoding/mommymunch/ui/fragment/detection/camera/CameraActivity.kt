package com.dicoding.mommymunch.ui.fragment.detection.camera

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.dicoding.mommymunch.R
import com.dicoding.mommymunch.databinding.ActivityCameraBinding
import com.dicoding.mommymunch.ui.fragment.detection.captured.CapturedImageFragment
import com.dicoding.mommymunch.ui.utils.createCustomTempFile
import com.dicoding.mommymunch.ui.utils.getImageUri

class CameraActivity : AppCompatActivity() {
    lateinit var binding: ActivityCameraBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null

    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

            startCamera()
        }

        binding.cameraButton.setOnClickListener{ startNormalCamera() }
        binding.cameraXButton.setOnClickListener { startCameraX() }
        binding.galleryButton.setOnClickListener {
            startGallery()
            binding.loadingIndicator.visibility = View.GONE
        }
        startCameraIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        //startCamera()
        startCameraIfNeeded()
    }

    private fun startCameraIfNeeded() {
        // Pastikan bahwa camera hanya diinisialisasi jika viewFinder tidak terlihat
        if (binding.viewFinder.visibility == View.VISIBLE && binding.constraintCameraCapture.visibility == View.VISIBLE) {
            startCamera()
        }
    }

    // Fungsi untuk membuka normal Camera bawaan
    private fun startNormalCamera(){
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            currentImageUri?.let { showCapturedImageFragment(it) }
        }
    }
    //---------------------------------------------------

    //Fungsi untuk membuka galery
    private fun startGallery() {
        binding.loadingIndicator.visibility = View.VISIBLE
        launcherGallery.launch("image/*")
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            binding.loadingIndicator.visibility = View.GONE
            showCapturedImageFragment(it)
        } ?: Log.d("Photo Picker", "No media selected")
    }
    //---------------------------------------------------

    //Fungsi untuk membuka cameraX
    private fun startCameraX() {
        val imageCapture = imageCapture ?: return
        val photoFile = createCustomTempFile(application)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    showCapturedImageFragment(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Gagal mengambil gambar.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "onError: ${exc.message}")
                }
            }
        )
    }
    //---------------------------------------------------

    // Fungsi menampilkan display camera untuk cameraX
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@CameraActivity,
                    "Gagal memunculkan kamera.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "startCamera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }
    //---------------------------------------------------

    //Fungsi untuk menampilkan gambar yang telah dicapture/dipilih dari gallery & ditampilkan di CapturedImageFragment
    private fun showCapturedImageFragment(uri: Uri) {
        currentImageUri = uri
        // Periksa apakah aktivitas masih aktif
        if (!isFinishing && !isDestroyed) {
            val fragment = CapturedImageFragment.newInstance(uri.toString())
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
            binding.viewFinder.visibility = View.GONE
            binding.constraintCameraCapture.visibility = View.GONE
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }
    //---------------------------------------------------

    companion object {
        private const val TAG = "CameraActivity"
    }
}
