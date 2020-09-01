package tv.limehd.adsmodule

sealed class AdType(val typeSdk: String) {
    class MyTarget : AdType("mytarget")
    class Yandex : AdType("yandex")
    class Google : AdType("google")
    class IMA : AdType("ima")
    class IMADEVICE : AdType("ima-device")
}
