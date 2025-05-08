package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FeedActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

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


        rvRecipes = findViewById(R.id.rvRecipes)
        adapter = RecipeAdapter { recipe ->
            startActivity(Intent(this, RecipeDetailActivity::class.java).apply {
                putExtra("RECIPE_ID", recipe.id)
            })
        }

        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = adapter

        Firebase.database
            .getReference("recipes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull {
                        it.getValue(Recipe::class.java)
                    }
                    adapter.submitList(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@FeedActivity,
                        "Failed to load recipes: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
