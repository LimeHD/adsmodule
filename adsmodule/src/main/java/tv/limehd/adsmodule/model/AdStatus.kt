package tv.limehd.adsmodule.model

sealed class AdStatus {

    object Online : AdStatus()
    object Archive : AdStatus()

}