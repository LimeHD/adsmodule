package tv.limehd.adsmodule

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
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
                LimeAds.isDisposeCalled?.let {
                    if (it) {
                        BackgroundAdManger.clearVariables()
                        limeAds.prerollTimerHandler.removeCallbacks(limeAds.prerollTimerRunnable)
                        LimeAds.isDisposeAdImaAd = false
                        LimeAds.isDisposeCalled = false
                        LimeAds.prerollTimer = 0
                        LimeAds.userClicksCounter = 0
                        limeAds.isAllowedToRequestAd = true
                        LimeAds.getAd(
                            context,
                            resId,
                            fragmentState,
                            true,
                            adRequestListener,
                            adShowListener
                        )
                    } else {
                        viewGroup.visibility = View.VISIBLE
                        val adsManager = BackgroundAdManger.imaAdsManager
                        if (adsManager != null) {
                            myTargetFragment.setAdsManager(adsManager)
                            fragmentState.onSuccessState(myTargetFragment, AdType.IMA)
                        } else {
                            adRequestListener?.onEarlyRequest()
                        }
                    }
                }
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
            AdType.AdFox.typeSdk -> {
                // show adFox ad
                Log.d(TAG, "showReadyAd: show AD FOX from background")
                viewGroup.visibility = View.VISIBLE
                val mediaFile = BackgroundAdManger.mediaFile
                if(mediaFile != null) {
                    myTargetFragment.setMediaFile(mediaFile)
                    fragmentManager.beginTransaction().show(myTargetFragment).commitAllowingStateLoss()
                    fragmentState.onSuccessState(myTargetFragment, AdType.AdFox)
                }else{
                    adRequestListener?.onEarlyRequest()
                }
            }
        }
    }

}
