package com.mobnote.permission;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.RestrictTo;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

/**
 * Created by liumin on 2018/3/19.
 */

public class GolukSettingDialog implements Parcelable {

    public static final int DEFAULT_SETTINGS_REQ_CODE = 16061;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public static final Parcelable.Creator<GolukSettingDialog> CREATOR = new Creator<GolukSettingDialog>() {
        @Override
        public GolukSettingDialog createFromParcel(Parcel in) {
            return new GolukSettingDialog(in);
        }

        @Override
        public GolukSettingDialog[] newArray(int size) {
            return new GolukSettingDialog[size];
        }
    };

    static final String EXTRA_APP_SETTINGS = "extra_app_settings";

    private final int mThemeResId;
    private final String mRationale;
    private final String mTitle;
    private final String mPositiveButtonText;
    private final String mNegativeButtonText;
    private final int mRequestCode;
    private Object mActivityOrFragment;
    private Context mContext;

    private GolukSettingDialog(Parcel in) {
        mThemeResId = in.readInt();
        mRationale = in.readString();
        mTitle = in.readString();
        mPositiveButtonText = in.readString();
        mNegativeButtonText = in.readString();
        mRequestCode = in.readInt();
    }

    public int getRequestCode() {
        return mRequestCode;
    }

    private GolukSettingDialog(final Object activityOrFragment,
                               int themeResId,
                               String rationale,
                               String title,
                               String positiveButtonText,
                               String negativeButtonText,
                               int requestCode) {
        setActivityOrFragment(activityOrFragment);
        mThemeResId = themeResId;
        mRationale = rationale;
        mTitle = title;
        mPositiveButtonText = positiveButtonText;
        mNegativeButtonText = negativeButtonText;
        mRequestCode = requestCode;
    }

    public static GolukSettingDialog fromIntent(Intent intent, Activity activity) {
        GolukSettingDialog dialog = intent.getParcelableExtra(GolukSettingDialog.EXTRA_APP_SETTINGS);
        dialog.setActivityOrFragment(activity);
        return dialog;
    }

    private void setActivityOrFragment(Object activityOrFragment) {
        mActivityOrFragment = activityOrFragment;

        if (activityOrFragment instanceof Activity) {
            mContext = (Activity) activityOrFragment;
        } else if (activityOrFragment instanceof Fragment) {
            mContext = ((Fragment) activityOrFragment).getContext();
        } else if (activityOrFragment instanceof android.app.Fragment) {
            mContext = ((android.app.Fragment) activityOrFragment).getActivity();
        } else {
            throw new IllegalStateException("Unknown object: " + activityOrFragment);
        }
    }

    private void startForResult(Intent intent) {
        if (mActivityOrFragment instanceof Activity) {
            ((Activity) mActivityOrFragment).startActivityForResult(intent, mRequestCode);
        } else if (mActivityOrFragment instanceof Fragment) {
            ((Fragment) mActivityOrFragment).startActivityForResult(intent, mRequestCode);
        } else if (mActivityOrFragment instanceof android.app.Fragment) {
            ((android.app.Fragment) mActivityOrFragment).startActivityForResult(intent,
                    mRequestCode);
        }
    }

    /**
     * Display the built dialog.
     */
    public void show() {
        startForResult(GolukSettingsDialogHolderActivity.createShowDialogIntent(mContext, this));
    }

