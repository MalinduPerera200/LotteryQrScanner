package lk.jiat.qr_scanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ResultDispatcherActivity extends AppCompatActivity {
    private static final Map<String, Class<?>> LOTTERY_ACTIVITY_MAP = new HashMap<String, Class<?>>() {{
        put("Mega Power", MegaPowerResultActivity.class);
        put("Govi Setha", GoviSethaResultActivity.class);
        put("Mahajana Sampatha", MahajanaSampathaResultActivity.class);
        put("Dhana Nidhanaya", DhanaNidhanayaResultActivity.class);
        put("NLB Jaya", NLBJayaResultActivity.class);
    }};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String qrResult = getIntent().getStringExtra("QrResult");

        if (qrResult == null || qrResult.trim().isEmpty()) {
            showError("No QR result found");
            return;
        }

        BaseResultActivity.QRParser parser = new BaseResultActivity.QRParser(qrResult);
        if (!parser.isValid()) {
            showError("Could not parse QR Code");
            return;
        }

        String lotteryName = parser.getLotteryName();
        Class<?> targetActivityClass = LOTTERY_ACTIVITY_MAP.get(lotteryName);

        if (targetActivityClass != null) {
            Intent intent = new Intent(this, targetActivityClass);
            intent.putExtra("QrResult", qrResult);
            startActivity(intent);
        } else {
            showError("Unsupported lottery type: " + lotteryName);
        }
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}