package tv.limehd.adsmodule.model

/**
 *  This class stands for the ad model
 *  Like MyTarget, Ima, Yandex or Google ad
 */

data class Ad(
    val id: Int,
    val url: String,
    val is_onl: Int,
    val is_arh: Int,
    val type_sdk: String,
    val type_identity: String,
    val type_block: Int,
    val type_device: Int,
    val orientation: Int,
    val code: String,
    val enable_cache: Boolean,
    val window: Int
)