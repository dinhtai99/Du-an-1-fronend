package fpoly.haideptrai.duan1.customer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import fpoly.haideptrai.duan1.R;
import fpoly.haideptrai.duan1.customer.adapters.OnboardingAdapter;
import fpoly.haideptrai.duan1.customer.models.OnboardingItem;
import fpoly.haideptrai.duan1.utils.SessionManager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout layoutIndicators;
    private Button btnNext;
    private TextView btnSkip;
    private OnboardingAdapter onboardingAdapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        // Tạm thời comment để test onboarding luôn hiển thị
        // TODO: Bỏ comment khi đã test xong
        /*
        if (sessionManager.isOnboardingCompleted()) {
            navigateToAuth();
            return;
        }
        */
        setContentView(R.layout.activity_onboarding);
        initViews();
        setupOnboardingItems();
        setupIndicators();
        setCurrentIndicator(0);
        setupListeners();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPagerOnboarding);
        layoutIndicators = findViewById(R.id.layoutIndicators);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
    }

    private void setupOnboardingItems() {
        android.util.Log.d("OnboardingActivity", "Setting up onboarding items...");
        List<OnboardingItem> items = new ArrayList<>();

        int img1ResId = R.drawable.img_1;
        int img2ResId = R.drawable.img_2;
        int img3ResId = R.drawable.img_3;

        Log.d("OnboardingActivity", "Image resource IDs - img_1: " + img1ResId + ", img_2: " + img2ResId + ", img_3: " + img3ResId);

        items.add(new OnboardingItem(
                img1ResId,
                R.string.onboarding_title_1,
                R.string.onboarding_description_1
        ));
        items.add(new OnboardingItem(
                img2ResId,
                R.string.onboarding_title_2,
                R.string.onboarding_description_2
        ));
        items.add(new OnboardingItem(
                img3ResId,
                R.string.onboarding_title_3,
                R.string.onboarding_description_3
        ));

        android.util.Log.d("OnboardingActivity", "Created " + items.size() + " onboarding items");

        onboardingAdapter = new OnboardingAdapter(items);
        viewPager.setAdapter(onboardingAdapter);

        android.util.Log.d("OnboardingActivity", "Adapter set to ViewPager2");
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentIndicator(position);
                updateNextButtonLabel(position);
            }
        });
    }

    private void setupIndicators() {
        int itemCount = onboardingAdapter != null ? onboardingAdapter.getItemCount() : 0;
        layoutIndicators.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        for (int i = 0; i < itemCount; i++) {
            View indicator = new View(this);
            indicator.setBackgroundResource(R.drawable.bg_indicator_inactive);
            indicator.setLayoutParams(params);
            indicator.setMinimumWidth(16);
            indicator.setMinimumHeight(16);
            layoutIndicators.addView(indicator);
        }
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicators.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = layoutIndicators.getChildAt(i);
            child.setBackgroundResource(i == position ? R.drawable.img_1 : R.drawable.img_2);
        }
    }

    private void updateNextButtonLabel(int position) {
        if (onboardingAdapter == null) {
            return;
        }
        if (position == onboardingAdapter.getItemCount() - 1) {
            btnNext.setText(R.string.onboarding_start);
        } else {
            btnNext.setText(R.string.onboarding_next);
        }
    }

    private void setupListeners() {
        btnSkip.setOnClickListener(v -> finishOnboarding());
        btnNext.setOnClickListener(v -> {
            int nextPosition = viewPager.getCurrentItem() + 1;
            if (onboardingAdapter != null && nextPosition < onboardingAdapter.getItemCount()) {
                viewPager.setCurrentItem(nextPosition);
            } else {
                finishOnboarding();
            }
        });
    }

    private void finishOnboarding() {
        sessionManager.setOnboardingCompleted(true);
        navigateToAuth();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(this, DangNhapActivity.class);
        startActivity(intent);
        finish();
    }
}
