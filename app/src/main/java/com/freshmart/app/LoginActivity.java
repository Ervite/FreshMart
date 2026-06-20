package com.freshmart.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class LoginActivity extends AppCompatActivity {

    private static final String PREFS = "freshmart_user";

    private EditText nameEdit, phoneEdit, emailEdit, passwordEdit;
    private TextView titleText, switchLink, errorText;
    private Button actionButton;
    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Уже вошли — сразу в магазин
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean("logged_in", false)) {
            goToMain();
            return;
        }

        setContentView(buildLayout());
    }

    private View buildLayout() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(0xFFF5F7F5);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(48));

        // Лого
        TextView logo = new TextView(this);
        logo.setText("🛒");
        logo.setTextSize(72f);
        logo.setGravity(Gravity.CENTER);
        root.addView(logo);

        // Название приложения
        TextView appName = new TextView(this);
        appName.setText("ФрешМарт");
        appName.setTextSize(28f);
        appName.setTypeface(null, Typeface.BOLD);
        appName.setTextColor(0xFF2D7A3A);
        appName.setGravity(Gravity.CENTER);
        lp(appName, 0, 8, 0, 4);
        root.addView(appName);

        TextView sub = new TextView(this);
        sub.setText("Свежие продукты с доставкой");
        sub.setTextSize(13f);
        sub.setTextColor(0xFF888888);
        sub.setGravity(Gravity.CENTER);
        lp(sub, 0, 0, 0, dp(32));
        root.addView(sub);

        // Карточка формы
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardLp.bottomMargin = dp(16);
        card.setLayoutParams(cardLp);
        card.setRadius(dp(20));
        card.setCardElevation(dp(6));
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(28), dp(24), dp(28));

        titleText = new TextView(this);
        titleText.setText("Вход в аккаунт");
        titleText.setTextSize(20f);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setTextColor(0xFF1A1A1A);
        lp(titleText, 0, 0, 0, dp(20));
        form.addView(titleText);

        // Поля только для регистрации
        nameEdit = field("Имя", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameEdit.setVisibility(View.GONE);
        form.addView(nameEdit);

        phoneEdit = field("Телефон (+7...)", InputType.TYPE_CLASS_PHONE);
        phoneEdit.setVisibility(View.GONE);
        form.addView(phoneEdit);

        // Общие поля
        emailEdit = field("Email", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        form.addView(emailEdit);

        passwordEdit = field("Пароль", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(passwordEdit);

        // Ошибка
        errorText = new TextView(this);
        errorText.setTextSize(13f);
        errorText.setTextColor(0xFFD32F2F);
        errorText.setVisibility(View.GONE);
        lp(errorText, 0, dp(4), 0, dp(8));
        form.addView(errorText);

        // Кнопка действия
        actionButton = new Button(this);
        actionButton.setText("Войти");
        actionButton.setTextColor(Color.WHITE);
        actionButton.setTextSize(16f);
        actionButton.setTypeface(null, Typeface.BOLD);
        actionButton.setBackgroundColor(0xFF2D7A3A);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        btnLp.topMargin = dp(8);
        actionButton.setLayoutParams(btnLp);
        actionButton.setOnClickListener(v -> handleAction());
        form.addView(actionButton);

        card.addView(form);
        root.addView(card);

        // Переключить режим
        switchLink = new TextView(this);
        switchLink.setText("Нет аккаунта? Зарегистрироваться");
        switchLink.setTextSize(14f);
        switchLink.setTextColor(0xFF2D7A3A);
        switchLink.setGravity(Gravity.CENTER);
        switchLink.setPadding(0, dp(8), 0, dp(8));
        switchLink.setOnClickListener(v -> toggleMode());
        root.addView(switchLink);

        // Войти как гость
        TextView guestBtn = new TextView(this);
        guestBtn.setText("Войти как гость →");
        guestBtn.setTextSize(13f);
        guestBtn.setTextColor(0xFF999999);
        guestBtn.setGravity(Gravity.CENTER);
        guestBtn.setPadding(0, dp(12), 0, 0);
        guestBtn.setOnClickListener(v -> loginAsGuest());
        root.addView(guestBtn);

        scroll.addView(root);
        return scroll;
    }

    private EditText field(String hint, int inputType) {
        EditText et = new EditText(this);
        et.setHint(hint);
        et.setInputType(inputType);
        et.setTextSize(15f);
        et.setTextColor(0xFF1A1A1A);
        et.setHintTextColor(0xFFBBBBBB);
        et.setBackgroundColor(0xFFF2F4F2);
        et.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        lp.bottomMargin = dp(12);
        et.setLayoutParams(lp);
        return et;
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            titleText.setText("Регистрация");
            actionButton.setText("Создать аккаунт");
            switchLink.setText("Уже есть аккаунт? Войти");
            nameEdit.setVisibility(View.VISIBLE);
            phoneEdit.setVisibility(View.VISIBLE);
        } else {
            titleText.setText("Вход в аккаунт");
            actionButton.setText("Войти");
            switchLink.setText("Нет аккаунта? Зарегистрироваться");
            nameEdit.setVisibility(View.GONE);
            phoneEdit.setVisibility(View.GONE);
        }
        errorText.setVisibility(View.GONE);
    }

    private void handleAction() {
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            showError("Введите корректный email");
            return;
        }
        if (password.length() < 6) {
            showError("Пароль — минимум 6 символов");
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor ed = prefs.edit();

        if (isRegisterMode) {
            String name = nameEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            if (name.isEmpty()) { showError("Введите ваше имя"); return; }

            ed.putString("name", name)
              .putString("phone", phone)
              .putString("email", email)
              .putBoolean("logged_in", true)
              .apply();

            Toast.makeText(this, "👋 Добро пожаловать, " + name + "!", Toast.LENGTH_LONG).show();
        } else {
            String savedEmail = prefs.getString("email", "");
            if (!savedEmail.equals(email)) {
                showError("Email не найден. Сначала зарегистрируйтесь.");
                return;
            }
            ed.putBoolean("logged_in", true).apply();
            String name = prefs.getString("name", "Пользователь");
            Toast.makeText(this, "👋 С возвращением, " + name + "!", Toast.LENGTH_LONG).show();
        }

        goToMain();
    }

    private void loginAsGuest() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString("name", "Гость")
                .putString("email", "")
                .putBoolean("logged_in", true)
                .apply();
        goToMain();
    }

    private void showError(String msg) {
        errorText.setText("⚠ " + msg);
        errorText.setVisibility(View.VISIBLE);
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }

    private void lp(View v, int l, int t, int r, int b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(l, t, r, b);
        v.setLayoutParams(p);
    }
}
