package com.example.ui

import com.example.data.local.AppLanguage

/** Shared copy for secondary screens that are not part of the settings/home copy model. */
object LocalizedCopy {
    fun get(language: AppLanguage, key: String): String {
        if (language == AppLanguage.TURKISH) {
            return mapOf(
                "history" to "Park Geçmişi",
                "noHistory" to "Geçmiş Park Kaydı Yok",
                "historyDescription" to "Önceki park konumlarınız burada çevrimdışı görünecek.",
                "active" to "AKTİF",
                "delete" to "Sil",
                "clearSpot" to "Park Konumu Temizlensin mi?",
                "clearDescription" to "Aracınıza geri mi döndünüz? Aktif park konumu temizlenir ve bir sonraki sefer yeni konum kaydedebilirsiniz.",
                "back" to "Geri",
                "yesBack" to "Evet, Döndüm",
                "route" to "Rota",
                "editSpot" to "Konumu Düzenle",
                "clearLocation" to "Konumu Temizle",
                "tapToEnlarge" to "Büyütmek için dokunun",
                "enableLocation" to "Canlı pusula yönlendirmesi için konumu aç"
            )[key] ?: key
        }
        return mapOf(
            "history" to "Parking History",
            "noHistory" to "No Past Parking Records",
            "historyDescription" to "Your previous parking locations will appear here offline.",
            "active" to "ACTIVE",
            "delete" to "Delete",
            "clearSpot" to "Clear Parking Spot?",
            "clearDescription" to "Are you back at your vehicle? This will clear the active parking spot so you can save a new one next time.",
            "back" to "Back",
            "yesBack" to "Yes, I'm Back",
            "route" to "Route",
            "editSpot" to "Edit Spot",
            "clearLocation" to "Clear Location",
            "tapToEnlarge" to "Tap to enlarge",
            "enableLocation" to "Enable Location for Live Compass Guidance"
        )[key] ?: key
    }
}
