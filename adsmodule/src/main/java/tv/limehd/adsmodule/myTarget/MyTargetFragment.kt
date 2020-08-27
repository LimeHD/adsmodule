package tv.limehd.adsmodule.myTarget

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.ads.interactivemedia.v3.api.AdsManager
import com.my.target.instreamads.InstreamAd
import com.my.target.instreamads.InstreamAdPlayer
import tv.limehd.adsmodule.AdType
import tv.limehd.adsmodule.BackgroundAdManger
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.R
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState

class MyTargetFragment(
    private var lastAd: String,
    private var resId: Int,
    private var fragmentState: FragmentState,
    private val adRequestListener: AdRequestListener?,
    private val adShowListener: AdShowListener?,
    private var limeAds: LimeAds
) : Fragment() {

    companion object {
        private const val TAG = "myTargetFragment"
        var isShowingAd: Boolean = false
    }

    private lateinit var mInstreamAd: InstreamAd
    private lateinit var mInstreamAdPlayer: InstreamAdPlayer

    private lateinit var videoContainer: RelativeLayout
    private lateinit var rootContainer: RelativeLayout
    private lateinit var buttonSkip: Button

    private lateinit var leftTimeText: TextView

    private var leftTimeDelay = 0f
    private lateinit var leftHandler: Handler

    private var adsManager: AdsManager? = null

    private var leftRunnable: Runnable = object : Runnable {
        override fun run() {
            if (leftTimeDelay > 0) {
                leftTimeDelay--
                val leftText = "Осталось ${leftTimeDelay.toInt()} сек."
                leftTimeText.text = leftText
                leftHandler.postDelayed(this, 1000)
            }
        }
    }

    private var skipAllowDelay = 0f
    private lateinit var skipHandler: Handler
    private val skipRunnable: Runnable = object : Runnable {
        override fun run() {
            if (skipAllowDelay > 0) {
                skipAllowDelay--
                val skipText = "Пропустить через " + skipAllowDelay.toInt() + " сек."
                buttonSkip.text = skipText
                skipHandler.postDelayed(this, 1000)
            }
            if (skipAllowDelay == 0f) {
                buttonSkip.text = "Пропустить рекламу"
                buttonSkip.isEnabled = true
            }
        }
    }

    fun setInstreamAd(instreamAd: InstreamAd){
        mInstreamAd = instreamAd
        mInstreamAd.useDefaultPlayer()
        setListener()
    }

    fun setAdsManager(adsManager: AdsManager?) {
        this.adsManager = adsManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_instream, container, false)
        rootContainer = rootView.findViewById(R.id.root_container)
        videoContainer = rootView.findViewById(R.id.video_container)
        leftTimeText = rootView.findViewById(R.id.time_left)
        buttonSkip = rootView.findViewById(R.id.btn_skip)
        // Skip button
        buttonSkip.setOnClickListener {
            adShowListener?.onSkip(this.getString(R.string.skipped), AdType.MyTarget)
            mInstreamAd.skip()
        }
        // Ad Click
        rootContainer.setOnClickListener {
            adShowListener?.onClick(this.getString(R.string.clicked), AdType.MyTarget)
        }
        return rootView
    }

    fun initializePlaying(adType: AdType){
        when(adType) {
            is AdType.IMA -> {
                // should hide my target ui widgets
                adsManager?.start()
            }
            is AdType.MyTarget -> mInstreamAd.startPreroll()
        }
    }

    private fun setListener() {
        mInstreamAdPlayer = mInstreamAd.player!!
        try {
            videoContainer.removeAllViews()
        }catch (e: Exception) {
            Log.d(TAG, "setListener: ${e.printStackTrace()}")
            adRequestListener?.onEarlyRequest()
        }
        videoContainer.addView(mInstreamAdPlayer.view)
        mInstreamAd.listener = object : InstreamAd.InstreamAdListener {
            override fun onLoad(p0: InstreamAd) {
                Log.d(TAG, "onLoad called")
            }

            override fun onComplete(message: String, p1: InstreamAd) {

                isShowingAd = false

                limeAds.adUiContainer?.visibility = View.GONE

                if(LimeAds.isBackgroundRequestsCalled) {
                    // should restart BackgroundAdManager
                    BackgroundAdManger.clearVariables()
                    LimeAds.startBackgroundRequests(context!!, resId, fragmentState, adRequestListener, adShowListener)
                }else {
                    Log.d(TAG, "BACKGROUND REQUEST ARE NOT STARTED")
                }

                if(this@MyTargetFragment::leftHandler.isInitialized) {
                    leftHandler.removeCallbacks(leftRunnable)
                }
                if(this@MyTargetFragment::skipHandler.isInitialized) {
                    skipHandler.removeCallbacks(skipRunnable)
                }
                adShowListener?.onComplete(this@MyTargetFragment.getString(R.string.completed), AdType.MyTarget)
                limeAds.prerollTimerHandler.postDelayed(limeAds.prerollTimerRunnable, 1000)
            }

            override fun onBannerPause(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
                Log.d(TAG, "onBannerPause called")
            }

            override fun onBannerStart(instreamAd: InstreamAd, instreamAdBanner: InstreamAd.InstreamAdBanner) {
                isShowingAd = true
                adShowListener?.onShow(this@MyTargetFragment.getString(R.string.showing), AdType.MyTarget)
                leftTimeText.visibility = View.VISIBLE
                leftTimeText.bringToFront()
                if(instreamAdBanner.duration > 0) {
                    leftTimeDelay = instreamAdBanner.duration
                    leftHandler = Handler()
                    val leftText = "Осталось ${instreamAdBanner.duration.toInt()} сек."
                    leftTimeText.text = leftText
                    leftHandler.postDelayed(leftRunnable, 1000)
                }

                if(instreamAdBanner.allowClose) {
                    skipAllowDelay = if(instreamAdBanner.allowCloseDelay > 1f){
                        instreamAdBanner.allowCloseDelay
                    }else {
                        5f
                    }
                    buttonSkip.isEnabled = false
                    buttonSkip.visibility = View.VISIBLE
                    val skipText = "Пропустить через " + skipAllowDelay.toInt() + " сек."
                    buttonSkip.text = skipText
                    skipHandler = Handler()
                    skipHandler.postDelayed(skipRunnable, 1000)
                }

            }

            override fun onNoAd(error: String, p1: InstreamAd) {
                Log.d(TAG, "onNoAd called")
                adShowListener?.onError(error, AdType.MyTarget)
            }

            override fun onBannerResume(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
                Log.d(TAG, "onBannerResume called")
            }

            override fun onBannerTimeLeftChange(p0: Float, p1: Float, p2: InstreamAd) {

            }

            override fun onError(error: String, p1: InstreamAd) {
                Log.d(TAG, "onError called")
                adShowListener?.onError(error, AdType.MyTarget)
                fragmentManager?.beginTransaction()?.remove(this@MyTargetFragment)?.commit()
                limeAds.getNextAd(AdType.MyTarget.typeSdk)
            }

            override fun onBannerComplete(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
                Log.d(TAG, "onBannerComplete called")
            }

        }
    }

    override fun onResume() {
        super.onResume()
        if(this::mInstreamAd.isInitialized) {
            mInstreamAd.resume()
        }
        if(this::leftHandler.isInitialized) {
            leftHandler.postDelayed(leftRunnable, 1000)
        }
    }

    override fun onPause() {
        super.onPause()
        if(this::mInstreamAd.isInitialized) {
            mInstreamAd.pause()
        }
        if(this::leftHandler.isInitialized) {
            leftHandler.removeCallbacks(leftRunnable)
        }
    }

    override fun onStop() {
        super.onStop()
        if(this::mInstreamAd.isInitialized) {
            mInstreamAd.pause()
        }
        if(this::leftHandler.isInitialized) {
            leftHandler.removeCallbacks(leftRunnable)
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
        if(this::leftHandler.isInitialized) {
            leftHandler.removeCallbacks(leftRunnable)
        }
        if(this::skipHandler.isInitialized) {
            skipHandler.removeCallbacks(skipRunnable)
        }
        if(this::mInstreamAd.isInitialized) {
            mInstreamAd.stop()
        }
        if(adsManager != null) {
            adsManager?.destroy()
        }
    }

}