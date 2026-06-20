package com.freshmart.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;

public class MapPickerActivity extends AppCompatActivity {

    private WebView mapView;
    private TextView addressText;
    private Button confirmButton;

    private String selectedAddress = "";
    private double selectedLat    = 0;
    private double selectedLng    = 0;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildLayout());

        WebSettings ws = mapView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);

        mapView.addJavascriptInterface(new MapBridge(this), "Android");
        mapView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return false;
            }
        });

        mapView.loadDataWithBaseURL(
            "https://unpkg.com",
            buildMapHtml(),
            "text/html", "UTF-8", null
        );
    }

    @Override
    protected void onDestroy() {
        if (mapView != null) {
            mapView.removeAllViews();
            mapView.destroy();
        }
        super.onDestroy();
    }

    // ── static inner class избегает утечки памяти ─────────────
    static class MapBridge {
        private final WeakReference<MapPickerActivity> ref;
        MapBridge(MapPickerActivity activity) { ref = new WeakReference<>(activity); }

        @JavascriptInterface
        public void onLocationSelected(double lat, double lng, String address) {
            MapPickerActivity act = ref.get();
            if (act == null || act.isFinishing()) return;
            act.selectedLat     = lat;
            act.selectedLng     = lng;
            act.selectedAddress = address;
            act.runOnUiThread(() -> {
                act.addressText.setText("📍 " + address);
                act.confirmButton.setEnabled(true);
                act.confirmButton.setAlpha(1f);
            });
        }
    }

    // ── Программный layout ────────────────────────────────────
    private View buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF5F7F5);

        // Шапка
        LinearLayout header = new LinearLayout(this);
        header.setBackgroundColor(0xFFFFFFFF);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(14), dp(12), dp(14));
        header.setElevation(dp(4));

        TextView backBtn = new TextView(this);
        backBtn.setText("←");
        backBtn.setTextSize(22f);
        backBtn.setTextColor(0xFF2D7A3A);
        backBtn.setPadding(0, 0, dp(14), 0);
        backBtn.setOnClickListener(v -> finish());
        header.addView(backBtn);

        TextView title = new TextView(this);
        title.setText("Выберите адрес на карте");
        title.setTextSize(16f);
        title.setTextColor(0xFF1A1A1A);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        root.addView(header, matchWrap());

        // Подсказка
        TextView hint = new TextView(this);
        hint.setText("Нажмите на карту для выбора точки доставки");
        hint.setTextSize(12f);
        hint.setTextColor(0xFF2D7A3A);
        hint.setGravity(Gravity.CENTER);
        hint.setBackgroundColor(0xFFE8F5E9);
        hint.setPadding(dp(16), dp(9), dp(16), dp(9));
        root.addView(hint, matchWrap());

        // Карта
        mapView = new WebView(this);
        LinearLayout.LayoutParams mapLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(mapView, mapLp);

        // Нижняя панель
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setBackgroundColor(0xFFFFFFFF);
        bottom.setPadding(dp(16), dp(14), dp(16), dp(24));
        bottom.setElevation(dp(8));

        addressText = new TextView(this);
        addressText.setText("📍 Нажмите на карту для выбора адреса");
        addressText.setTextSize(13f);
        addressText.setTextColor(0xFF555555);
        addressText.setMinLines(2);
        addressText.setPadding(0, 0, 0, dp(14));
        bottom.addView(addressText, matchWrap());

        confirmButton = new Button(this);
        confirmButton.setText("Подтвердить адрес ✓");
        confirmButton.setTextColor(Color.WHITE);
        confirmButton.setTextSize(15f);
        confirmButton.setTypeface(null, Typeface.BOLD);
        confirmButton.setBackgroundColor(0xFF2D7A3A);
        confirmButton.setEnabled(false);
        confirmButton.setAlpha(0.45f);
        confirmButton.setOnClickListener(v -> confirmAddress());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        bottom.addView(confirmButton, btnLp);

        root.addView(bottom, matchWrap());
        return root;
    }

    private void confirmAddress() {
        Intent result = new Intent();
        result.putExtra("address", selectedAddress);
        result.putExtra("lat",     selectedLat);
        result.putExtra("lng",     selectedLng);
        setResult(RESULT_OK, result);
        finish();
    }

    // ── OpenStreetMap + Leaflet (без внешнего CDN в baseURL) ─
    private String buildMapHtml() {
        return "<!DOCTYPE html><html><head>"
            + "<meta charset='UTF-8'>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no'>"
            + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
            + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
            + "<style>*{margin:0;padding:0;box-sizing:border-box}html,body,#map{width:100%;height:100vh;overflow:hidden}</style>"
            + "</head><body>"
            + "<div id='map'></div>"
            + "<script>"
            // Центр Москвы по умолчанию
            + "var map=L.map('map',{zoomControl:true}).setView([55.7558,37.6173],11);"
            + "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{"
            +   "maxZoom:19,attribution:'© OpenStreetMap'}).addTo(map);"
            + "var pin=null;"
            + "map.on('click',function(e){"
            +   "var lat=e.latlng.lat.toFixed(6),lng=e.latlng.lng.toFixed(6);"
            +   "if(pin)map.removeLayer(pin);"
            +   "pin=L.marker([lat,lng]).addTo(map);"
            +   "pin.bindPopup('Определяем адрес…').openPopup();"
            +   "fetch('https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat='+lat+'&lon='+lng+'&accept-language=ru',{"
            +     "headers:{'User-Agent':'FreshMartApp/1.0'}"
            +   "}).then(function(r){return r.json();}).then(function(d){"
            +     "var a=d.address||{};"
            +     "var parts=[];"
            +     "var city=a.city||a.town||a.village||a.municipality||'';"
            +     "if(city)parts.push(city);"
            +     "if(a.road)parts.push(a.road);"
            +     "if(a.house_number)parts.push(a.house_number);"
            +     "var addr=parts.length>0?parts.join(', '):(d.display_name||'').split(',').slice(0,3).join(',').trim();"
            +     "pin.setPopupContent('<b>'+addr+'</b>').openPopup();"
            +     "Android.onLocationSelected(parseFloat(lat),parseFloat(lng),addr);"
            +   "}).catch(function(){"
            +     "var addr=lat+', '+lng;"
            +     "pin.setPopupContent(addr).openPopup();"
            +     "Android.onLocationSelected(parseFloat(lat),parseFloat(lng),addr);"
            +   "});"
            + "});"
            + "</script></body></html>";
    }

    private int dp(int v) {
        return (int)(v * getResources().getDisplayMetrics().density);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
    }
}
