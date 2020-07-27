package tv.limehd.adsmodule.ima.vast2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.ads.interactivemedia.v3.api.AdsManager
import tv.limehd.adsmodule.R

class ImaFragment(private val adsManager: AdsManager) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ima, container, false)
    }

    fun initializePlaying() {
        adsManager.start()
    }

}