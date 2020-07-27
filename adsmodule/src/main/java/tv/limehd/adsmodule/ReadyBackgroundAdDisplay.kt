package tv.limehd.adsmodule

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.ads.interactivemedia.v3.api.AdEvent
import tv.limehd.adsmodule.ima.vast2.ImaFragment
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState
import tv.limehd.adsmodule.myTarget.MyTargetFragment

/**
 * Class for displaying background ad that already cached
 * in the UI
 * Class will understand what ad is ready to be displayed
 * and than get this ad from the cache, show it to the user
 */

class ReadyBackgroundAdDisplay(
    private val readyBackgroundSkd: String,
    private val viewGroup: ViewGroup,
    private val adRequestListener: AdRequestListener?,
    private val adShowListener: AdShowListener?,
    private val context: Context,
    private val resId: Int,
    private val fragmentState: FragmentState,
    private val limeAds: LimeAds,
    private val myTargetFragment: MyTargetFragment,
    private val fragmentManager: FragmentManager
) {

    companion object {
        private const val TAG = "ReadyBGAdDisplay"
    }

    /**
     * Show ad from background
     */

    fun showReadyAd() {
        when(readyBackgroundSkd){
            AdType.IMA.typeSdk -> {
                // show ima ad
                Log.d(TAG, "getAd: show ima from background")
                viewGroup.visibility = View.VISIBLE
                val adsManager = BackgroundAdManger.imaAdsManager
                adsManager?.addAdEventListener { adEvent ->
                    when(adEvent.type){
                        AdEvent.AdEventType.LOADED -> {
                            adRequestListener?.onLoaded(context.getString(R.string.loaded), AdType.IMA)
                        }
                        AdEvent.AdEventType.SKIPPED -> {
                            adShowListener?.onSkip(context.getString(R.string.skipped), AdType.IMA)
                        }
                        AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                            adShowListener?.onComplete(context.getString(R.string.completed), AdType.IMA)

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
                        }
                        AdEvent.AdEventType.STARTED -> {
                            adShowListener?.onShow(context.getString(R.string.showing), AdType.IMA)
                        }

                        AdEvent.AdEventType.TAPPED -> {
                            adShowListener?.onClick(context.getString(R.string.clicked), AdType.IMA)
                        }
                    }
                }
                adsManager!!.init()
                val imaFragment =
                    ImaFragment(adsManager)
                fragmentState.onSuccessState(imaFragment, AdType.IMA)
            }
            AdType.MyTarget.typeSdk -> {
                // show mytarget ad
                Log.d(TAG, "getAd: show mytarget from background")
                viewGroup.visibility = View.VISIBLE
                val instreamAd = BackgroundAdManger.myTargetInstreamAd
                if(instreamAd != null) {
                    myTargetFragment.setInstreamAd(instreamAd)
                    fragmentManager.beginTransaction().show(myTargetFragment).commitAllowingStateLoss()
                    fragmentState.onSuccessState(myTargetFragment, AdType.MyTarget)
                }else{
                    adRequestListener?.onEarlyRequest()
                }
            }
            AdType.Google.typeSdk -> {
                // show google ad
                Log.d(TAG, "getAd: show google from background")
                val interstitial = BackgroundAdManger.googleInterstitialAd
                interstitial!!.show()
            }
        }
    }

}