package tv.limehd.adsmodule.ima.vast2

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import tv.limehd.adsmodule.AdType
import tv.limehd.adsmodule.BackgroundAdManger
import tv.limehd.adsmodule.Constants.Companion.TIMEOUT
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.R
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState
import tv.limehd.adsmodule.myTarget.MyTargetFragment

/**
 * This class stands for loading ima ads logic
 * This is where all business magic happens
 * Errors/Clicks/Completion/Loaded/Requested - will be thrown to AdRequest and AdShow listeners
 */

class ImaLoader constructor(
    private val context: Context,
    private val adTagUrl: String,
    private val lastAd: String,
    private val resId: Int,
    private val container: FrameLayout,
    private val adRequestListener: AdRequestListener?,
    private val adShowListener: AdShowListener?,
    private val limeAds: LimeAds,
    private val myTargetFragment: MyTargetFragment,
    private val fragmentManager: FragmentManager
) : AdsLoader.AdsLoadedListener, AdErrorEvent.AdErrorListener, AdEvent.AdEventListener {

    companion object {
        private const val TAG = "ImaLoader"
    }

    private lateinit var imaFragment: ImaFragment

    private var isTimeout = true

    private lateinit var mSdkFactory: ImaSdkFactory
    private lateinit var mSdkSetting: ImaSdkSettings
    private lateinit var mAdsLoader: AdsLoader
    private lateinit var adsManager: AdsManager

    private lateinit var fragmentState: FragmentState

    private val leftHandler: Handler = Handler()

    private var timeout = TIMEOUT / 1000

    private var leftRunnable: Runnable = object : Runnable {
        override fun run() {
            if (timeout > 0) {
                timeout--
                Log.d(TAG, timeout.toString())
                leftHandler.postDelayed(this, 1000)
            }else{
                if(isTimeout){
                    adRequestListener?.onError(context.resources.getString(R.string.timeout_occurred), AdType.IMA)
                    if(lastAd == AdType.IMA.typeSdk){
                        fragmentState.onErrorState(context.resources.getString(R.string.no_ad_found_at_all), AdType.IMA)
                    }else {
                        limeAds.getNextAd(AdType.IMA.typeSdk)
                    }
                }
            }
        }
    }

    private lateinit var viewGroup: ViewGroup

    fun loadImaAd(fragmentState: FragmentState) {
        Log.d(TAG, "loadImaAd called")
        this.fragmentState = fragmentState

        val activity = context as Activity
        viewGroup = activity.findViewById(resId)

        mSdkFactory = ImaSdkFactory.getInstance()
        mSdkSetting = mSdkFactory.createImaSdkSettings()
        mSdkSetting.language = "ru"

        val adDisplayContainer = mSdkFactory.createAdDisplayContainer()
        adDisplayContainer.adContainer = container

        mAdsLoader = mSdkFactory.createAdsLoader(context, mSdkSetting, adDisplayContainer)
        mAdsLoader.addAdsLoadedListener(this)
        mAdsLoader.addAdErrorListener(this)

        val adsRequest = mSdkFactory.createAdsRequest()
        adsRequest.adTagUrl = adTagUrl
        adsRequest.adDisplayContainer = adDisplayContainer

        adsRequest.contentProgressProvider = ContentProgressProvider {
            VideoProgressUpdate(0, 120)
        }

        adsRequest.setVastLoadTimeout(TIMEOUT)

        adRequestListener?.onRequest(context.getString(R.string.requested), AdType.IMA)
        mAdsLoader.requestAds(adsRequest)

        leftHandler.postDelayed(leftRunnable, 1000)
    }

    override fun onAdsManagerLoaded(adsManagerLoadedEvent: AdsManagerLoadedEvent?) {
        Log.d(TAG, "onAdsManagerLoaded")
        isTimeout = false
        adsManager = adsManagerLoadedEvent!!.adsManager
        adsManager.addAdEventListener(this)
        adsManager.addAdErrorListener(this)
        adsManager.init()
    }

    override fun onAdError(adErrorEvent: AdErrorEvent?) {
        Log.d(TAG, "Ima onAdError called with ${adErrorEvent?.error?.errorCodeNumber}")
        adRequestListener?.onError(adErrorEvent?.error?.message.toString(), AdType.IMA)
        adShowListener?.onError(adErrorEvent?.error?.message.toString(), AdType.IMA)
        if(!isTimeout) {
            if (lastAd == AdType.IMA.typeSdk) {
                fragmentState.onErrorState(context.getString(R.string.no_ad_found_at_all), AdType.IMA)
            } else {
                limeAds.getNextAd(AdType.IMA.typeSdk)
            }
        }
    }

    override fun onAdEvent(adEvent: AdEvent?) {
        when(adEvent?.type){
            AdEvent.AdEventType.LOADED -> {
                Log.d(TAG, "loaded")
                adRequestListener?.onLoaded(context.getString(R.string.loaded), AdType.IMA)
                viewGroup.visibility = View.VISIBLE
                myTargetFragment.setAdsManager(adsManager)
                fragmentManager.beginTransaction().show(myTargetFragment).commitAllowingStateLoss()
                fragmentState.onSuccessState(myTargetFragment, AdType.IMA)
            }
            AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                Log.d(TAG, "ALL_ADS_COMPLETED")
                viewGroup.visibility = View.INVISIBLE
                adShowListener?.onComplete(context.getString(R.string.completed), AdType.IMA)

                // should restart BackgroundAdManager
                BackgroundAdManger.clearVariables()
                LimeAds.startBackgroundRequests(context, resId, fragmentState, adRequestListener, adShowListener)

                limeAds.prerollTimerHandler.postDelayed(limeAds.prerollTimerRunnable, 1000)
            }
            AdEvent.AdEventType.CLICKED -> {
                Log.d(TAG, "CLICKED")
                adShowListener?.onClick(context.getString(R.string.clicked), AdType.IMA)
            }
            AdEvent.AdEventType.COMPLETED -> {
                Log.d(TAG, "COMPLETED")
            }
            AdEvent.AdEventType.CUEPOINTS_CHANGED -> {
                Log.d(TAG, "CUEPOINTS_CHANGED")
            }
            AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED -> {
                Log.d(TAG, "CONTENT_PAUSE_REQUESTED")
            }
            AdEvent.AdEventType.CONTENT_RESUME_REQUESTED -> {
                Log.d(TAG, "CONTENT_RESUME_REQUESTED")
            }
            AdEvent.AdEventType.FIRST_QUARTILE -> {
                Log.d(TAG, "FIRST_QUARTILE")
            }
            AdEvent.AdEventType.LOG -> {
                Log.d(TAG, "LOG")
            }
            AdEvent.AdEventType.AD_BREAK_READY -> {
                Log.d(TAG, "AD_BREAK_READY")
            }
            AdEvent.AdEventType.MIDPOINT -> {
                Log.d(TAG, "MIDPOINT")
            }
            AdEvent.AdEventType.PAUSED -> {
                Log.d(TAG, "PAUSED")
            }
            AdEvent.AdEventType.RESUMED -> {
                Log.d(TAG, "RESUMED")
            }
            AdEvent.AdEventType.SKIPPABLE_STATE_CHANGED -> {
                Log.d(TAG, "SKIPPABLE_STATE_CHANGED")
            }
            AdEvent.AdEventType.SKIPPED -> {
                Log.d(TAG, "SKIPPED")
                adShowListener?.onSkip(context.getString(R.string.skipped), AdType.IMA)
            }
            AdEvent.AdEventType.STARTED -> {
                Log.d(TAG, "STARTED")
                container.visibility = View.VISIBLE
                adShowListener?.onShow(context.getString(R.string.showing), AdType.IMA)
            }
            AdEvent.AdEventType.TAPPED -> {
                Log.d(TAG, "TAPPED")
                adShowListener?.onClick(context.getString(R.string.clicked), AdType.IMA)
            }
            AdEvent.AdEventType.ICON_TAPPED -> {
                Log.d(TAG, "ICON_TAPPED")
            }
            AdEvent.AdEventType.THIRD_QUARTILE -> {
                Log.d(TAG, "THIRD_QUARTILE")
            }
            AdEvent.AdEventType.AD_PROGRESS -> {
                Log.d(TAG, "AD_PROGRESS")
            }
            AdEvent.AdEventType.AD_BUFFERING -> {
                Log.d(TAG, "AD_BUFFERING")
            }
            AdEvent.AdEventType.AD_BREAK_STARTED -> {
                Log.d(TAG, "AD_BREAK_STARTED")
            }
            AdEvent.AdEventType.AD_BREAK_ENDED -> {
                Log.d(TAG, "AD_BREAK_ENDED")
            }
            AdEvent.AdEventType.AD_PERIOD_STARTED -> {
                Log.d(TAG, "AD_PERIOD_STARTED")
            }
            AdEvent.AdEventType.AD_PERIOD_ENDED -> {
                Log.d(TAG, "AD_PERIOD_ENDED")
            }
        }
    }

}