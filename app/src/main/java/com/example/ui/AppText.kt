package com.example.ui

import com.example.data.local.AppLanguage

/** Small UI copy layer so language changes recompose instantly without restarting the activity. */
data class AppText(
    val settings: String,
    val preferences: String,
    val language: String,
    val english: String,
    val turkish: String,
    val appearance: String,
    val distanceUnits: String,
    val metric: String,
    val imperial: String,
    val haptics: String,
    val hapticsDescription: String,
    val privacy: String,
    val localData: String,
    val eraseData: String,
    val eraseTitle: String,
    val eraseDescription: String,
    val eraseEverything: String,
    val cancel: String,
    val carQuestion: String,
    val carDescription: String,
    val parkedHere: String,
    val saveHint: String,
    val replaceTitle: String,
    val replaceDescription: String,
    val saveNewSpot: String
) {
    companion object {
        fun forLanguage(language: AppLanguage) = if (language == AppLanguage.TURKISH) {
            AppText(
                "Ayarlar", "Tercihler", "Dil", "İngilizce", "Türkçe", "Görünüm", "Mesafe Birimleri",
                "Metrik (m / km)", "İngiliz (ft / mil)", "Titreşimli Geri Bildirim",
                "İşlemlerde ve pusula yönünde hafifçe titreştir", "Gizlilik ve Çevrimdışı Yapı",
                "Park verileriniz cihazınızda kalır.", "Kayıtlı Verileri Sil", "Tüm Park Verileri Silinsin mi?",
                "Bu işlem cihazdaki tüm aktif ve geçmiş park kayıtlarını ve fotoğrafları kalıcı olarak siler. Geri alınamaz.",
                "Her Şeyi Sil", "İptal", "Arabanız nerede?",
                "Tam konumunuzu, katınızı ve fotoğrafınızı çevrimdışı hatırlamak için bir kez dokunun.",
                "Buraya Park Ettim", "Anında GPS ve çevrimdışı kayıt", "Mevcut Konum Değiştirilsin mi?",
                "Zaten aktif bir park konumunuz var. Yeni konum kaydedilirse mevcut konum güncellenir.", "Yeni Konum Kaydet"
            )
        } else {
            AppText(
                "Settings", "Preferences", "Language", "English", "Turkish", "Appearance Theme", "Distance Units",
                "Metric (m / km)", "Imperial (ft / mi)", "Haptic Feedback",
                "Vibrate gently on actions and compass heading", "Privacy & Offline Architecture",
                "Your parking data stays on your device.", "Erase All Stored Data", "Erase All Parking Data?",
                "This will permanently remove all active and historical parking records and photos from this device. This cannot be undone.",
                "Erase Everything", "Cancel", "Where's your car?",
                "Tap once to remember your exact spot, floor, and photo offline.",
                "I Parked Here", "Instant GPS & offline spot saving", "Replace Current Spot?",
                "You already have an active parking spot saved. Saving a new location will update your active spot.", "Save New Spot"
            )
        }
    }
}
