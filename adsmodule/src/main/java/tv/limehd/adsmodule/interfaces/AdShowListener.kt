package tv.limehd.adsmodule.interfaces

import tv.limehd.adsmodule.AdType

/**
 * Callback for showing ad. Used for showing application what
 * is going on with AD SHOWING PROCESS
 */

interface AdShowListener {

    fun onShow(message: String, owner: AdType)

    fun onError(message: String, owner: AdType)

    fun onComplete(message: String, owner: AdType)

    fun onSkip(message: String, owner: AdType)

    fun onClick(message: String, owner: AdType)

    fun onCompleteInterstitial()
}