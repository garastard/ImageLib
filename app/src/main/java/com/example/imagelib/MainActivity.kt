package com.example.imagelib

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.imagelib.databinding.ActivityMainBinding
import com.example.imagelib.ui.ImageAdapter
import com.example.imagelib.ui.MainViewModel
import com.example.imagelib.ui.UiState
import com.example.imageloader.ImageLoader
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ImageAdapter(imageLoader)
        applyWindowInsets()
        setupRecyclerView()
        setupActions()
        observeState()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = bars.top)
            binding.root.updatePadding(bottom = bars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter
    }

    private fun setupActions() {
        binding.invalidateButton.setOnClickListener {
            viewModel.invalidateCache()
            Snackbar.make(binding.root, R.string.cache_invalidated, Snackbar.LENGTH_SHORT).show()
        }
        binding.retryButton.setOnClickListener { viewModel.loadImages() }
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadImages() }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: UiState) {
        val hasItems = adapter.itemCount > 0
        when (state) {
            is UiState.Loading -> {
                binding.errorGroup.visibility = View.GONE
                binding.progressBar.visibility = if (hasItems) View.GONE else View.VISIBLE
            }

            is UiState.Success -> {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                binding.errorGroup.visibility = View.GONE
                adapter.submitList(state.items)
            }

            is UiState.Error -> {
                binding.swipeRefresh.isRefreshing = false
                binding.progressBar.visibility = View.GONE
                if (hasItems) {
                    Snackbar.make(binding.root, R.string.error_loading_list, Snackbar.LENGTH_LONG).show()
                } else {
                    binding.errorGroup.visibility = View.VISIBLE
                }
            }
        }
    }
}