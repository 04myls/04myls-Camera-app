package com.example.mycamera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mycamera.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val REQUEST_PREVIEW = 1
    val REQUEST_PICTURE = 2
    val REQUEST_EXTERNAL_STORAGE = 3

    lateinit var currentPhotoUri: Uri

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.preview ->
                    binding.cameraButton.text = binding.preview.text
                R.id.takePicture ->
                    binding.cameraButton.text = binding.takePicture.text
            }
        }

        binding.cameraButton.setOnClickListener {
            when (binding.radioGroup.checkedRadioButtonId) {
                R.id.preview -> preview()
                R.id.takePicture -> takePicture()
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            storagePermission()
        }
    }

    private fun storagePermission() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        REQUEST_EXTERNAL_STORAGE -> {
            binding.cameraButton.isEnabled = grantResults.isNotEmpty() &&
                    grantResults[
        when (requestCode) {0] == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    private fun preview() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                startActivityForResult(intent, REQUEST_PREVIEW)
            }
        }
    }

    private fun takePicture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val time: String = SimpleDateFormat("yyyyMMdd_HHmmss")
                    .format(Date())
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "${time}_.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }
                val collection = MediaStore.Images.Media
                    .getContentUri("external")
                val photoUri = contentResolver.insert(collection, values)
                photoUri?.let {
                    currentPhotoUri = it
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_PICTURE)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PREVIEW && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imageView.setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_PICTURE) {
            when (resultCode) {
                RESULT_OK -> {
                    Intent(Intent.ACTION_SEND).also { share ->
                        share.type = "image/*"
                        share.putExtra(Intent.EXTRA_STREAM, currentPhotoUri)
                        startActivity(Intent.createChooser(share, "Share to"))
                    }
                }
                else -> {
                    contentResolver.delete(currentPhotoUri, null, null)
                }
            }
        }
    }
}