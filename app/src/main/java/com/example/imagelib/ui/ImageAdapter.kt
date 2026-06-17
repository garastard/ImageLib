package com.example.imagelib.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.imagelib.R
import com.example.imagelib.databinding.ItemImageBinding
import com.example.imagelib.domain.model.ImageResource
import com.example.imageloader.ImageLoader

class ImageAdapter(
    private val imageLoader: ImageLoader
) : ListAdapter<ImageResource, ImageAdapter.ImageViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding, imageLoader)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ImageViewHolder(
        private val binding: ItemImageBinding,
        private val imageLoader: ImageLoader
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageResource) {
            binding.idText.text =
                binding.root.context.getString(R.string.image_id, item.id)

            imageLoader
                .load(item.url)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(binding.imageView)
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ImageResource>() {
            override fun areItemsTheSame(oldItem: ImageResource, newItem: ImageResource): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ImageResource, newItem: ImageResource): Boolean =
                oldItem == newItem
        }
    }
}