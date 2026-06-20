package com.freshmart.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "freshmart_user";
    private static final int REQ_CHECKOUT = 100;

    private LinearLayout cartBadgeLayout;
    private TextView cartCountText;
    private TextView deliveryAddressText;
    private int cartCount = 0;
    private int cartTotal = 0;

    static class Product {
        String name, emoji, price, category;
        int priceRub;
        Product(String name, String emoji, String price, int priceRub, String category) {
            this.name = name; this.emoji = emoji;
            this.price = price; this.priceRub = priceRub; this.category = category;
        }
    }

    final List<Product> allProducts = Arrays.asList(
        new Product("Яблоки органические", "🍎", "279 ₽", 279, "Фрукты"),
        new Product("Молоко 3.2%",          "🥛", "139 ₽", 139, "Молочное"),
        new Product("Хлеб цельнозерновой",  "🍞",  "89 ₽",  89, "Хлеб"),
        new Product("Бананы",               "🍌", "129 ₽", 129, "Фрукты"),
        new Product("Сыр Российский",       "🧀", "389 ₽", 389, "Молочное"),
        new Product("Яйца С1 (10 шт.)",     "🥚", "129 ₽", 129, "Молочное"),
        new Product("Томаты",               "🍅", "199 ₽", 199, "Овощи"),
        new Product("Сок апельсиновый",     "🍊", "179 ₽", 179, "Напитки"),
        new Product("Куриная грудка",       "🍗", "459 ₽", 459, "Мясо"),
        new Product("Брокколи",             "🥦", "149 ₽", 149, "Овощи"),
        new Product("Макароны",             "🍝", "119 ₽", 119, "Бакалея"),
        new Product("Греческий йогурт",     "🫙", "219 ₽", 219, "Молочное")
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Инициализируем элементы интерфейса
        cartBadgeLayout     = findViewById(R.id.cart_badge_layout);
        cartCountText       = findViewById(R.id.cart_count_text);
        deliveryAddressText = findViewById(R.id.delivery_address_text);

        // УДАЛЯЕМ строчку с recyclerView, так как у тебя используется product_grid (LinearLayout)!

        // 2. Вызываем методы настройки
        refreshDeliveryAddress();
        setupProfileButton();
        setupCategoryChips();

        // 3. Строим сетку товаров (allProducts у тебя инициализирован вверху класса, так что проверка на null не обязательна, но пусть будет)
        if (allProducts != null) {
            buildProductGrid(allProducts);
        }

        setupCartButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDeliveryAddress();
    }

    private void refreshDeliveryAddress() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String addr = prefs.getString("delivery_address", null);
        if (addr != null && deliveryAddressText != null) {
            deliveryAddressText.setText(addr);
        }
    }

    private void setupProfileButton() {
        View profileBtn = findViewById(R.id.profile_btn);
        profileBtn.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            String name  = prefs.getString("name", "Гость");
            String email = prefs.getString("email", "");
            String info  = "👤 " + name + (email.isEmpty() ? "" : "\n✉ " + email);
            Toast.makeText(this, info, Toast.LENGTH_LONG).show();
        });
    }

    private void setupCategoryChips() {
        LinearLayout chipContainer = findViewById(R.id.chip_container);
        String[] cats = {"Все", "Фрукты", "Молочное", "Овощи", "Хлеб", "Мясо", "Напитки", "Бакалея"};

        for (int i = 0; i < cats.length; i++) {
            final String cat = cats[i];
            TextView chip = new TextView(this);
            chip.setText(cat);
            chip.setPadding(dp(14), dp(8), dp(14), dp(8));
            chip.setTextSize(13f);
            chip.setTextColor(i == 0 ? 0xFFFFFFFF : 0xFF2D7A3A);
            chip.setBackgroundResource(i == 0 ? R.drawable.chip_selected : R.drawable.chip_default);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(10));
            chip.setLayoutParams(lp);

            chip.setOnClickListener(v -> {
                for (int j = 0; j < chipContainer.getChildCount(); j++) {
                    TextView c = (TextView) chipContainer.getChildAt(j);
                    c.setBackgroundResource(R.drawable.chip_default);
                    c.setTextColor(0xFF2D7A3A);
                }
                chip.setBackgroundResource(R.drawable.chip_selected);
                chip.setTextColor(0xFFFFFFFF);

                List<Product> filtered;
                if (cat.equals("Все")) {
                    filtered = allProducts;
                } else {
                    filtered = new ArrayList<>();
                    for (Product p : allProducts) if (p.category.equals(cat)) filtered.add(p);
                }
                buildProductGrid(filtered);
            });

            chipContainer.addView(chip);
        }
    }

    private void buildProductGrid(List<Product> list) {
        LinearLayout grid = findViewById(R.id.product_grid);
        grid.removeAllViews();

        for (int i = 0; i < list.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rowLp.bottomMargin = dp(16);
            row.setLayoutParams(rowLp);

            row.addView(makeProductCard(list.get(i)));
            if (i + 1 < list.size()) {
                row.addView(makeProductCard(list.get(i + 1)));
            } else {
                View spacer = new View(this);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 0, 1f);
                sp.setMarginStart(dp(8));
                spacer.setLayoutParams(sp);
                row.addView(spacer);
            }
            grid.addView(row);
        }
    }

    private View makeProductCard(Product product) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd(dp(8));
        card.setLayoutParams(lp);
        card.setRadius(dp(18));
        card.setCardElevation(dp(4));
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(14), dp(14), dp(14), dp(14));

        // Emoji
        TextView emojiView = new TextView(this);
        emojiView.setText(product.emoji);
        emojiView.setTextSize(48f);
        emojiView.setGravity(Gravity.CENTER);
        inner.addView(emojiView);

        // Название
        TextView nameView = new TextView(this);
        nameView.setText(product.name);
        nameView.setTextSize(13f);
        nameView.setTextColor(0xFF1A1A1A);
        nameView.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams nameLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nameLp.topMargin = dp(6);
        nameView.setLayoutParams(nameLp);
        inner.addView(nameView);

        // Категория
        TextView catView = new TextView(this);
        catView.setText(product.category);
        catView.setTextSize(11f);
        catView.setTextColor(0xFF888888);
        inner.addView(catView);

        // Строка цена + кнопка
        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams priceRowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        priceRowLp.topMargin = dp(10);
        priceRow.setLayoutParams(priceRowLp);

        TextView priceView = new TextView(this);
        priceView.setText(product.price);
        priceView.setTextSize(15f);
        priceView.setTextColor(0xFF2D7A3A);
        priceView.setTypeface(null, Typeface.BOLD);
        priceView.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        priceRow.addView(priceView);

        TextView addBtn = new TextView(this);
        addBtn.setText("+");
        addBtn.setTextSize(20f);
        addBtn.setTextColor(0xFFFFFFFF);
        addBtn.setBackgroundResource(R.drawable.add_button_bg);
        addBtn.setPadding(dp(10), dp(2), dp(10), dp(4));
        addBtn.setGravity(Gravity.CENTER);
        addBtn.setOnClickListener(v -> {
            cartCount++;
            cartTotal += product.priceRub;
            cartCountText.setText(String.valueOf(cartCount));
            cartBadgeLayout.setVisibility(View.VISIBLE);
            Toast.makeText(this, product.name + " добавлен в корзину!", Toast.LENGTH_SHORT).show();
        });
        priceRow.addView(addBtn);

        inner.addView(priceRow);
        card.addView(inner);
        return card;
    }

    private void setupCartButton() {
        View cartBtn = findViewById(R.id.cart_fab);
        cartBtn.setOnClickListener(v -> {
            if (cartCount > 0) {
                Intent intent = new Intent(this, CheckoutActivity.class);
                intent.putExtra("cart_count", cartCount);
                intent.putExtra("cart_total", cartTotal);
                startActivityForResult(intent, REQ_CHECKOUT);
            } else {
                Toast.makeText(this, "Корзина пуста. Добавьте товары!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CHECKOUT && resultCode == RESULT_OK) {
            cartCount = 0;
            cartTotal = 0;
            cartCountText.setText("0");
            cartBadgeLayout.setVisibility(View.GONE);
        }
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }
}
