package com.example.video_player_hub.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.video_player_hub.R
import com.example.video_player_hub.util.TokenManager
import com.example.video_player_hub.vm.ContentDetailUiState
import com.example.video_player_hub.vm.ContentDetailViewModel
import com.example.video_player_hub.vm.ContentDetailViewModelFactory
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.launch

class ContentDetailFragment : Fragment() {

    private lateinit var playerView: PlayerView
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private lateinit var bodyTextView: TextView
    private lateinit var backButton: Button

    private var exoPlayer: ExoPlayer? = null
    private lateinit var token: String

    private val vm: ContentDetailViewModel by viewModels {
        ContentDetailViewModelFactory(token, this)
    }

    companion object {
        private const val ARG_POST_ID = "post_id"

        fun newInstance(postId: Int): ContentDetailFragment {
            val fragment = ContentDetailFragment()
            val args = Bundle()
            args.putInt(ARG_POST_ID, postId)
            fragment.arguments = args
            return fragment
        }
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
    ): View = inflater.inflate(R.layout.content_detail_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        playerView = view.findViewById(R.id.playerView)
        progressBar = view.findViewById(R.id.progressBar)
        titleTextView = view.findViewById(R.id.titleTextView)
        bodyTextView = view.findViewById(R.id.bodyTextView)
        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val postId = arguments?.getInt(ARG_POST_ID) ?: -1
        if (postId == -1) {
            Toast.makeText(requireContext(), "Invalid post ID", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        // Observe UI state in lifecycle-aware manner
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    when (state) {
                        is ContentDetailUiState.Loading -> {
                            progressBar.visibility = ProgressBar.VISIBLE
                            titleTextView.text = ""
                            bodyTextView.text = ""
                            releasePlayer()
                        }

                        is ContentDetailUiState.Success -> {
                            progressBar.visibility = ProgressBar.GONE
                            titleTextView.text = state.post.title
                            bodyTextView.text = state.post.body
                            setupPlayer()
                        }

                        is ContentDetailUiState.Error -> {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                            releasePlayer()
                        }
                    }
                }
            }
        }

        // Trigger post loading
        vm.loadPost(postId)
    }

    private fun setupPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(requireContext()).build()
            playerView.player = exoPlayer
        }

        val videoUrl = "https://samplelib.com/lib/preview/mp4/sample-5s.mp4"
        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            // Seek to saved playback position from ViewModel
            seekTo(vm.playbackPosition)
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    progressBar.visibility =
                        if (state == Player.STATE_BUFFERING) ProgressBar.VISIBLE else ProgressBar.GONE
                }

                override fun onPlayerError(error: PlaybackException) {
                    Toast.makeText(requireContext(), "Video error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
        playerView.player = null
    }

    override fun onStart() {
        super.onStart()
        // No initialization here, done when post loads
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.let {
            vm.playbackPosition = it.currentPosition
            it.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        exoPlayer?.let {
            vm.playbackPosition = it.currentPosition
            it.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }
}
