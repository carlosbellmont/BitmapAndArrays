package com.cbellmont.cameraexample

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.cbellmont.cameraexample.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.bFileExplorer.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            resultFileExplorer.launch(intent)
        }

        binding.bCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            resultCamera.launch(intent)
        }
    }

    private val resultFileExplorer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.ivPicture.setImageURI(result.data?.data)
            result.data?.data?.let { uri ->
                val bitmap = transformUriToBitmap(uri)
                bitmap?.let { bitmap ->
                    saveToInternalStorage(bitmap)
                }
            }
        }
    }

    private val resultCamera  = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as Bitmap
            binding.ivPicture.setImageBitmap(bitmap)
            saveToInternalStorage(bitmap)
        }
    }

    private fun transformUriToBitmap(uri : Uri): Bitmap? {
        contentResolver?.let { contentResolver ->
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            }
        }
        return null
    }

    private fun saveToInternalStorage(bitmap: Bitmap) {
        val directory = getDir("imageDir", Context.MODE_PRIVATE)
        val file = File(directory, "image.jpg")


        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            Log.d(MainActivity::class.java.name, file.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.let {
                    fos.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}