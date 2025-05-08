package com.example.finalproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var ivImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvDescription: TextView
    private lateinit var tvIngredients: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val btnFeed   = findViewById<Button>(R.id.btnNavFeed)
        val btnCreate = findViewById<Button>(R.id.btnNavCreate)

        btnFeed.setOnClickListener {
            startActivity(Intent(this, FeedActivity::class.java))
            finish()
        }
        btnCreate.setOnClickListener {
            startActivity(Intent(this, CreateRecipeActivity::class.java))
            finish()
        }

        ivImage       = findViewById(R.id.ivDetailImage)
        tvTitle       = findViewById(R.id.tvDetailTitle)
        ratingBar     = findViewById(R.id.ratingBarDetail)
        tvDescription = findViewById(R.id.tvDetailDescription)
        tvIngredients = findViewById(R.id.tvDetailIngredients)

        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "No recipe ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Firebase.database
            .getReference("recipes")
            .child(recipeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recipe = snapshot.getValue(Recipe::class.java)
                    if (recipe == null) {
                        Toast.makeText(
                            this@RecipeDetailActivity,
                            "Recipe not found",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                        return
                    }
                    bindRecipe(recipe)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@RecipeDetailActivity,
                        "Load failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            })
    }

    private fun bindRecipe(recipe: Recipe) {
        tvTitle.text       = recipe.title
        ratingBar.rating   = recipe.rating


        val bytes = Base64.decode(recipe.imageData, Base64.DEFAULT)
        val bmp   = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ivImage.setImageBitmap(bmp)

        tvDescription.text = recipe.description
        tvIngredients.text = recipe.ingredients
    }
}

