package tv.limehd.adsmodule

sealed class AdType(val typeSdk: String) {
    object MyTarget : AdType("mytarget")
    object Yandex : AdType("yandex")
    object Google : AdType("google")
    object IMA : AdType("ima")
    object IMADEVICE : AdType("ima-device")
}