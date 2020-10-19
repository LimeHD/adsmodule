package tv.limehd.adsmodule

import org.json.JSONObject

/**
 * Класс со вссеми нужными и ненужными константами
 */

class Constants {

    companion object {
        const val json = "{\"ads\": [\n" +
                "        {\n" +
                "            \"id\": 4,\n" +
                "            \"url\": \"\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"google\",\n" +
                "            \"type_identity\": \"googleinterstitial\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": true,\n" +
                "            \"window\": 99\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 25,\n" +
                "            \"url\": \"\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"mytarget\",\n" +
                "            \"type_identity\": \"MyTarget\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": true,\n" +
                "            \"window\": 99\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 6,\n" +
                "            \"url\": \"https://data.videonow.ru/?profile_id=2392203&format=vast&container=preroll\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"ima-device\",\n" +
                "            \"type_identity\": \"videonow\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": false,\n" +
                "            \"window\": 99\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 26,\n" +
                "            \"url\": \"https://ads.hyperadx.com/v2/vast/b3Qn1BKM?v=3.0&device.ua=com.infolink.limeiptv&device.ip=85.234.0.52&device.ifa=81d24299-dccb-47e0-a4c5-fcc9ecdeae11&app.bundle=com.infolink.limeiptv&app.cat=intertainment&app.name=\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"ima\",\n" +
                "            \"type_identity\": \"hyperaudience\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": true,\n" +
                "            \"window\": 99\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 7,\n" +
                "            \"url\": \"https://adddd.adriver.ru/cgi-bin/rle.cgi?sid=216288&bt=61&pz=0&target=blank&tuid=1&rnd=1484434468\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"ima\",\n" +
                "            \"type_identity\": \"adriver\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": true,\n" +
                "            \"window\": 99\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": 100,\n" +
                "            \"url\": \"\",\n" +
                "            \"is_onl\": 1,\n" +
                "            \"is_arh\": 1,\n" +
                "            \"type_sdk\": \"yandex\",\n" +
                "            \"type_identity\": \"adFox\",\n" +
                "            \"type_block\": 10,\n" +
                "            \"type_device\": 10,\n" +
                "            \"orientation\": 10,\n" +
                "            \"code\": \"\",\n" +
                "            \"enable_cache\": true,\n" +
                "            \"window\": 99\n" +
                "        }\n" +
                "    ]," +
                "    \"ads_global\": {\n" +
                "        \"preroll\": {\n" +
                "            \"enabled\": true,\n" +
                "            \"epg_interval\": 1000,\n" +
                "            \"epg_timer\": 30,\n" +
                "            \"skip_first\": true,\n" +
                "            \"skip_count\": \"1\",\n" +
                "            \"tv_mode\": true,\n" +
                "            \"skip_timer\": 10\n" +
                "        },\n" +
                "        \"preload_ads\": {\n" +
                "            \"timeout\": 1200,\n" +
                "            \"count\": 2,\n" +
                "            \"block_timeout\": 1200\n" +
                "        },\n" +
                "        \"yandex_min_api\": 19,\n" +
                "        \"interstitial\": {\n" +
                "            \"timer\": \"1200\",\n" +
                "            \"interval\": \"1\",\n" +
                "            \"show_skip\": true\n" +
                "        }\n" +
                "    }" +
                "}"

        var TIMEOUT = 3000f
        const val testAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="
        const val libraryIsNotInitExceptionMessage = "You have not initialized LimeAds. Please, do the following: LimeAds.init(...)"
    }

    val jsonObject = JSONObject(json)

}
