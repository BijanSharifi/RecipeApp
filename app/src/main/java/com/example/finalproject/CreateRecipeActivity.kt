package com.example.finalproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.finalproject.Recipe
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*

class CreateRecipeActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etIngredients: EditText
    private lateinit var btnTakePhoto: Button
    private lateinit var btnSubmit: Button
    private lateinit var sbDifficulty : SeekBar
    private lateinit var tvDifficulty : TextView

    private var lastBitmap: Bitmap? = null
    private val CAMERA_REQ_CODE = 100
    private val CAMERA_PERMISSION_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        val btnFeed   = findViewById<Button>(R.id.btnNavFeed)
        val btnCreate = findViewById<Button>(R.id.btnNavCreate)
        val btnLast = findViewById<Button>(R.id.btnLastViewed)
        btnLast.isEnabled = PrefsHelper.getLastRecipe(this) != null

        btnFeed.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
            finish()
        }
        btnCreate.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            finish()
        }
        btnLast.setOnClickListener {
            PrefsHelper.getLastRecipe(this)?.let {
                val intent = Intent(this, RecipeDetailActivity::class.java)
                intent.putExtra("RECIPE_ID", it)
                startActivity(intent)
            } ?: Toast.makeText(this, "No last viewed recipe found", Toast.LENGTH_SHORT).show()
        }

        sbDifficulty = findViewById(R.id.sbDifficulty)
        tvDifficulty = findViewById(R.id.tvDifficulty)

        sbDifficulty.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvDifficulty.setText("Difficulty Rating " + progress + "/10")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        ivPreview     = findViewById(R.id.ivPreview)
        etTitle       = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etIngredients = findViewById(R.id.etIngredients)
        btnTakePhoto  = findViewById(R.id.btnTakePhoto)
        btnSubmit     = findViewById(R.id.btnSubmit)

        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            } else {
                launchCamera()
            }
        }
        btnSubmit.setOnClickListener { uploadForm() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this,
                    "Camera permission is required to take photos",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
       startActivityForResult(intent, CAMERA_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQ_CODE && resultCode == Activity.RESULT_OK) {
            val bmp = data?.extras?.get("data") as? Bitmap
            bmp?.let {
                lastBitmap = it
                ivPreview.setImageBitmap(it)
            }
        }
    }

    private fun uploadForm() {
        val title       = etTitle.text.toString().trim()
        val desc        = etDescription.text.toString().trim()
        val ingredients = etIngredients.text.toString().trim()
        val difficulty = sbDifficulty.progress
        val bmp         = lastBitmap

        if (title.isEmpty() || desc.isEmpty() || ingredients.isEmpty() || bmp == null) {
            Toast.makeText(this, "Fill all fields & take a photo", Toast.LENGTH_SHORT).show()
            return
        }

        val baos = ByteArrayOutputStream().apply {
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
        }
        val imageData = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

        val id = UUID.randomUUID().toString()
        val recipe = Recipe(
            id          = id,
            title       = title,
            description = desc,
            difficulty = difficulty,
            imageData   = imageData,
            rating      = 0f,
            ratingCount = 0,
            ingredients = ingredients
        )

        val ref = Firebase.database
            .getReference("recipes")
            .child(id)

        ref.setValue(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, FeedActivity::class.java))
                finish()
            }

    }

}


