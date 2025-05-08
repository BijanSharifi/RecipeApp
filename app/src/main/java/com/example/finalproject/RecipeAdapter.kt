package com.example.finalproject

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class RecipeAdapter(
    private val onViewDetails: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Recipe>() {
            override fun areItemsTheSame(old: Recipe, new: Recipe) = old.id == new.id
            override fun areContentsTheSame(old: Recipe, new: Recipe) = old == new
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView   = itemView.findViewById(R.id.ivRecipeImage)
        private val tvTitle: TextView    = itemView.findViewById(R.id.tvTitle)
        private val tvDesc: TextView     = itemView.findViewById(R.id.tvDescription)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val btnDetails: Button   = itemView.findViewById(R.id.btnViewDetails)

        fun bind(recipe: Recipe) {
            tvTitle.text     = recipe.title
            tvDesc.text      = recipe.description
            ratingBar.rating = recipe.rating

            val bytes = Base64.decode(recipe.imageData, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.let {
                ivImage.setImageBitmap(it)
            }

            btnDetails.setOnClickListener { onViewDetails(recipe) }
        }
    }
}
