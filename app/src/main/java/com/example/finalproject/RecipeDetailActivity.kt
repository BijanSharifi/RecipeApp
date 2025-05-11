package com.example.finalproject

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.snapshots
import com.google.firebase.ktx.Firebase

class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var ivImage: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var overallRating: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var tvDifficulty: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvIngredients: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        val btnFeed   = findViewById<Button>(R.id.btnNavFeed)
        val btnCreate = findViewById<Button>(R.id.btnNavCreate)
        val btnLast = findViewById<Button>(R.id.btnLastViewed)
        btnLast.isEnabled = PrefsHelper.getLastRecipe(this) != null

        btnFeed.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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

        ivImage       = findViewById(R.id.ivDetailImage)
        tvTitle       = findViewById(R.id.tvDetailTitle)
        overallRating = findViewById(R.id.tvOverallRating)
        ratingBar     = findViewById(R.id.ratingBarDetail)
        tvDifficulty  = findViewById(R.id.tvDifficulty)
        tvDescription = findViewById(R.id.tvDetailDescription)
        tvIngredients = findViewById(R.id.tvDetailIngredients)

        val recipeId = intent.getStringExtra("RECIPE_ID")
        if (recipeId.isNullOrEmpty()) {
            Toast.makeText(this, "No recipe ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        PrefsHelper.saveLastRecipe(this, recipeId)

        ratingBar.setOnRatingBarChangeListener(object : RatingBar.OnRatingBarChangeListener{
            override fun onRatingChanged(ratingBar: RatingBar?, rating: Float, fromUser: Boolean) {
                val ref = Firebase.database.getReference("recipes").child(recipeId)
                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {

                        var recipe = snapshot.getValue(Recipe::class.java)
                        val newRating =
                            ((recipe!!.rating * recipe.ratingCount) + rating) / (recipe.ratingCount + 1)
                        recipe = Recipe(
                            id = recipe.id,
                            title = recipe.title,
                            description = recipe.description,
                            difficulty = recipe.difficulty,
                            imageData = recipe.imageData,
                            rating = newRating,
                            ratingCount = recipe.ratingCount + 1,
                            ingredients = recipe.ingredients
                        )
                        overallRating.text = "Overall User Rating: " + "%.2f".format(newRating)
                        ref.setValue(recipe).addOnSuccessListener {
                            Toast.makeText(
                                this@RecipeDetailActivity,"Rating updated!", Toast.LENGTH_SHORT).show()
                        }


                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
            }


        })

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
        overallRating.text = "Overall User Rating: " + "%.2f".format(recipe.rating)


        val bytes = Base64.decode(recipe.imageData, Base64.DEFAULT)
        val bmp   = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ivImage.setImageBitmap(bmp)

        tvDifficulty.text = "Difficulty Rating: " + recipe.difficulty + "/10"
        tvDescription.text = recipe.description
        tvIngredients.text = recipe.ingredients
    }
}

