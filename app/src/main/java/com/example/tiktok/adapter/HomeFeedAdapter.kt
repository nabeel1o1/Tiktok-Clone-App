package com.example.tiktok.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tiktok.databinding.ItemHomeFeedBinding
import com.example.tiktok.model.Data
import com.example.tiktok.utilies.Constants.MEDIA_BASE_URL

class HomeFeedAdapter : PagingDataAdapter<Data, HomeFeedAdapter.HomeFeedViewHolder>(COMPARATOR) {

    private var currentPlayingPosition: Int = -1
    private var currentPlayer: ExoPlayer? = null

    private var isVideoPlaying: Boolean = false

    override fun onBindViewHolder(holder: HomeFeedViewHolder, position: Int) {

        val item : Data? = getItem(position)
        item ?: return

        with(ItemHomeFeedBinding.bind(holder.itemView)) {
            val profileImgUrl = MEDIA_BASE_URL + item.user.profilePicture
            Glide.with(holder.itemView.context).load(profileImgUrl).into(imgProfile)

            val isLiked: Boolean = item.liked
            chLike.isChecked = isLiked

            tvLikeCount.text = item.counts.likes.toString()

            tvCommentCount.text = item.counts.comments.toString()

            chSave.isChecked = item.bookmarked

            tvSaveCount.text = item.counts.bookmarks.toString()

            tvShareCount.text = item.shareCount.toString()

            tvTitle.text = item.title

            tvDescription.text = item.description

            item.tags?.let {
                if (it.isNotEmpty()) {
                    tvTags.text = it.joinToString(separator = " ")
                }
            }

            exoPlayerView.player = null

            if (position == currentPlayingPosition) {
                currentPlayer?.let {
                    exoPlayerView.player = it
                    exoPlayerView.player?.playWhenReady = isVideoPlaying
                }
            }
        }

        holder.itemView.setOnClickListener {
            currentPlayer?.let { player ->
                if (currentPlayingPosition == position) {
                    isVideoPlaying = !player.playWhenReady
                    player.playWhenReady = isVideoPlaying
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFeedViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHomeFeedBinding.inflate(inflater, parent, false)
        return HomeFeedViewHolder(binding)
    }

    override fun onViewAttachedToWindow(holder: HomeFeedViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (holder.absoluteAdapterPosition == 0 && currentPlayingPosition == -1) {
            playVideo(holder, holder.absoluteAdapterPosition)
        }
    }

    override fun onViewDetachedFromWindow(holder: HomeFeedViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder.absoluteAdapterPosition == currentPlayingPosition) {
            currentPlayer?.playWhenReady = false
        }
    }

    inner class HomeFeedViewHolder(itemView: ItemHomeFeedBinding) :
        RecyclerView.ViewHolder(itemView.root)

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Data>() {

            override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
                return oldItem._id == newItem._id
            }

            override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
                return oldItem == newItem
            }
        }
    }

    fun playVideo(holder: HomeFeedViewHolder, position: Int) {

        with(ItemHomeFeedBinding.bind(holder.itemView)) {

            currentPlayer?.release()

            currentPlayer = ExoPlayer.Builder(holder.itemView.context).build()

            exoPlayerView.player = currentPlayer

            val item = getItem(position) ?: return

            val videoUrl = MEDIA_BASE_URL + item.media.key

            val mediaItem = MediaItem.fromUri(videoUrl)
            currentPlayer?.setMediaItem(mediaItem)

            currentPlayer?.prepare()

            currentPlayer?.playWhenReady = true

            currentPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_ENDED) {
                        currentPlayer?.seekTo(0)
                        currentPlayer?.playWhenReady = true
                    }
                }
            })
            currentPlayingPosition = position
        }
    }

    fun pauseCurrentVideo() {
        currentPlayer?.playWhenReady = false
    }

    fun resumeCurrentVideo() {
        currentPlayer?.playWhenReady = true
    }

    fun releasePlayer() {
        currentPlayer?.release()
        currentPlayer = null
        currentPlayingPosition = -1
    }
}