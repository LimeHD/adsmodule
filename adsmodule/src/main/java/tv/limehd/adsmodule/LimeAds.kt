package tv.limehd.adsmodule

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import kotlinx.coroutines.Runnable
import org.json.JSONObject
import tv.limehd.adsmodule.adFox.AdFoxLoader
import tv.limehd.adsmodule.google.Google
import tv.limehd.adsmodule.ima.Ima
import tv.limehd.adsmodule.ima.vast2.ImaFragment
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState
import tv.limehd.adsmodule.model.*
import tv.limehd.adsmodule.myTarget.MyTarget
import tv.limehd.adsmodule.myTarget.MyTargetFragment

/**
 * Класс для работы с рекламой
 */

class LimeAds {

    private var viewGroup: ViewGroup? = null
    var mVideoView: VideoView? = null
    private var context: Context? = null
    var adUiContainer: ViewGroup? = null
    private var checkConnection: Boolean = true

    companion object {
        private const val TAG = "LimeAds"
        private lateinit var myTargetFragment: MyTargetFragment
        private lateinit var fragmentState: FragmentState
        var currentAdCounter = 1
        private var resId: Int = -1
        private var adsList = listOf<Ad>()
        private var limeAds: LimeAds? = null
        private lateinit var json: JSONObject
        private var adRequestListener: AdRequestListener? = null
        private var adShowListener: AdShowListener? = null
        private lateinit var fragmentManager: FragmentManager
        private var currentAdStatus: AdStatus = AdStatus.Online
        private val myTargetAdStatus: HashMap<String, Int> = HashMap()
        private val imaAdStatus: HashMap<String, Int> = HashMap()
        private val googleAdStatus: HashMap<String, Int> = HashMap()
        private lateinit var myTarget: MyTarget
        private lateinit var ima: Ima
        private lateinit var google: Google
        private lateinit var loadedAdStatusMap: HashMap<String, Int>
        private lateinit var interstitial: Interstitial
        private lateinit var preroll: Preroll
        private lateinit var preload: PreloadAds
        var prerollTimer = 0
        private var prerollEpgInterval = 0
        var userClicksCounter = 0
        private var skipFirst = true
        private var getAdFunCallAmount = 0
        lateinit var googleUnitId: String
        @JvmField
        var myTargetBlockId = -1
        private lateinit var backgroundAdManger: BackgroundAdManger
        @JvmField
        var isDisposeCalled: Boolean? = null
        @JvmField
        var isDisposeAdImaAd: Boolean? = null
        var isBackgroundRequestsCalled = false
        var mClientIp = ""
        var mAdvertisingId = ""

        @JvmStatic
        @Throws(NullPointerException::class)
        fun dispose() {
            Log.d(TAG, "dispose: called")
            isDisposeCalled = true

            isBackgroundRequestsCalled = false

            currentAdCounter = 1

            limeAds?.let {
                it.adUiContainer = null
                it.viewGroup = null
                it.mVideoView = null

                if(it.getReadyAd() == AdType.IMA.typeSdk) {
                    isDisposeAdImaAd = true
                    BackgroundAdManger.clearVariables()
                }
            } ?: throw NullPointerException(Constants.libraryIsNotInitExceptionMessage)

            try {
                fragmentManager.beginTransaction().remove(fragmentManager.fragments[1]).commitNowAllowingStateLoss()
            }catch (e: Exception) {
                Log.d(TAG, "dispose: ${e.message}")
            }

            limeAds?.context = null
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        fun checkConnectionStatus(checkConnection: Boolean) {
            limeAds?.let {
                it.checkConnection = checkConnection
            } ?: throw NullPointerException(Constants.libraryIsNotInitExceptionMessage)
        }

        /**
         * Function stands for requesting ad in the background while user
         * doing/watching some movie or something. See more information in
         * [BackgroundAdManger.startBackgroundRequests]
         */

        @JvmStatic
        @JvmOverloads
        @Throws(NullPointerException::class)
        fun startBackgroundRequests(context: Context, resId: Int, fragmentState: FragmentState, adRequestListener: AdRequestListener? = null, adShowListener: AdShowListener? = null) {

            requireNotNull(limeAds) {
                NullPointerException(Constants.libraryIsNotInitExceptionMessage)
            }

            isBackgroundRequestsCalled = true

            if(limeAds!!.checkConnection) {
                if(isConnectionSpeedEnough(context)){
                    myTargetFragment = MyTargetFragment(limeAds!!.lastAd, resId, fragmentState, adRequestListener, adShowListener, limeAds!!)
                    val fragmentActivity = context as FragmentActivity
                    fragmentManager = fragmentActivity.supportFragmentManager
                    if(!MyTargetFragment.isShowingAd){
                        fragmentManager.beginTransaction().replace(resId, myTargetFragment).commitAllowingStateLoss()
                        fragmentManager.beginTransaction().hide(myTargetFragment).commitAllowingStateLoss()
                    }
                    backgroundAdManger = BackgroundAdManger(context, resId, fragmentState, adShowListener, adRequestListener, preload, adsList, limeAds!!, fragmentManager, myTargetFragment)
                    backgroundAdManger.startBackgroundRequests()
                }else{
                    Log.d(TAG, "startBackgroundRequests: not called, cause of the internet")
                }
            }else {
                // do module job using handlers and timeouts
                Log.d(TAG, "getAd: do module job using handlers and timeouts")
            }
        }

        /**
         * Init LimeAds library
         *
         * @param   json    JSONObject which server gives to library to load ads
         */

        @JvmStatic
        @Throws(IllegalArgumentException::class)
        fun init(context: Context, json: JSONObject, application: Application) {
            if(json.isNull("ads") || json.isNull("ads_global") || json.getJSONArray("ads").length() == 0){
                throw IllegalArgumentException("JSONObject is empty!")
            }
            this.json = json
            limeAds = LimeAds()
            limeAds?.let {
                val gson = GsonBuilder().create()
                it.getAdsList(gson)
                it.getAdsGlobalModels(gson)
            }

            // Initializing the AppMetrica SDK
            Log.d(TAG, "init: AppMetrica SDK")
            val config: YandexMetricaConfig = YandexMetricaConfig.newConfigBuilder(context.getString(R.string.yandex_metrica_id)).build()
            YandexMetrica.activate(context, config)
            // Automatic tracking of user activity
            YandexMetrica.enableActivityAutoTracking(application)
        }

        /**
         * Load ad in correct order. That depends on the JSONObject.
         * Get current ad status (Online or Archive)
         * Get and Save isOnline and isArchive for each ad in JSONObject. Function checks
         * is ad allowed to request. Because of the timer in JSONObject -> preroll -> epg_timer. Also ad can be loaded if
         * user has clicked specific amount of times (JSONObject -> preroll -> epg_interval)
         * Another feature is that this function call background ad if these loaded. For more information check
         * this [BackgroundAdManger] class out
         */

        @JvmStatic
        @JvmOverloads
        @Throws(NullPointerException::class, IllegalArgumentException::class)
        fun getAd(context: Context,
                  resId: Int,
                  fragmentState: FragmentState,
                  isOnline: Boolean,
                  adRequestListener: AdRequestListener? = null,
                  adShowListener: AdShowListener? = null
        ) {

            requireNotNull(limeAds) {
                NullPointerException(Constants.libraryIsNotInitExceptionMessage)
            }

            // After this null check. limeAds variable is not null. So below limeAds?.... does not throw NPE exception

            limeAds?.context = context
            this.adRequestListener = adRequestListener
            this.adShowListener = adShowListener
            val activity = context as Activity
            limeAds?.adUiContainer = activity.findViewById(resId)
            limeAds?.viewGroup = activity.findViewById(resId)
            this.fragmentState = fragmentState
            val fragmentActivity = context as FragmentActivity
            if(!::fragmentManager.isInitialized){
                this.fragmentManager = fragmentActivity.supportFragmentManager
            }
            this.resId = resId

            if(!isBackgroundRequestsCalled) {
                myTargetFragment = MyTargetFragment(limeAds!!.lastAd, resId, fragmentState, adRequestListener, adShowListener, limeAds!!)
                fragmentManager = fragmentActivity.supportFragmentManager
                if (!MyTargetFragment.isShowingAd) {
                    fragmentManager.beginTransaction().replace(resId, myTargetFragment).commitAllowingStateLoss()
                    fragmentManager.beginTransaction().hide(myTargetFragment).commitAllowingStateLoss()
                }
            }

            if(!this::myTargetFragment.isInitialized) {
                myTargetFragment = MyTargetFragment(limeAds!!.lastAd, Companion.resId, Companion.fragmentState, Companion.adRequestListener, Companion.adShowListener, limeAds!!)
                fragmentManager.beginTransaction().replace(Companion.resId, myTargetFragment).commitAllowingStateLoss()
                fragmentManager.beginTransaction().hide(myTargetFragment).commitAllowingStateLoss()
            }

            limeAds?.let {
                it.getCurrentAdStatus(isOnline)
                it.populateAdStatusesHashMaps()
            }

            userClicksCounter++
            Log.d(TAG, "userClicks: $userClicksCounter")

            limeAds?.mVideoView = myTargetFragment.view?.findViewById(R.id.video_view)

            if(myTargetFragment.view != null) {
                BackgroundAdManger.myTargetFragmentFrameLayout = myTargetFragment.view?.findViewById(R.id.imaAdFrameLayout)
                limeAds?.mVideoView = myTargetFragment.view?.findViewById(R.id.video_view)
            }

            val readyBackgroundSkd = limeAds!!.getReadyAd()

            if(!limeAds?.isAllowedToRequestAd!! && userClicksCounter < 5){
                adRequestListener?.onEarlyRequest()
            }

            if(prerollTimer <= 0) {
                limeAds?.isAllowedToRequestAd = true
            }

            limeAds?.let {
                if ((it.isAllowedToRequestAd && prerollTimer <= 0) || userClicksCounter >= 5) {
                    if (skipFirst && getAdFunCallAmount == 0) {
                        Log.d(TAG, "getAd: skip first ad")
                        getAdFunCallAmount++
                        adRequestListener?.onEarlyRequest()
                    } else {
                        if(it.checkConnection) {
                            if(isConnectionSpeedEnough(context)) {
                                prerollTimer = preroll.epg_timer
                                it.prerollTimerHandler.removeCallbacks(it.prerollTimerRunnable)
                                it.isAllowedToRequestAd = false
                                userClicksCounter = 0
                                if(readyBackgroundSkd.isEmpty()){
                                    Log.d(TAG, "getAd: load ad in main thread")
                                    if(isDisposeCalled != null && isDisposeAdImaAd != null) {
                                        if (isDisposeCalled!! && isDisposeAdImaAd!!) {
                                            it.getGoogleAd()
                                        } else {
                                            when (adsList[0].type_sdk) {
                                                AdType.Google.typeSdk -> it.getGoogleAd()
                                                AdType.IMA.typeSdk -> it.getImaAd(adsList[0].url, adsList[0].type_identity)
                                                AdType.Yandex.typeSdk -> it.getAdFoxAd()
                                                AdType.MyTarget.typeSdk -> it.getMyTargetAd()
                                                AdType.IMADEVICE.typeSdk -> it.getImaDeviceAd()
                                                else -> Log.d(TAG, "getAd: else branch in when expression")
                                            }
                                        }
                                    }else {
                                        when (adsList[0].type_sdk) {
                                            AdType.Google.typeSdk -> it.getGoogleAd()
                                            AdType.IMA.typeSdk -> it.getImaAd(adsList[0].url, adsList[0].type_identity)
                                            AdType.Yandex.typeSdk -> it.getAdFoxAd()
                                            AdType.MyTarget.typeSdk -> it.getMyTargetAd()
                                            AdType.IMADEVICE.typeSdk -> it.getImaDeviceAd()
                                            else -> Log.d(TAG, "getAd: else branch in when expression")
                                        }
                                    }
                                }else {
                                    ReadyBackgroundAdDisplay(
                                        readyBackgroundSkd, limeAds?.viewGroup!!, adRequestListener, adShowListener,
                                        context, resId, fragmentState, limeAds!!, myTargetFragment, fragmentManager
                                    ).showReadyAd()
                                }
                            }else{
                                Log.d(TAG, "getAd: not called, cause of the internet")
                            }
                        }else {
                            // do module job using handlers and timeouts
                            Log.d(TAG, "getAd: do module job using handlers and timeouts")
                        }
                    }
                }else {
                    adRequestListener?.onEarlyRequest()
                }
            }
        }

        /**
         * Function returns TRUE, if library has been initialized
         * Function returns FALSE, if library has not been initialized
         *
         * @return Boolean
         */

        @JvmStatic
        fun isInitialized() : Boolean = limeAds != null

        /**
         * Show fragment with loaded ad
         * Starts displaying ad
         *
         * @param   fragment      Fragment on which library will show ad (MyTargetFragment, ImaFragment ...)
         */
        @JvmStatic
        fun showAd(fragment: MyTargetFragment, owner: AdType){
            fragmentManager.beginTransaction().show(fragment).commitAllowingStateLoss()
            fragment.initializePlaying(owner)
        }

        /**
         * Get only google interstitial ad
         *
         * When user exit from fullscreen mode, app should call google interstitial ad
         * If during interstitial -> timer (JSONObject from server) user exit from fullscreen more again,
         * library should not request google interstitial ad. Otherwise if timer is ended and user exit
         * fullscreen mode, then library should request google interstitial ad
         */

        @JvmStatic
        @Throws(NullPointerException::class)
        fun getGoogleInterstitialAd(context: Context, fragmentState: FragmentState, adRequestListener: AdRequestListener?, adShowListener: AdShowListener?) {
            requireNotNull(limeAds) {
                NullPointerException(Constants.libraryIsNotInitExceptionMessage)
            }
            // After this null check. limeAds variable is not null. So below limeAds?.... does not throw NPE exception
            with(limeAds!!) {
                if(this.isAllowedToRequestGoogleAd){
                    if(this.checkConnection) {
                        if(isConnectionSpeedEnough(context)) {
                            this.isAllowedToRequestGoogleAd = false
                            if (this.timer == 0) this.timer = 30
                            google = Google(context, lastAd, resId, fragmentState, adRequestListener, adShowListener, preroll, this)
                            google.getGoogleAd(true)
                        }else{
                            Log.d(TAG, "getGoogleInterstitialAd: not called, cause of the internet")
                        }
                    }else {
                        // do module job using handlers and timeouts
                        Log.d(TAG, "getAd: do module job using handlers and timeouts")
                    }
                }else {
                    adRequestListener?.onEarlyRequestInterstitial()
                }
            }
        }

        /**
         * Check internet connection speed
         * In case 0-100 kbps -> false
         * Otherwise true
         */

        private fun isConnectionSpeedEnough(context: Context) : Boolean {
            val currentDeviceConnectionType = Connectivity.getNetworkInfo(context).type
            val currentDeviceConnectionSubtype = Connectivity.getNetworkInfo(context).subtype
            if(!Connectivity.isConnectionFast(currentDeviceConnectionType, currentDeviceConnectionSubtype)) {
                return false
            }
            return true
        }

        @JvmStatic
        fun setClientIp(ip: String) {
            mClientIp = ip
        }

        @JvmStatic
        fun setAdvertisingId(id: String) {
            mAdvertisingId = id
        }

    }

    /**
     * Get already requested and cached ad type sdk name
     *
     * @return type sdk for ad that's ready
     */

    fun getReadyAd() : String {
        var readySdk = ""
        if(BackgroundAdManger.imaAdsManager != null){
            readySdk = AdType.IMA.typeSdk
        }
        if(BackgroundAdManger.myTargetInstreamAd != null){
            readySdk = AdType.MyTarget.typeSdk
        }
        if(BackgroundAdManger.googleInterstitialAd != null){
            readySdk = AdType.Google.typeSdk
        }
        if(BackgroundAdManger.mediaFile != null){
            readySdk = AdType.AdFox.typeSdk
        }
        return readySdk
    }

    private fun getCurrentAdStatus(isOnline: Boolean) {
        currentAdStatus = when(isOnline){
            true -> AdStatus.Online
            false -> AdStatus.Archive
        }
    }

    private fun populateAdStatusesHashMaps() {
        for(ad in adsList){
            val online = ad.is_onl
            val archive = ad.is_arh
            when(ad.type_sdk){
                AdType.MyTarget.typeSdk -> {
                    myTargetAdStatus[context!!.getString(R.string.isOnline)] = online
                    myTargetAdStatus[context!!.getString(R.string.isArchive)] = archive
                }
                AdType.IMA.typeSdk -> {
                    imaAdStatus[context!!.getString(R.string.isOnline)] = online
                    imaAdStatus[context!!.getString(R.string.isArchive)] = archive
                }
                AdType.Google.typeSdk -> {
                    googleAdStatus[context!!.getString(R.string.isOnline)] = online
                    googleAdStatus[context!!.getString(R.string.isArchive)] = archive
                }
            }
        }
    }

    /**
     * Get ads list from param JSONObject. This list
     * already has the correct order in which library will
     * load ad
     */

    private fun getAdsList(gson: Gson) {
        adsList = gson.fromJson(json.getJSONArray("ads").toString(), Array<Ad>::class.java).toList()
    }

    /**
     * Get ads_global models from JSONObject
     * preroll, preload_ads, yandex_min_api, interstitial
     */

    private fun getAdsGlobalModels(gson: Gson) {
        interstitial = gson.fromJson(json.getJSONObject("ads_global").getJSONObject("interstitial").toString(), Interstitial::class.java)
        preroll = gson.fromJson(json.getJSONObject("ads_global").getJSONObject("preroll").toString(), Preroll::class.java)
        preload = gson.fromJson(json.getJSONObject("ads_global").getJSONObject("preload_ads").toString(), PreloadAds::class.java)
        prerollEpgInterval = preroll.epg_interval
        skipFirst = preroll.skip_first
    }

    private val lastAd: String get() = adsList.last().type_sdk      // last ad type sdk in JSONObject

    /**
     * Получить/вызвать слудущию рекламу после currentAd
     *
     * @param   currentAd   реклама на которой сейчас произошла загрузка
     */

    fun getNextAd(currentAd: String) {
        var nextAd: String? = null
        var tagUrl: String? = null

        if(currentAdCounter == adsList.size){
            Log.d(TAG, "getNextAd: NOT LOADING NEXT AD, BECAUSE ALREADY ON THE LAST AD")
            context?.let {
                fragmentState.onErrorState(it.getString(R.string.no_ad_found_at_all))
            }
            limeAds?.isAllowedToRequestAd = true
            prerollTimer = 0

            limeAds?.adUiContainer?.visibility = View.GONE
            currentAdCounter = 1
        }else {
            currentAdCounter++
            for (i in adsList.indices) {
                if (i != adsList.size - 1 && adsList[i].type_identity == currentAd) {
                    nextAd = adsList[i + 1].type_identity
                    tagUrl = adsList[i + 1].url
                }
            }
            Log.d(TAG, "Next ad after '$currentAd' is '$nextAd'")
            when (nextAd) {
                AdTypeIdentity.Google.typeIdentity -> getGoogleAd()
                AdTypeIdentity.MyTarget.typeIdentity -> getMyTargetAd()
                AdTypeIdentity.Adriver.typeIdentity -> getImaAd(tagUrl, nextAd)
                AdTypeIdentity.Hyperaudience.typeIdentity -> getImaAd(tagUrl, nextAd)
                AdTypeIdentity.VideoNow.typeIdentity -> getImaAd(tagUrl, nextAd)
                AdTypeIdentity.AdFox.typeIdentity -> getAdFoxAd()
                null -> {
                    fragmentState.onErrorState(context!!.getString(R.string.no_ad_found_at_all))
                    limeAds?.isAllowedToRequestAd = true
                    prerollTimer = 0

                    limeAds?.adUiContainer?.visibility = View.GONE
                    currentAdCounter = 1
                    BackgroundAdManger.clearVariables()
                }
            }
        }
    }

    private fun getAdFoxAd() {
        Log.d(TAG, "getAdFoxAd: called")
        val pageId = "634713"
        val params = hashMapOf<String, String>().apply {
            this["p1"] = "cmilp"
            this["p2"] = "gmjh"
        }
        context?.let { context ->
            mVideoView?.let { videoView ->
                val adFoxLoader = AdFoxLoader(context, params, pageId, videoView, adRequestListener, adShowListener)
                adFoxLoader.loadVmap()
            } ?: adRequestListener?.onEarlyRequest()
        }
    }

    /**
     * Получить рекламу от площадки myTarget
     */

    @Throws(NullPointerException::class)
    private fun getMyTargetAd() {
        Log.d(TAG, "Load mytarget ad")

        context?.let { context ->
            viewGroup?.let { viewGroup ->
                myTarget = MyTarget(context, resId, myTargetFragment, fragmentManager, fragmentState, lastAd, adRequestListener, this, viewGroup)
            }
        } ?: throw NullPointerException("Context is null. Maybe you did not init library!")

        loadAd(AdType.MyTarget)
    }

    /**
     * Function stands for :
     * 1) If current ad status is online status, then we check isOnline value for current ad (JSONObject)
     *  1.1) If isOnline equals 1, then we load current ad
     *  1.2) Otherwise, if current ad is the last ad in the JSONObject -> ads array, then we throw exception.
     *       If current ad is not the last ad, then we load next ad after current one
     *
     *  @param  adType  Type of the current ad (IMA, MyTarget...)
     *  @param adStatus Status for the ad (Online or Archive)
     */

    private fun loadOrLoadNextOrThrowExceptionByAdStatus(adType: AdType, adStatus: String){
        if(loadedAdStatusMap[adStatus] == 1){
            Log.d(TAG, "$adStatus == 1, load ${adType.typeSdk}")
            when(adType){
                is AdType.IMA -> ima.loadAd()
                is AdType.MyTarget -> myTarget.loadAd()
                is AdType.Google -> google.getGoogleAd(false)
            }
        }else{
            Log.d(TAG, "$adStatus == 0, not loading ${adType.typeSdk}")
            if(lastAd == adType.typeSdk){
                fragmentState.onErrorState(context!!.resources.getString(R.string.no_ad_found_at_all), adType)
            }else {
                getNextAd(adType.typeSdk)
            }
        }
    }

    /**
     * Load ad base on AdType and AdStatus
     *
     * @param   adType  Type of the current ad (IMA, MyTarget...)
     */

    private fun loadAd(adType: AdType){
        when(adType){
            is AdType.IMA -> loadedAdStatusMap = imaAdStatus
            is AdType.MyTarget -> loadedAdStatusMap = myTargetAdStatus
            is AdType.Google -> loadedAdStatusMap = googleAdStatus
        }
        if(currentAdStatus == AdStatus.Online){
            loadOrLoadNextOrThrowExceptionByAdStatus(adType, context!!.getString(R.string.isOnline))
        }else if(currentAdStatus == AdStatus.Archive){
            loadOrLoadNextOrThrowExceptionByAdStatus(adType, context!!.getString(R.string.isArchive))
        }
    }

    /**
     * Получить рекламу от площадки IMA
     */

    @Throws(NullPointerException::class)
    private fun getImaAd(tagUrl: String?, adTypeIdentity: String) {
        Log.d(TAG, "Load IMA ad")
        if(myTargetFragment.view != null) {
            val imaAdContainer = myTargetFragment.view?.findViewById(R.id.imaAdFrameLayout) as FrameLayout
            if(tagUrl != null) {
                ima = Ima(context!!, tagUrl, lastAd, resId, imaAdContainer, fragmentState, adRequestListener, adShowListener, this, myTargetFragment, fragmentManager, adTypeIdentity)
            } else {
                ima = Ima(context!!, Constants.testAdTagUrl, lastAd, resId, imaAdContainer, fragmentState, adRequestListener, adShowListener, this, myTargetFragment, fragmentManager, adTypeIdentity)
            }
            loadAd(AdType.IMA)
        }else {
            Log.d(TAG, "getImaAd: MyTargetFragment view is null")
            limeAds?.isAllowedToRequestAd = true
            prerollTimer = 0
            BackgroundAdManger.clearVariables()
            adShowListener?.onComplete("MyTargetFragment view is null", AdType.IMA)
        }
    }

    /**
     * Получить рекламу для площадки Google
     */

    @Throws(NullPointerException::class)
    private fun getGoogleAd() {
        Log.d(TAG, "getGoogleAd: called")
        context?.let { context ->
            google = Google(context, lastAd, resId, fragmentState, adRequestListener, adShowListener, preroll, this)
        } ?: throw NullPointerException("Context is null. Maybe you did not init library!")

        loadAd(AdType.Google)
    }

    /**
     * Получить рекламу для площадки Yandex
     */

    private fun getYandexAd() {
        Log.d(TAG, "Load yandex ad")

        // If success then give AdFragment
        // Otherwise, onNoAd callback will be occurred

        Log.d(TAG, "YandexAd onNoAd called")

        if(lastAd == AdType.Yandex.typeSdk){
            fragmentState.onErrorState(context!!.resources.getString(R.string.no_ad_found_at_all), AdType.Yandex)
        }else {
            getNextAd(AdType.Yandex.typeSdk)
        }
    }

    private fun getImaDeviceAd() {
        Log.d(TAG, "Load Ima-Device ad")

        // If success then give AdFragment
        // Otherwise, onNoAd callback will be occurred

        Log.d(TAG, "Ima-Device onNoAd called")

        if(lastAd == AdType.IMADEVICE.typeSdk){
            fragmentState.onErrorState(context!!.resources.getString(R.string.no_ad_found_at_all), AdType.IMADEVICE)
        }else {
            getNextAd(AdType.IMADEVICE.typeSdk)
        }
    }

    //********************************************* GOOGLE INTERSTITIAL TIMER HANDLER ****************************************************** //

    val googleTimerHandler: Handler = Handler()
    var timer = 30
    var isAllowedToRequestGoogleAd = true
    val googleTimerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (timer > 0) {
                timer--
                Log.d(TAG, "Google timer: $timer")
                googleTimerHandler.postDelayed(this, 1000)
            }else{
                isAllowedToRequestGoogleAd = true
            }
        }
    }

    //********************************************* PREROLL TIMER HANDLER ****************************************************** //

    val prerollTimerHandler: Handler = Handler()
    var isAllowedToRequestAd = true
    val prerollTimerRunnable: Runnable = object : Runnable {
        override fun run() {
            if (prerollTimer > 0) {
                prerollTimer--
                Log.d(TAG, "Preroll timer: $prerollTimer")
                prerollTimerHandler.postDelayed(this, 1000)
            }else{
                isAllowedToRequestAd = true
            }
        }
    }

}

