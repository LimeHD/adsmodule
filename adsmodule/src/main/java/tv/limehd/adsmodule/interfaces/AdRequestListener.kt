package tv.limehd.adsmodule.interfaces

import tv.limehd.adsmodule.AdType

/**
 * Callback for loading ad. Used for showing application what
 * is going on with AD LOADING PROCESS
 */

interface AdRequestListener {

    fun onRequest(message: String, owner: AdType)

    fun onLoaded(message: String, owner: AdType)

    fun onError(message: String, owner: AdType)

    fun onNoAd(message: String, owner: AdType)

    fun onEarlyRequest()

    fun onEarlyRequestInterstitial()

}