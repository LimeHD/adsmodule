package tv.limehd.adsmodule.google

import android.content.Context
import android.util.Log
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState
import tv.limehd.adsmodule.model.Preroll

/**
 * Class with GoogleAd logic
 * For better comprehension all business logic will be
 * put right here, but not in the LimeAds class
 * See also the following classes
 * [tv.limehd.adsmodule.myTarget.MyTarget]
 * [tv.limehd.adsmodule.ima.Ima]
 */

class Google(private val context: Context,
             private val lastAd: String,
             private val resId: Int,
             private val fragmentState: FragmentState,
             private val adRequestListener: AdRequestListener?,
             private val adShowListener: AdShowListener?,
             private val preroll: Preroll,
             private val limeAds: LimeAds) {

    companion object {
        private const val TAG = "Google"
    }

    /**
     * Получить рекламу для площадки Google
     */

    fun getGoogleAd(isLoadInterstitial: Boolean) {
        Log.d(TAG, "Load google ad")
        val googleLoader = GoogleLoader(context, lastAd, resId, fragmentState, adRequestListener, adShowListener, isLoadInterstitial, preroll, limeAds)
        googleLoader.loadAd()
    }
}