// Team Contribution Percentages
// Bijan Sharifi - 33%
// Austin Manos - 33%
// Muhammad Maaz Khan - 33%

package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.LoadAdError

class MainActivity : AppCompatActivity() {

    private lateinit var rvRecipes: RecyclerView
    private lateinit var adapter: RecipeAdapter
    private var interstitial: InterstitialAd? = null

    private fun loadAd() {
        InterstitialAd.load(this, "ca-app-pub-3940256099942544/1033173712",
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
                override fun onAdFailedToLoad(err: LoadAdError) { interstitial = null }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        val bar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(bar)

        val btnFeed   = findViewById<Button>(R.id.btnNavFeed)
        val btnCreate = findViewById<Button>(R.id.btnNavCreate)
        val btnLast = findViewById<Button>(R.id.btnLastViewed)
        btnLast.isEnabled = PrefsHelper.getLastRecipe(this) != null

        loadAd()

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


        rvRecipes = findViewById(R.id.rvRecipes)
        adapter = RecipeAdapter { recipe ->
            val openDetails  = {
                startActivity(Intent(this, RecipeDetailActivity::class.java)
                    .putExtra("RECIPE_ID", recipe.id))
            }
            if (interstitial != null) {
                interstitial!!.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitial = null
                            loadAd()
                            openDetails()
                        }
                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            interstitial = null
                            loadAd()
                            openDetails()
                        }
                    }
                interstitial!!.show(this)
            } else {
                openDetails()
            }
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
                        this@MainActivity,
                        "Failed to load recipes: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_feed, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_theme) {
            val darkNow = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
            PrefsHelper.saveTheme(this, !darkNow)
            AppCompatDelegate.setDefaultNightMode(
                if (darkNow) AppCompatDelegate.MODE_NIGHT_NO else AppCompatDelegate.MODE_NIGHT_YES
            )
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
