package com.android.settings.security;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SetupWizardUtils;
import com.android.settings.overlay.FeatureFactory;
import com.google.android.setupdesign.GlifLayout;
import com.google.android.setupdesign.template.DescriptionMixin;
import com.google.android.setupdesign.util.ThemeHelper;

public class DuressPasswordActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setTheme(SetupWizardUtils.getTheme(this, getIntent()));
        ThemeHelper.trySetDynamicColor(this);

        setResult(RESULT_OK);
    }

    protected boolean allowNextOnStop;

    @Override
    protected void onStop() {
        super.onStop();
        if (!isChangingConfigurations() && hasUserCredential()) {
            if (allowNextOnStop) {
                allowNextOnStop = false;
            } else {
                // require user credential to be re-entered after activity is backgrounded
                setResult(RESULT_CANCELED);
                finish();
            }
        }
    }

    protected boolean hasUserCredential() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            /** @see com.android.settings.password.ConfirmDeviceCredentialBaseActivity#onDestroy */
            getMainThreadHandler().postDelayed(() -> {
                System.gc();
                System.runFinalization();
                System.gc();
            }, 5000);
        }
    }

    static void adjustDescriptionStyle(GlifLayout l) {
        l.getMixin(DescriptionMixin.class).getTextView().setTextSize(16f);
    }

    LockPatternUtils getLockPatternUtils() {
        return FeatureFactory.getFeatureFactory()
                .getSecurityFeatureProvider()
                .getLockPatternUtils(this);
    }
}