    /**
     * Show the dialog. {@link #show()} is a wrapper to ensure backwards compatibility
     */
    AlertDialog showDialog(DialogInterface.OnClickListener positiveListener,
                           DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder;
        if (mThemeResId > 0) {
            builder = new AlertDialog.Builder(mContext, mThemeResId);
        } else {
            builder = new AlertDialog.Builder(mContext);
        }
        return builder
                .setCancelable(false)
                .setTitle(mTitle)
                .setMessage(mRationale)
                .setPositiveButton(mPositiveButtonText, positiveListener)
                .setNegativeButton(mNegativeButtonText, negativeListener)
                .show();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mThemeResId);
        dest.writeString(mRationale);
        dest.writeString(mTitle);
        dest.writeString(mPositiveButtonText);
        dest.writeString(mNegativeButtonText);
        dest.writeInt(mRequestCode);
    }

    /**
     * Builder for an {@link GolukSettingDialog}.
     */
    public static class Builder {

        private final Object mActivityOrFragment;
        private final Context mContext;
        private int mThemeResId = -1;
        private String mRationale;
        private String mTitle;
        private String mPositiveButtonText;
        private String mNegativeButtonText;
        private int mRequestCode = -1;


        public Builder(Activity activity) {
            mActivityOrFragment = activity;
            mContext = activity;
        }

        public Builder(Fragment fragment) {
            mActivityOrFragment = fragment;
            mContext = fragment.getContext();
        }

        /**
         * Create a new Builder for an {@link GolukSettingDialog}.
         *
         * @param fragment the {@link android.app.Fragment} in which to display the dialog.
         */
        public Builder(android.app.Fragment fragment) {
            mActivityOrFragment = fragment;
            mContext = fragment.getActivity();
        }

        /**
         * Set the dialog theme.
         */
        public GolukSettingDialog.Builder setThemeResId(int themeResId) {
            mThemeResId = themeResId;
            return this;
        }

        /**
         * Set the title dialog. Default is "Permissions Required".
         */
        public GolukSettingDialog.Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        /**
         * Set the title dialog. Default is "Permissions Required".
         */
        public GolukSettingDialog.Builder setTitle(int title) {
            mTitle = mContext.getString(title);
            return this;
        }

        /**
         * Set the rationale dialog. Default is
         * "This app may not work correctly without the requested permissions.
         * Open the app settings screen to modify app permissions."
         */
        public GolukSettingDialog.Builder setRationale(String rationale) {
            mRationale = rationale;
            return this;
        }

        /**
         * Set the rationale dialog. Default is
         * "This app may not work correctly without the requested permissions.
         * Open the app settings screen to modify app permissions."
         */
        public GolukSettingDialog.Builder setRationale(int rationale) {
            mRationale = mContext.getString(rationale);
            return this;
        }

        /**
         * Set the positive button text, default is {@link android.R.string#ok}.
         */
        public GolukSettingDialog.Builder setPositiveButton(String text) {
            mPositiveButtonText = text;
            return this;
        }

        /**
         * Set the positive button text, default is {@link android.R.string#ok}.
         */
        public GolukSettingDialog.Builder setPositiveButton(int textId) {
            mPositiveButtonText = mContext.getString(textId);
            return this;
        }

        public GolukSettingDialog.Builder setNegativeButton(String text) {
            mNegativeButtonText = text;
            return this;
        }

        /**
         * Set the negative button text, default is {@link android.R.string#cancel}.
         */
        public GolukSettingDialog.Builder setNegativeButton(int textId) {
            mNegativeButtonText = mContext.getString(textId);
            return this;
        }

        public GolukSettingDialog.Builder setRequestCode(int requestCode) {
            mRequestCode = requestCode;
            return this;
        }

        /**
         * Build the {@link GolukSettingDialog} from the specified options. Generally followed by a
         * call to {@link GolukSettingDialog#show()}.
         */
        public GolukSettingDialog build() {
            mRationale = TextUtils.isEmpty(mRationale) ?
                    mContext.getString(pub.devrel.easypermissions.R.string.rationale_ask_again) : mRationale;
            mTitle = TextUtils.isEmpty(mTitle) ?
                    mContext.getString(pub.devrel.easypermissions.R.string.title_settings_dialog) : mTitle;
            mPositiveButtonText = TextUtils.isEmpty(mPositiveButtonText) ?
                    mContext.getString(android.R.string.ok) : mPositiveButtonText;
            mNegativeButtonText = TextUtils.isEmpty(mNegativeButtonText) ?
                    mContext.getString(android.R.string.cancel) : mNegativeButtonText;
            mRequestCode = mRequestCode > 0 ? mRequestCode : DEFAULT_SETTINGS_REQ_CODE;


            return new GolukSettingDialog(
                    mActivityOrFragment,
                    mThemeResId,
                    mRationale,
                    mTitle,
                    mPositiveButtonText,
                    mNegativeButtonText,
                    mRequestCode);
        }

    }
}
