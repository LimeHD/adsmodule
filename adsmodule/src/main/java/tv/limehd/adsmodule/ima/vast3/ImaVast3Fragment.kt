package tv.limehd.adsmodule.ima.vast3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ui.PlayerView
import tv.limehd.adsmodule.R

class ImaVast3Fragment() : Fragment() {

    var playerView: PlayerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        playerView = view?.findViewById(R.id.player_view) as PlayerView?
        return inflater.inflate(R.layout.fragment_ima_vast3, container, false)
    }

}