package tv.limehd.adsmodule.model

data class Preroll(
    val enabled: Boolean,
    val epg_interval: Int,
    val epg_timer: Int,
    val skip_first: Boolean,
    val skip_count: String,
    val tv_mode: Boolean,
    val skip_timer: Int
)