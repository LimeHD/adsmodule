package tv.limehd.adsmodule

sealed class AdTypeIdentity(val typeIdentity: String) {

    object Google : AdTypeIdentity("googleinterstitial")
    object MyTarget : AdTypeIdentity("MyTarget")
    object VideoNow : AdTypeIdentity("videonow")
    object Hyperaudience : AdTypeIdentity("hyperaudience")
    object Adriver : AdTypeIdentity("adriver")
    object AdFox : AdTypeIdentity("adFox")

}