package com.iate.android.ui.base

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Locale

abstract class TakePictureFragment<B : ViewBinding, VM : ViewModel>(
    inflateBinding: (LayoutInflater, ViewGroup?, Boolean) -> B
) : BaseFragment<B, VM>(inflateBinding) {
    private var imageFilePath: String? = null

    // Launcher for requesting camera permission
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            onCameraPermissionDenied()
        }
    }

    // Launcher for taking a picture
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            onPictureTaken(File(imageFilePath!!))
        } else {
            onPictureFailed()
        }
    }

    /**
     * Call this method to start the process of taking a picture.
     */
    protected fun startTakingPicture() {
        if (isCameraPermissionGranted()) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Check if the camera permission is already granted.
     */
    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Launch the camera intent to take a picture.
     */
    private fun launchCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePictureLauncher.launch(photoURI)
        } catch (ex: IOException) {
            Log.e("TakePictureFragment", "Error occurred while creating the file", ex)
            onPictureFailed()
        } catch (ex: ActivityNotFoundException) {
            Log.e("TakePictureFragment", "No camera application found", ex)
            onPictureFailed()
        }
    }

    /**
     * Create an image file where the picture will be saved.
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name with timestamp
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().cacheDir // Save in cache directory
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg",       /* suffix */
            storageDir    /* directory */
        )

        // Save a file path for use with ACTION_VIEW intents
        imageFilePath = image.absolutePath
        return image
    }

    /**
     * Delete the image file after it has been used.
     */
    protected fun deleteImageFile() {
        imageFilePath?.let {
            val file = File(it)
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d("TakePictureFragment", "Image file deleted")
                } else {
                    Log.e("TakePictureFragment", "Failed to delete image file")
                }
            }
            imageFilePath = null
        }
    }

    /**
     * Called when the camera permission is denied.
     * Override to handle the event.
     */
    protected abstract fun onCameraPermissionDenied()

    /**
     * Called when a picture is successfully taken.
     * @param imageFile The file where the image is saved.
     */
    protected abstract fun onPictureTaken(imageFile: File)

    /**
     * Called when taking a picture failed.
     * Override to handle the event.
     */
    protected abstract fun onPictureFailed()

}