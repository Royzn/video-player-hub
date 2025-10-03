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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.repeatOnLifecycle
import com.example.video_player_hub.MainActivity
import com.example.video_player_hub.R
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

    private lateinit var token: String

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(token)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = TokenManager.getToken(requireContext()).orEmpty()
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Authentication required", Toast.LENGTH_LONG).show()
            requireActivity().finish()
        }
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is ProfileUiState.Loading -> {
                            tvName.text = ""
                            tvEmail.text = ""
                            btnLogout.isEnabled = false
                        }
                        is ProfileUiState.Success -> {
                            tvName.text = "Name : ${state.name}"
                            tvEmail.text = "Email : ${state.email}"
                            btnLogout.isEnabled = true
                        }
                        is ProfileUiState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            btnLogout.isEnabled = false
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
