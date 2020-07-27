package tv.limehd.adsmodule.ima

import android.content.Context
import android.view.ViewGroup
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.ima.vast2.ImaLoader
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState

/**
 * Class with ImaAd logic
 * For better comprehension all business logic will be
 * put right here, but not in the LimeAds class
 *
 * See also the following classes
 * [tv.limehd.adsmodule.myTarget.MyTarget]
 * [tv.limehd.adsmodule.google.Google]
 */

class Ima(private val context: Context,
          private val adTagUrl: String,
          private val lastAd: String,
          private val resId: Int,
          private val viewGroup: ViewGroup,
          private val fragmentState: FragmentState,
          private val adRequestListener: AdRequestListener?,
          private val adShowListener: AdShowListener?,
          private val limeAds: LimeAds
) {

    fun loadAd() {
        val imaLoader = ImaLoader(
            context,
            lastAd,
            adTagUrl,
            resId,
            viewGroup,
            adRequestListener,
            adShowListener,
            limeAds
        )
        imaLoader.loadImaAd(fragmentState)
    }

}