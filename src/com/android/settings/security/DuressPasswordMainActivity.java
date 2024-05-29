package com.android.settings.security;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockscreenCredential;
import com.android.settings.R;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.google.android.setupdesign.GlifRecyclerLayout;
import com.google.android.setupdesign.items.IItem;
import com.google.android.setupdesign.items.Item;
import com.google.android.setupdesign.items.ItemGroup;
import com.google.android.setupdesign.items.RecyclerItemAdapter;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class DuressPasswordMainActivity extends DuressPasswordActivity implements RecyclerItemAdapter.OnItemSelectedListener {
    private static final String TAG = DuressPasswordMainActivity.class.getSimpleName();
    private static final String KEY_USER_CREDENTIAL = "user_credential";
    private static final String KEY_HAS_ASKED_FOR_USER_CREDENTIALS = "asked_for_user_credentials";

    private GlifRecyclerLayout layout;
    private LockscreenCredential userCredential;
    private boolean askedForUserCredentials;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        var layout = new GlifRecyclerLayout(this);
        this.layout = layout;
        layout.setIcon(getDrawable(R.drawable.ic_lock));
        layout.setHeaderText(R.string.duress_pwd_pref_title);
        adjustDescriptionStyle(layout);
        layout.setDescriptionText(R.string.duress_pwd_description);

        setContentView(layout);

        if (savedInstanceState != null) {
            userCredential = savedInstanceState.getParcelable(KEY_USER_CREDENTIAL, LockscreenCredential.class);
            askedForUserCredentials = requireNonNull(savedInstanceState.getBoolean2(KEY_HAS_ASKED_FOR_USER_CREDENTIALS));
        }
    }

    @Override
    protected boolean hasUserCredential() {
        return userCredential != null && !userCredential.isNone();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_USER_CREDENTIAL, userCredential);
        outState.putBoolean(KEY_HAS_ASKED_FOR_USER_CREDENTIALS, askedForUserCredentials);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (userCredential == null) {
            if (!getLockPatternUtils().isSecure(getUserId())) {
                userCredential = LockscreenCredential.createNone();
            } else if (!askedForUserCredentials) {
                var b = new ChooseLockSettingsHelper.Builder(this);
                b.setRequestCode(REQ_CODE_OBTAIN_USER_CREDENTIALS);
                b.setReturnCredentials(true);
                b.setForegroundOnly(true);
                b.show();
                askedForUserCredentials = true;
            }
        }

        updateActionList();
    }

    private void updateActionList() {
        if (userCredential == null) {
            Log.d(TAG, "no userCredential, skipping updateActionList");
            return;
        }

        var g = new ItemGroup();

        if (getLockPatternUtils().hasDuressCredentials(userCredential)) {
            g.addChild(createItem(R.string.duress_pwd_action_update, R.drawable.ic_edit));
            g.addChild(createItem(R.string.duress_pwd_action_delete, R.drawable.ic_delete));
        } else {
            g.addChild(createItem(R.string.duress_pwd_action_add, R.drawable.ic_add_24dp));
        }

        var adapter = new RecyclerItemAdapter(g);
        adapter.setOnItemSelectedListener(this);
        layout.setAdapter(adapter);
    }

    private Item createItem(@StringRes int title, @DrawableRes int icon) {
        var i = new Item();
        i.setId(title);
        i.setTitle(getText(title));
        i.setIcon(getDrawable(icon));
        return i;
    }

    // RecyclerItemAdapter.OnItemSelectedListener
    @Override
    public void onItemSelected(IItem iitem) {
        Item item = (Item) iitem;
        int id = item.getId();

        if (id == R.string.duress_pwd_action_add || id == R.string.duress_pwd_action_update) {
            var i = new Intent(this, DuressPasswordSetupActivity.class);
            i.putExtra(DuressPasswordSetupActivity.EXTRA_TITLE_TEXT, id);
            i.putExtra(DuressPasswordSetupActivity.EXTRA_USER_CREDENTIAL, userCredential);
            allowNextOnStop = true;
            startActivityForResult(i, REQ_CODE_SETUP);
        } else if (id == R.string.duress_pwd_action_delete) {
            var b = new AlertDialog.Builder(this);
            b.setMessage(R.string.duress_pwd_action_delete_confirmation);
            b.setPositiveButton(R.string.duress_pwd_delete_button, (dialog, which) -> {
                LockPatternUtils lpu = getLockPatternUtils();
                try {
                    lpu.deleteDuressCredentials(userCredential);
                } catch (Exception e) {
                    Log.e(TAG, "deleteDuressCredentials failed", e);

                    var d = new AlertDialog.Builder(this);
                    d.setMessage(getString(R.string.duress_pwd_delete_error, e.toString()));
                    d.setNeutralButton(R.string.duress_pwd_error_dialog_dismiss, null);
                    d.show();
                    return;
                }
                updateActionList();
            });
            b.setNegativeButton(R.string.duress_pwd_cancel_button, null);
            b.show();
        }
    }

    static final int REQ_CODE_OBTAIN_USER_CREDENTIALS = 1;
    static final int REQ_CODE_SETUP = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_SETUP) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
            return;
        }

        if (requestCode != REQ_CODE_OBTAIN_USER_CREDENTIALS) {
            throw new IllegalStateException(Integer.toString(resultCode));
        }

        Log.d(TAG, "onActivityResult, requestCode: " + requestCode + ", resultCode: " + resultCode);

        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        if (data == null) {
            throw new IllegalStateException("data == null");
        }

        var credential = data.getParcelableExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, LockscreenCredential.class);
        if (credential == null) {
            throw new IllegalStateException("no returned credential");
        }
        userCredential = credential;
    }
}
