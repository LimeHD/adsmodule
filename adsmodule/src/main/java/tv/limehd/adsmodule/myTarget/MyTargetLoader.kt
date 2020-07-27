package tv.limehd.adsmodule.myTarget

import android.content.Context
import com.my.target.instreamads.InstreamAd
import tv.limehd.adsmodule.Constants.Companion.TIMEOUT
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.interfaces.AdLoader

/**
 * This class stands for loading MyTarget ads logic
 */

class MyTargetLoader constructor(private val context: Context) : InstreamAd.InstreamAdListener {

    private lateinit var adLoader: AdLoader
//  private val slotId = 621422 // in production
    private val slotId = LimeAds.myTargetBlockId   // for testing
//    private val slotId = 1010 // error

    fun setAdLoader(adLoader: AdLoader){
        this.adLoader = adLoader
    }

    fun loadAd() {
        val instreamAd = InstreamAd(slotId, context)
        instreamAd.loadingTimeout = TIMEOUT.toInt() / 1000
        instreamAd.listener = this
        instreamAd.load()
    }

    override fun onLoad(instreamAd: InstreamAd) {
        adLoader.onLoaded(instreamAd)
    }

    override fun onComplete(p0: String, p1: InstreamAd) {
        TODO("Not yet implemented")
    }

    override fun onBannerPause(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
        TODO("Not yet implemented")
    }

    override fun onBannerStart(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
        TODO("Not yet implemented")
    }

    override fun onNoAd(error: String, p1: InstreamAd) {
        adLoader.onNoAd(error)
    }

    override fun onBannerResume(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
        TODO("Not yet implemented")
    }

    override fun onBannerTimeLeftChange(p0: Float, p1: Float, p2: InstreamAd) {
        TODO("Not yet implemented")
    }

    override fun onError(error: String, p1: InstreamAd) {
        adLoader.onError(error)
    }

    override fun onBannerComplete(p0: InstreamAd, p1: InstreamAd.InstreamAdBanner) {
        TODO("Not yet implemented")
    }

}