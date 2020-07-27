package tv.limehd.adsmodule.ima.vast3

import android.content.Context
import android.net.Uri
import androidx.fragment.app.FragmentManager
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import tv.limehd.adsmodule.R
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.AdShowListener
import tv.limehd.adsmodule.interfaces.FragmentState

class ImaVast3Loader(
    private val context: Context,
    private val adRequestListener: AdRequestListener?,
    private val adShowListener: AdShowListener?,
    private val fragmentState: FragmentState,
    private val resId: Int,
    private val imaVast3Fragment: ImaVast3Fragment
) {

    companion object {
        private const val TAG = "ImaVast3Loader"
    }

    private var adsLoader: ImaAdsLoader = ImaAdsLoader(context, Uri.parse("https://exchange.buzzoola.com/adv/kbDH64c7yFY_jqB7YcKn5Fe1xALB2bNgjXr1P_8yfXuCZKsWdzlR9A/vast2"))
    private var player: SimpleExoPlayer? = null

//    private var playerView = imaVast3Fragment.view?.findViewById(R.id.player_view) as PlayerView

    private fun releasePlayer() {
        adsLoader.setPlayer(null)
        imaVast3Fragment.playerView?.player = null
        player?.release()
        player = null
    }

    fun initPlayer() {
        player = SimpleExoPlayer.Builder(context).build()
        imaVast3Fragment.playerView?.player = player
        adsLoader.setPlayer(player)
        val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getString(R.string.app_name)))
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource = mediaSourceFactory.createMediaSource(Uri.parse("https://exchange.buzzoola.com/adv/kbDH64c7yFY_jqB7YcKn5Fe1xALB2bNgjXr1P_8yfXuCZKsWdzlR9A/vast2"))
        val adsMediaSource = AdsMediaSource(mediaSource, dataSourceFactory, adsLoader, imaVast3Fragment.playerView)
        player?.prepare(adsMediaSource)
        player?.playWhenReady = true
    }

}