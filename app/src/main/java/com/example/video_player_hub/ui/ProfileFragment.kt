package com.example.video_player_hub.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.video_player_hub.MainActivity
import com.example.video_player_hub.R
import com.example.video_player_hub.adapter.WatchListAdapter
import com.example.video_player_hub.room.AppDatabase
import com.example.video_player_hub.room.WatchListDao
import com.example.video_player_hub.util.TokenManager
import com.example.video_player_hub.vm.ProfileUiState
import com.example.video_player_hub.vm.ProfileViewModel
import com.example.video_player_hub.vm.ProfileViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: Button
    private lateinit var watchlistRecyclerView: RecyclerView
    private lateinit var watchListDao: WatchListDao
    private lateinit var noWatchlistMessage: TextView

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(token, watchListDao)
    }

    private lateinit var token: String
    private lateinit var watchlistAdapter: WatchListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = TokenManager.getToken(requireContext()).orEmpty()
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_LONG).show()
            requireActivity().finish()
        }
        watchListDao = AppDatabase.get(requireContext()).watchListDao()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.profile_activity, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tvName = view.findViewById(R.id.textName)
        tvEmail = view.findViewById(R.id.textEmail)
        btnLogout = view.findViewById(R.id.buttonLogout)
        btnBack = view.findViewById(R.id.buttonBack)
        watchlistRecyclerView = view.findViewById(R.id.watchlistRecyclerView)
        noWatchlistMessage = view.findViewById(R.id.textEmptyWatchlist)

        // Set up the RecyclerView and Adapter
        watchlistAdapter = WatchListAdapter(
            items = mutableListOf(),
            onViewClick = { post ->
                // Handle View Details
                val fragment = ContentDetailFragment.newInstance(post.id.toInt())
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onDeleteClick = { post ->
                viewModel.removeFromWatchlist(post)
            }
        )

        watchlistRecyclerView.adapter = watchlistAdapter
        watchlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is ProfileUiState.Loading -> {
                            tvName.text = ""
                            tvEmail.text = ""
                        }
                        is ProfileUiState.Success -> {
                            tvName.text = "Name : ${state.name}"
                            tvEmail.text = "Email : ${state.email}"
                            watchlistAdapter.updateList(state.watchlist)
                            if (state.watchlist.isEmpty()) {
                                noWatchlistMessage.visibility = View.VISIBLE  // Show the "No watchlist" message
                                watchlistRecyclerView.visibility = View.GONE  // Hide the RecyclerView
                            } else {
                                noWatchlistMessage.visibility = View.GONE  // Hide the "No watchlist" message
                                watchlistRecyclerView.visibility = View.VISIBLE  // Show the RecyclerView
                            }
                        }
                        is ProfileUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                        }
                        is ProfileUiState.LoggedOut -> {
                            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    }
                }
            }
        }

        btnLogout.setOnClickListener {
            viewModel.logout(requireContext())
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}
