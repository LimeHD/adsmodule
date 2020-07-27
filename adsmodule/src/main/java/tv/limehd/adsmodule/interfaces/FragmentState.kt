package tv.limehd.adsmodule.interfaces

import androidx.fragment.app.Fragment
import tv.limehd.adsmodule.AdType

interface FragmentState {

    fun onSuccessState(fragment: Fragment, owner: AdType)
    fun onErrorState(message: String, owner: AdType)

}