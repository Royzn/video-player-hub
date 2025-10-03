package com.example.video_player_hub.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.video_player_hub.R
import com.example.video_player_hub.adapter.ContentAdapter
import com.example.video_player_hub.util.TokenManager
import com.example.video_player_hub.vm.ContentUiState
import com.example.video_player_hub.vm.ContentViewModel
import com.example.video_player_hub.vm.ContentViewModelFactory
import kotlinx.coroutines.launch

class ContentFragment : Fragment() {

    private val vm: ContentViewModel by viewModels {
        ContentViewModelFactory(TokenManager.getToken(requireContext()).toString())
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var emptyTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var adapter: ContentAdapter
    private lateinit var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    private lateinit var profileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.content_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.contentRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        searchEditText = view.findViewById(R.id.searchEditText)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        profileButton = view.findViewById(R.id.profileButton)

        adapter = ContentAdapter(mutableListOf()) { post ->
            val fragment = ContentDetailFragment.newInstance(post.id)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // Your container id here
                .addToBackStack(null)
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Observe uiState flow and update UI accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    when (state) {
                        is ContentUiState.Idle -> {
                            progressBar.visibility = View.GONE
                            errorTextView.visibility = View.GONE
                            emptyTextView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            swipeRefreshLayout.isRefreshing = false
                        }
                        is ContentUiState.Loading -> {
                            if (!swipeRefreshLayout.isRefreshing) {
                                progressBar.visibility = View.VISIBLE
                            }
                            errorTextView.visibility = View.GONE
                            emptyTextView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                        }
                        is ContentUiState.Success -> {
                            progressBar.visibility = View.GONE
                            errorTextView.visibility = View.GONE
                            emptyTextView.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                            adapter.setData(state.posts)
                            swipeRefreshLayout.isRefreshing = false
                            // Debug log
                            println("Loaded ${state.posts.size} posts")
                        }
                        is ContentUiState.Error -> {
                            progressBar.visibility = View.GONE
                            errorTextView.visibility = View.VISIBLE
                            errorTextView.text = state.message
                            emptyTextView.visibility = View.GONE
                            recyclerView.visibility = View.GONE
                            swipeRefreshLayout.isRefreshing = false
                            // Debug log
                            println("Error: ${state.message}")
                        }
                    }
                }
            }
        }

        // Keep EditText synced with ViewModel query to preserve on rotation
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.currentQuery.collect { query ->
                    val currentText = searchEditText.text.toString()
                    if (currentText != query) {
                        searchEditText.setText(query)
                        searchEditText.setSelection(query.length) // cursor at end
                    }
                }
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                vm.updateQuery(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        swipeRefreshLayout.setOnRefreshListener {
            vm.loadPosts()
        }

        if (savedInstanceState == null) {
            vm.loadPosts()
        }

        profileButton.setOnClickListener {
            val fragment = ProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }
}
