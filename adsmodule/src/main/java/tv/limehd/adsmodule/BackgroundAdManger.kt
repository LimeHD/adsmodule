package tv.limehd.adsmodule

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.my.target.instreamads.InstreamAd
import kotlinx.coroutines.*
import tv.limehd.adsmodule.interfaces.AdLoader
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState
import tv.limehd.adsmodule.model.Ad
import tv.limehd.adsmodule.model.PreloadAds
import tv.limehd.adsmodule.myTarget.MyTargetFragment
import tv.limehd.adsmodule.myTarget.MyTargetLoader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BackgroundAdManger(
    private val context: Context,
    private val resId: Int,
    private val fragmentState: FragmentState,
    private val adShowListener: AdShowListener?,
    private val adRequestListener: AdRequestListener?,
    private val preload: PreloadAds,
    private val adsList: List<Ad>,
    private val limeAds: LimeAds,
    private val fragmentManager: FragmentManager,
    private val myTargetFragment: MyTargetFragment
){

    companion object {
        private const val TAG = "BackgroundAdManger"
        var imaAdsManager: AdsManager? = null
        var myTargetInstreamAd: InstreamAd? = null
        var googleInterstitialAd: InterstitialAd? = null

        var myTargetFragmentFrameLayout: FrameLayout? = null

        fun clearVariables() {
            googleInterstitialAd = null
            imaAdsManager = null
            myTargetInstreamAd = null
        }
    }

    // ***************************************************** IMA SDK ********************************************************* //

    private lateinit var mSdkFactory: ImaSdkFactory
    private lateinit var mSdkSetting: ImaSdkSettings
    private lateinit var mAdsLoader: AdsLoader

    private suspend fun loadIma(container: ViewGroup) : Boolean {
        Log.d(TAG, "loadIma: called")

        var adContainer = myTargetFragment.view?.findViewById(R.id.imaAdFrameLayout) as FrameLayout?

        if(myTargetFragment.view == null) {
            adContainer = myTargetFragmentFrameLayout
        }

        mSdkFactory = ImaSdkFactory.getInstance()
        mSdkSetting = mSdkFactory.createImaSdkSettings()
        mSdkSetting.language = "ru"

        val adDisplayContainer = mSdkFactory.createAdDisplayContainer()
        adDisplayContainer.adContainer = adContainer

        mAdsLoader = mSdkFactory.createAdsLoader(context, mSdkSetting, adDisplayContainer)

        val adsRequest = mSdkFactory.createAdsRequest()
        adsRequest.adTagUrl = Constants.testAdTagUrl
        adsRequest.adDisplayContainer = adDisplayContainer

        adsRequest.contentProgressProvider = ContentProgressProvider {
            VideoProgressUpdate(0, 120)
        }

        adsRequest.setVastLoadTimeout(Constants.TIMEOUT)

        adRequestListener?.onRequest(context.getString(R.string.requested), AdType.IMA)
        mAdsLoader.requestAds(adsRequest)

        return suspendCoroutine {cont ->
            mAdsLoader.addAdsLoadedListener {
                Log.d(TAG, "loadIma: ima loaded")
                imaAdsManager = it!!.adsManager
                imaAdsManager?.addAdEventListener { adEvent ->
                    when(adEvent.type){
                        AdEvent.AdEventType.LOADED -> {
                            Log.d(TAG, "loadIma: LOADED")
                        }
                        AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                            Log.d(TAG, "loadIma: ALL_ADS_COMPLETED")
                            limeAds.adUiContainer?.visibility = View.GONE

                            // should restart BackgroundAdManager
                            BackgroundAdManger.clearVariables()
                            LimeAds.startBackgroundRequests(
                                context,
                                resId,
                                fragmentState,
                                adRequestListener,
                                adShowListener
                            )

                            // should start preroll handler
                            limeAds.prerollTimerHandler.postDelayed(limeAds.prerollTimerRunnable, 1000)

                            try {
                                fragmentManager.beginTransaction().remove(fragmentManager.fragments[7]).commitNow()
                            }catch (e: Exception){
                                Log.d(TAG, "loadIma: ${e.message}")
                            }

                            adShowListener?.onComplete(context.getString(R.string.completed), AdType.IMA)
                        }
                        AdEvent.AdEventType.COMPLETED -> {
                            Log.d(TAG, "loadIma: COMPLETED")
                        }
                        AdEvent.AdEventType.SKIPPED -> {
                            try {
                                fragmentManager.beginTransaction().remove(fragmentManager.fragments[7]).commitNow()
                            }catch (e: Exception){
                                Log.d(TAG, "loadIma: ${e.message}")
                            }
                            Log.d(TAG, "loadIma: SKIPPED")
                        }
                        AdEvent.AdEventType.TAPPED -> {
                            Log.d(TAG, "loadIma: CLICKED")
                        }
                    }
                }
                imaAdsManager?.addAdErrorListener { adErrorEvent ->
                    Log.d(TAG, "loadIma: ERROR ${adErrorEvent.error.message}")
                }
                imaAdsManager?.init()
                cont.resume(true)
            }
            mAdsLoader.addAdErrorListener {
                Log.d(TAG, "loadIma: error")
                adRequestListener?.onError(it?.error?.message.toString(), AdType.IMA)
                cont.resume(false)
            }
        }
    }

    // ***************************************************** MyTarget SDK ********************************************************* //

    private suspend fun loadMyTarget() : Boolean {
        Log.d(TAG, "loadMyTarget: called")
        adRequestListener?.onRequest(context.getString(R.string.requested), AdType.MyTarget)
        val myTargetLoader = MyTargetLoader(context)
        myTargetLoader.loadAd()
        return suspendCoroutine {
            myTargetLoader.setAdLoader(object : AdLoader {
                override fun onRequest() {
                    TODO()
                }

                override fun onLoaded(instreamAd: InstreamAd) {
                    Log.d(TAG, "onLoaded: mytarget loaded")
                    adRequestListener?.onLoaded(context.getString(R.string.loaded), AdType.MyTarget)
                    myTargetInstreamAd = instreamAd
                    it.resume(true)
                }

                override fun onError(error: String) {
                    adRequestListener?.onError(context.getString(R.string.requestError), AdType.MyTarget)
                }

                override fun onNoAd(error: String) {
                    Log.d(TAG, "onNoAd: mytarget error")
                    adRequestListener?.onNoAd(context.getString(R.string.noAd), AdType.MyTarget)
                    it.resume(false)
                }
            })
        }
    }

    // ***************************************************** Google SDK ********************************************************* //

    private lateinit var interstitialAd: InterstitialAd

    private suspend fun loadGoogleAd() : Boolean {
        Log.d(TAG, "loadGoogleAd: called")
        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = LimeAds.googleUnitId
        adRequestListener?.onRequest(context.getString(R.string.requested), AdType.Google)
        interstitialAd.loadAd(AdRequest.Builder().build())
        return suspendCoroutine {
            interstitialAd.adListener = object : AdListener() {
                override fun onAdImpression() {
                    TODO()
                }

                override fun onAdLeftApplication() {
                    TODO()
                }

                override fun onAdClicked() {
                    adShowListener?.onClick(context.getString(R.string.clicked), AdType.Google)
                }

                override fun onAdFailedToLoad(errorType: Int) {
                    Log.d(TAG, "onAdFailedToLoad: google error")

                    var errorMessage = ""
                    when(errorType){
                        0 -> errorMessage = "ERROR_CODE_INTERNAL_ERROR"
                        1 -> errorMessage = "ERROR_CODE_INVALID_REQUEST"
                        2 -> errorMessage = " ERROR_CODE_NETWORK_ERROR"
                        3 -> errorMessage = "ERROR_CODE_NO_FILL"
                    }
                    if(errorType == 3){
                        // No Ad Error
                        adRequestListener?.onNoAd(errorMessage, AdType.Google)
                    }else{
                        // Some other error happened
                        adRequestListener?.onError(errorMessage, AdType.Google)
                    }

                    adShowListener?.onError(context.getString(R.string.error), AdType.Google)
                    it.resume(false)
                }

                override fun onAdClosed() {
                    Log.d(TAG, "onAdClosed: called")

                    limeAds.adUiContainer?.visibility = View.GONE

                    // should restart BackgroundAdManager
                    clearVariables()
                    LimeAds.startBackgroundRequests(context, resId, fragmentState, adRequestListener, adShowListener)

                    // should start preroll handler
                    limeAds.prerollTimerHandler.postDelayed(limeAds.prerollTimerRunnable, 1000)

                    try {
                        fragmentManager.beginTransaction().remove(fragmentManager.fragments[1]).commitNow()
                    }catch (e: Exception){
                        Log.d(TAG, "onAdClosed: ${e.message}")
                    }

                    adShowListener?.onCompleteInterstitial()
                }

                override fun onAdOpened() {
                    adShowListener?.onShow(context.getString(R.string.showing), AdType.Google)
                }

                override fun onAdLoaded() {
                    Log.d(TAG, "onAdLoaded: google loaded")
                    googleInterstitialAd = interstitialAd
                    it.resume(true)
                }
            }
        }
    }

    /**
     * Get already requested and cached ad type sdk name
     *
     * @return type sdk for ad that's ready
     */

    private fun getReadyAd() : String {
        var readySdk = ""
        if(imaAdsManager != null){
            readySdk = AdType.IMA.typeSdk
        }
        if(myTargetInstreamAd != null){
            readySdk = AdType.MyTarget.typeSdk
        }
        if(googleInterstitialAd != null){
            readySdk = AdType.Google.typeSdk
        }
        return readySdk
    }

    /**
     * Function stands for requesting ad in the background while user
     * doing/watching some movie or something. Because ad usually has failure,
     * so if user goes to next channel, we request 1 time
     * But if we do it in background thread, we can request it more than 1 time. And percentage
     * of getting successful ad is higher
     *
     * BASIC ALGORITHM:
     * Imagine we have 2 ads (Google and Ima)
     * 1: Request 1st iteration with ads in order that we have in JSONObject
     * 2: If Google have ERROR_RESULT, then immediately should request from Ima
     * 2.1: If Ima have SUCCESS_RESULT, then we save this Ima ad to phone cache
     * 2.2: If Ima also have ERROR_RESULT, then we should wait for the TIMEOUT and after that go to 2nd iteration in the 1st block
     * 2.3: Do the same stuff in 2) point. If all (COUNT) iterations are finished, then wait for the BLOCK_TIMEOUT and after that go to 2nd block
     * 3: If Google have SUCCESS_RESULT, then we save this Google ad to phone cache
     * 4: We have to do this until we don`t have SUCCESS_RESULT
     */

    fun startBackgroundRequests() {
        val activity = context as Activity
        val viewGroup = activity.findViewById(resId) as ViewGroup
        if(getReadyAd().isEmpty()){
            backgroundAdLogic(viewGroup)
        }else{
            Log.d(TAG, "startBackgroundRequests: ${getReadyAd()} is ready!")
        }
    }

    private fun backgroundAdLogic(viewGroup: ViewGroup) {
        Log.d(TAG, "backgroundAdLogic: started")
        var result = false
        // beginning of the block
        CoroutineScope(Dispatchers.Main).launch {
            // loop through iterations
            for (i in 0 until preload.count) {
                // loop through each ad in the iteration
                for(ad in adsList){
                    when(ad.type_identity){
                        AdTypeIdentity.Google.typeIdentity -> {
                            if(result){
                                this.cancel()
                            }else {
                                result = loadGoogleAd()
                            }
                        }
                        AdTypeIdentity.Hyperaudience.typeIdentity -> {
                            if(result){
                                this.cancel()
                            }else {
                                // TODO("result = loadHyperaudienceAd()")
                            }
                        }
                        AdTypeIdentity.Adriver.typeIdentity -> {
                            if(result){
                                this.cancel()
                            }else {
                                result = loadGoogleAd()
                                // TODO("result = loadAdriverAd()")
                            }
                        }
                        AdTypeIdentity.MyTarget.typeIdentity -> {
                            if(result){
                                this.cancel()
                            }else {
                                result = loadMyTarget()
                            }
                        }
                        AdTypeIdentity.VideoNow.typeIdentity -> {
                            if(result){
                                this.cancel()
                            }else {
                                // TODO("result = loadVideoNowAd()")
                            }
                        }
                    }
                }
                // should have timeout after each iteration
                delay(5000)
            }
            // should have block timeout after each block
            delay(5000)
            backgroundAdLogic(viewGroup)
        }
    }

}
