package com.freshmart.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class CheckoutActivity extends AppCompatActivity {

    private static final String PREFS = "freshmart_user";
    private static final int REQ_MAP = 200;

    private TextView mapAddressText;
    private String pickedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        int cartCount = getIntent().getIntExtra("cart_count", 0);
        int total     = getIntent().getIntExtra("cart_total", 0);

        ((TextView) findViewById(R.id.order_summary_text))
                .setText(cartCount + " " + itemsWord(cartCount));
        ((TextView) findViewById(R.id.order_total_text))
                .setText(total + " ₽");

        mapAddressText = findViewById(R.id.map_address_text);

        // Если уже был сохранён адрес — показываем его
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = prefs.getString("delivery_address", null);
        if (saved != null && !saved.isEmpty()) {
            mapAddressText.setText("📍 " + saved);
            pickedAddress = saved;
        }

        // Кнопка «Выбрать на карте»
        View mapBtn = findViewById(R.id.btn_open_map);
        mapBtn.setOnClickListener(v ->
            startActivityForResult(new Intent(this, MapPickerActivity.class), REQ_MAP));

        // Кнопка назад
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Регион — показать/скрыть поле «Страна»
        RadioGroup regionGroup = findViewById(R.id.region_group);
        View labelCountry = findViewById(R.id.label_country);
        EditText fieldCountry = findViewById(R.id.field_country);
        RadioButton rbSbp = findViewById(R.id.rb_sbp);

        regionGroup.setOnCheckedChangeListener((g, id) -> {
            boolean intl = (id == R.id.rb_world);
            labelCountry.setVisibility(intl ? View.VISIBLE : View.GONE);
            fieldCountry.setVisibility(intl ? View.VISIBLE : View.GONE);
            rbSbp.setEnabled(!intl);
            rbSbp.setText(intl ? "📱  СБП (только для России)"
                               : "📱  СБП (Система быстрых платежей)");
        });

        // Оформить заказ
        findViewById(R.id.btn_place_order).setOnClickListener(v ->
                placeOrder(regionGroup, fieldCountry));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_MAP && resultCode == RESULT_OK && data != null) {
            pickedAddress = data.getStringExtra("address");
            mapAddressText.setText("📍 " + pickedAddress);
        }
    }

    private void placeOrder(RadioGroup regionGroup, EditText fieldCountry) {
        // Если адрес не выбран на карте — требуем
        if (pickedAddress.isEmpty()) {
            Toast.makeText(this, "⚠ Выберите адрес доставки на карте", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean intl = (regionGroup.getCheckedRadioButtonId() == R.id.rb_world);
        String country = fieldCountry.getText().toString().trim();
        if (intl && country.isEmpty()) {
            fieldCountry.setError("Укажите страну"); return;
        }

        String fullAddress = intl ? country + ", " + pickedAddress : pickedAddress;

        RadioGroup payGroup = findViewById(R.id.payment_group);
        int payId = payGroup.getCheckedRadioButtonId();
        String payment = payId == R.id.rb_cash ? "Наличные"
                       : payId == R.id.rb_sbp  ? "СБП"
                       : "Банковская карта";

        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("delivery_address", fullAddress)
                .apply();

        Toast.makeText(this,
                "✅ Заказ оформлен!\n📍 " + fullAddress + "\n💳 " + payment,
                Toast.LENGTH_LONG).show();

        setResult(RESULT_OK);
        finish();
    }

    private String itemsWord(int n) {
        int m = n % 100;
        if (m >= 11 && m <= 14) return "товаров";
        switch (n % 10) {
            case 1: return "товар";
            case 2: case 3: case 4: return "товара";
            default: return "товаров";
        }
    }
}
