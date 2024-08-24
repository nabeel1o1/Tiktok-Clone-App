# Tiktok-Clone App

## Overview

The Tiktok-Clone App is a video-sharing application inspired by TikTok. It allows users to view, scroll, and interact with short videos. The app is designed to handle video playback efficiently, including checking device capabilities and managing video playback smoothly as users scroll through the feed.

## Core Features

1. **Device-Specific Video Resolution**:
   - The app fetches video metadata and selects the appropriate video resolution based on the device's support for H264 or H265 video codecs.
   - The video URL is dynamically chosen to match the device's codec capabilities and preferred resolution.

2. **Smooth Video Scrolling**:
   - Videos are displayed in a vertical scrollable list using a `RecyclerView` with `PagerSnapHelper`, providing a TikTok-like scrolling experience.
   - The `PagerSnapHelper` ensures that videos snap to the center of the view on scroll, mimicking the TikTok interface.

3. **Paging with Retrofit**:
   - The app uses the Paging3 library to handle large lists of videos efficiently. The `PagingSource` is implemented to fetch data from a remote server via Retrofit.

4. **ExoPlayer Integration**:
   - ExoPlayer is used to handle video playback. The player is managed to start, pause, and resume playback based on user interactions and scroll position.

## Core Business Logic

## Pagination Logic

```kotlin
class HomeFeedRepo @Inject constructor(private val tiktokApi: TiktokApiService) :
    PagingSource<Int, Data>() {

    override fun getRefreshKey(state: PagingState<Int, Data>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Data> {
        return try {
            val position = params.key ?: 1
            val response = tiktokApi.getHomeFeed(page = position)
            LoadResult.Page(
                data = response.data,
                prevKey = if (position == 1) null else position - 1,
                nextKey = if (response.pagination.hasNextPage) position + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
```

### Device Resolution Check and Video Selection

The following logic is used to select the appropriate video URL based on device codec support:

**In Media data class**
```kotlin
data class Media(
    val h264: ResolutionUrls,
    val h265: ResolutionUrls,
    val key: String,
    val thumbnailKey: String,
    val transcoded: Boolean
)

fun Media.getVideoUrl(supportsH265: Boolean, supportsH264: Boolean): String {
    return when {
        supportsH265 -> h265.getBestResolution()
        supportsH264 -> h264.getBestResolution()
        else -> ""
    }
}
```
**ResolutionUrls class to get the video url**
```kotlin
data class ResolutionUrls(
    @SerializedName("1080p")
    val p1080: String,
    @SerializedName("720p")
    val p720: String,
    @SerializedName("480p")
    val p480: String
) {
    fun getBestResolution(): String {
        return when {
            p480.isNotEmpty() -> p480 //Setting the priority to 480p as per the requirement of assessment task
            p1080.isNotEmpty() -> p1080
            else -> p720
        }
    }
}
```
**ViewModel class handles the businees logic for selecting video resolution according to the device supported resolution**
```kotlin
@HiltViewModel
class HomeFeedViewModel @Inject constructor(private val homeFeedRepository: HomeFeedRepo) : ViewModel() {

    val supportsH265 = isCodecSupported(MediaFormat.MIMETYPE_VIDEO_HEVC) // For H265
    val supportsH264 = isCodecSupported(MediaFormat.MIMETYPE_VIDEO_AVC)  // For H264

    val homeFeedDataList = getHomeFeedData().cachedIn(viewModelScope)

    private fun getHomeFeedData() = Pager(
        config = PagingConfig(pageSize = 40),
        pagingSourceFactory = { homeFeedRepository }
    ).liveData.map { pagingData ->
        pagingData.map { videoItem ->
            videoItem.copy(media = videoItem.media.copy(
                key = videoItem.media.getVideoUrl(supportsH265, supportsH264)
            ))
        }
    }

    private fun isCodecSupported(codecName: String): Boolean {
        val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        for (codecInfo in codecList.codecInfos) {
            if (codecInfo.isEncoder) continue
            for (type in codecInfo.supportedTypes) {
                if (type.equals(codecName, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }
}
```
## Video Scrolling and Playback
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HomeFeedAdapter()
        val homeFeedViewModel = ViewModelProvider(this)[HomeFeedViewModel::class.java]

        val snapHelper = PagerSnapHelper()

        with(binding) {

            lytToolbar.setPadding(0, getStatusBarHeight(), 0, 0)
            rvHomeFeed.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvHomeFeed.setHasFixedSize(true)
            rvHomeFeed.adapter = adapter.withLoadStateHeaderAndFooter(
                header = LoaderAdapter(),
                footer = LoaderAdapter()
            )
            snapHelper.attachToRecyclerView(rvHomeFeed)

            rvHomeFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        playVideoAtVisiblePosition(recyclerView, newState)
                    }
                }
            })
        }

        homeFeedViewModel.homeFeedDataList.observe(viewLifecycleOwner) {
            adapter.submitData(lifecycle, it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    fun playVideoAtVisiblePosition(recyclerView: RecyclerView, recyclerViewState: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()

        if (visiblePosition != RecyclerView.NO_POSITION && recyclerViewState != RecyclerView.SCROLL_STATE_DRAGGING) {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(visiblePosition) as HomeFeedAdapter.HomeFeedViewHolder?
            viewHolder?.let {
                adapter.playVideo(it, visiblePosition)
            }
        }
    }
```
## Video Playback Management
```kotlin
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
```

