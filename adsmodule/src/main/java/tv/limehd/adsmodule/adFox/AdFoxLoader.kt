package tv.limehd.adsmodule.adFox

import android.content.Context
import android.util.Log
import android.widget.VideoView
import com.yandex.mobile.ads.video.*
import com.yandex.mobile.ads.video.models.ad.MediaFile
import com.yandex.mobile.ads.video.models.ad.VideoAd
import com.yandex.mobile.ads.video.models.vmap.Vmap
import tv.limehd.adsmodule.AdType.*
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener

class AdFoxLoader(
    private val context: Context,
    private val params: HashMap<String, String>,
    private val pageId: String,
    private val videoView: VideoView,
    private val adRequestListener: AdRequestListener? = null,
    private val adShowListener: AdShowListener? = null
) {

    companion object {
        private const val TAG = "AdFoxLoader"
    }

    private var vmapLoader: VmapLoader = VmapLoader(context)
    private var vmapRequestConfiguration = VmapRequestConfiguration.Builder(pageId).build()

    fun loadVmap() {
        Log.d(TAG, "loadVmap: loading AD FOX")
        vmapLoader.loadVmap(context, vmapRequestConfiguration)
        vmapLoader.setOnVmapLoadedListener(object : VmapLoader.OnVmapLoadedListener() {
            override fun onVmapLoaded(vmap: Vmap) {
                Log.d(TAG, "onVmapLoaded: Vmap is loaded")
                adRequestListener?.onLoaded("AdFox vmap is loaded", AdFox)
                val vastRequestConfiguration = VastRequestConfiguration.Builder(vmap.adBreaks[0]).setParameters(params).build()
                val videoAdLoader = VideoAdLoader(context)
                videoAdLoader.loadAd(context, vastRequestConfiguration)
                videoAdLoader.setOnVideoAdLoadedListener(object : VideoAdLoader.OnVideoAdLoadedListener() {
                    override fun onVideoAdLoaded(videoAds: MutableList<VideoAd>) {
                        Log.d(TAG, "onVideoAdLoaded: video ad is loaded")
                        adRequestListener?.onLoaded("AdFox video ad is loaded", AdFox)
                        val mediaFiles = videoAds[0].creatives[0].mediaFiles
                        var mMediaFile: MediaFile = mediaFiles[0]
                        for (mediaFile in mediaFiles) {
                            if (mediaFile.mimeType == "video/webm") {
                                mMediaFile = mediaFile
                            }
                        }
                        videoView.setVideoPath(mMediaFile.uri)
                        videoView.start()
                    }

                    override fun onVideoAdFailedToLoad(videoAdError: VideoAdError) {
                        Log.d(TAG, "onVideoAdFailedToLoad: ${videoAdError.description}")
                        adRequestListener?.onError(videoAdError.description, AdFox)
                    }
                })
            }

            override fun onVmapFailedToLoad(vmapError: VmapError) {
                Log.d(TAG, "onVmapFailedToLoad: Vmap is not loaded has ${vmapError.description}")
                adRequestListener?.onError(vmapError.description!!, AdFox)
            }
        })
    }

}