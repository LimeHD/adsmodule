package tv.limehd.adsmodule.myTarget

import android.content.Context
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.my.target.instreamads.InstreamAd
import tv.limehd.adsmodule.AdType
import tv.limehd.adsmodule.LimeAds
import tv.limehd.adsmodule.R
import tv.limehd.adsmodule.interfaces.AdLoader
import tv.limehd.adsmodule.interfaces.AdRequestListener
import tv.limehd.adsmodule.interfaces.FragmentState

/**
 * Class with MyTargetAd logic
 * For better comprehension all business logic will be
 * put right here, but not in the LimeAds class
 * See also the following classes
 * [tv.limehd.adsmodule.ima.Ima]
 * [tv.limehd.adsmodule.google.Google]
 */

class MyTarget(
    private val context: Context,
    private val resId: Int,
    private val myTargetFragment: MyTargetFragment,
    private val fragmentManager: FragmentManager,
    private val fragmentState: FragmentState,
    private val lastAd: String,
    private val adRequestListener: AdRequestListener?,
    private val limeAds: LimeAds
) {

    companion object {
        private const val TAG = "MyTarget"
    }

    fun loadAd() {
        val myTargetLoader = MyTargetLoader(context)
        fragmentManager.beginTransaction().replace(resId, myTargetFragment).commit()
        adRequestListener?.onRequest(context.getString(R.string.requested), AdType.MyTarget)
        myTargetLoader.loadAd()
        myTargetLoader.setAdLoader(object : AdLoader {
            override fun onRequest() {
                adRequestListener?.onRequest(context.getString(R.string.requested), AdType.MyTarget)
            }

            override fun onLoaded(instreamAd: InstreamAd) {
                adRequestListener?.onLoaded(context.getString(R.string.loaded), AdType.MyTarget)
                myTargetFragment.setInstreamAd(instreamAd)
                fragmentState.onSuccessState(myTargetFragment, AdType.MyTarget)
            }

            override fun onError(error: String) {
                adRequestListener?.onError(error, AdType.MyTarget)
            }

            override fun onNoAd(error: String) {
                Log.d(TAG, "MyTarget onNoAd called $error")
                adRequestListener?.onNoAd(error, AdType.MyTarget)
                fragmentManager.beginTransaction().remove(myTargetFragment).commit()
                if(lastAd == AdType.MyTarget.typeSdk){
                    fragmentState.onErrorState(context.resources.getString(R.string.no_ad_found_at_all), AdType.MyTarget)
                }else {
                    limeAds.getNextAd(AdType.MyTarget.typeSdk)
                }
            }
        })
    }

}