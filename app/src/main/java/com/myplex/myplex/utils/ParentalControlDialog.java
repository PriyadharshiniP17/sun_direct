package com.myplex.myplex.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.myplex.util.PrefUtils;
import com.myplex.util.SDKLogger;
import com.myplex.util.StringEscapeUtils;
import com.myplex.myplex.R;
import com.myplex.myplex.analytics.CleverTap;
import com.myplex.myplex.ui.views.PinEntryEditText;

/**
 * Created by Srikanth on 11-06-2018.
 */

public class ParentalControlDialog {
    private final ParentalControlOptionUpdateListener parentalControlOptionUpdateListener;
    private boolean reEnteringPin;
    private int previousPinNo;
    private boolean isHintShowing;

    public interface ParentalControlOptionUpdateListener {
        void onUpdateOption(boolean success);
    }

    private final Context mContext;

    public ParentalControlDialog(Context context, ParentalControlOptionUpdateListener parentalControlOptionUpdateListener) {
        this.mContext = context;
        this.parentalControlOptionUpdateListener = parentalControlOptionUpdateListener;
    }

    public void showSetPINDialog(int positionOfParentalRadioGroup) throws Exception {
        if (mContext == null
                || !(mContext instanceof Activity
                || ((Activity) mContext).isFinishing())) {
            throw new IllegalStateException("Invalid Context");
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View customView = inflater.inflate(R.layout.layout_pin_entry, null);
        final PinEntryEditText pinEntryEditText = (PinEntryEditText) customView.findViewById(R.id.txt_pin_entry);
        pinEntryEditText.setLongClickable(false);
        pinEntryEditText.setTextIsSelectable(false);
        final TextView pinEntryTitle = (TextView) customView.findViewById(R.id.txt_pin_entry_title);
        pinEntryTitle.setVisibility(View.VISIBLE);
        pinEntryTitle.setText(mContext.getString(R.string.parental_text_set_pin));
        final TextView pinEntryMessage = (TextView) customView.findViewById(R.id.txt_pin_entry_message);
        pinEntryMessage.setVisibility(View.VISIBLE);
        pinEntryMessage.setText(mContext.getString(R.string.parental_text_activate_message));
        final TextView forgotPINNote = (TextView) customView.findViewById(R.id.txt_forgot_pin_note);
        final TextView negativeButton = (TextView) customView.findViewById(R.id.negative_button);
        final TextView positiveButton = (TextView) customView.findViewById(R.id.positive_button);
        forgotPINNote.setVisibility(View.GONE);
        String textForgotPin = StringEscapeUtils.unescapeJava(mContext.getString(R.string.parental_text_forgot_pin));
        Spannable wordtoSpan = new SpannableString(textForgotPin);
        ClickableSpan span1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                // do some thing
                pinEntryEditText.setSecure(false);
                isHintShowing = true;
                positiveButton.setText(mContext.getString(R.string.text_okay));
                pinEntryEditText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (isHintShowing) {
                            isHintShowing = false;
                            pinEntryEditText.setOnTouchListener(null);
                            pinEntryEditText.setSecure(true);
                            positiveButton.setText(mContext.getString(R.string.otp_submit));
                            pinEntryEditText.setText("");
                        }
                        return false;
                    }
                });
                String hint = getHintText();
                String pin = String.valueOf(PrefUtils.getInstance().getPrefParentalControlPIN());
                CleverTap.eventParentalControlHint(hint, pin);
                pinEntryEditText.setText(hint);
            }

            private String getHintText() {
                int passward = PrefUtils.getInstance().getPrefParentalControlPIN();
                int firstDigit = firstDigit(passward);
                int lastDigit = passward % 10;
                SDKLogger.debug("password- " + passward + " firstDigit- " + firstDigit + " lastDigit- " + lastDigit);
                StringBuilder passwordText = new StringBuilder(firstDigit + "*" + "*" + lastDigit);
                return passwordText.toString();
            }

            // Find the first digit
            int firstDigit(int n) {
                // Remove last digit from number
                // till only one digit is left
                while (n >= 10)
                    n /= 10;

                // return the first digit
                return n;
            }

            @Override
            public void updateDrawState(final TextPaint textPaint) {
//                textPaint.setColor(Color.WHITE);
                textPaint.bgColor = mContext.getResources().getColor(R.color.app_theme_color);
                textPaint.setUnderlineText(true);
            }
        };

        forgotPINNote.setMovementMethod(LinkMovementMethod.getInstance());
        wordtoSpan.setSpan(span1, textForgotPin.indexOf("Get a"), textForgotPin.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), tncString.indexOf("Terms"), tncString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forgotPINNote.setText(wordtoSpan);

        if (PrefUtils.getInstance().getPrefParentalControlPIN() != -1) {
            pinEntryTitle.setText(mContext.getString(R.string.text_parental_change_setting));
            pinEntryMessage.setText(mContext.getString(R.string.text_parental_change_setting_message));
            if (positionOfParentalRadioGroup == 0) {
                pinEntryMessage.setText(mContext.getString(R.string.text_parental_change_setting_message_reset_pin_note));
            }
            positiveButton.setText(mContext.getString(R.string.otp_submit));
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setView(customView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        if (alertDialog != null) {
            alertDialog.show();
        }

        pinEntryEditText.post(new Runnable() {
            @Override
            public void run() {
                pinEntryEditText.requestFocus();
            }
        });

        if (negativeButton != null) {
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialog1Click()) {
                        alertDialog.dismiss();

                    }
                }

                public boolean onDialog1Click() {
                    if (parentalControlOptionUpdateListener != null)
                        parentalControlOptionUpdateListener.onUpdateOption(false);
                    return true;
                }
            });
        }

        if (positiveButton != null)
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialog2Click()) {
                        alertDialog.dismiss();
                    }
                }

                public boolean onDialog2Click() {
                    try {
                        if (pinEntryEditText == null
                                || pinEntryEditText.getText() == null || TextUtils.isEmpty(pinEntryEditText.getEditableText())) {
                            pinEntryEditText.setVisibility(View.VISIBLE);
                            pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                            pinEntryMessage.setText(mContext.getString(R.string.parental_text_wrong_pin_to_change));
                            SDKLogger.debug("invalid edit text");
                            return false;
                        }
                        if (isHintShowing) {
                            isHintShowing = false;
                            pinEntryEditText.setOnTouchListener(null);
                            pinEntryEditText.setSecure(true);
                            pinEntryEditText.setText("");
                            positiveButton.setText(mContext.getString(R.string.otp_submit));
                            return false;
                        }
                        String pinText = String.valueOf(pinEntryEditText.getText());
                        if (pinText.length() < 4) {
                            CleverTap.eventParentalControlStatus(CleverTap.INCORRECT_PIN);
                            pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                            pinEntryMessage.setText(mContext.getString(R.string.parental_text_wrong_pin_four_digits));
                            return false;
                        }
                        SDKLogger.debug("pinText- " + pinText);
                        int entPinInt = Integer.parseInt(pinText);
                        int availablePinInt = PrefUtils.getInstance().getPrefParentalControlPIN();
                        boolean isValidPin = false;
                        SDKLogger.debug("availablePinInt- " + availablePinInt);
                        if (availablePinInt == -1) {
                            SDKLogger.debug("setting pin by double confirmation");
                            if (!reEnteringPin) {
                                storePINConfirmationInfo(entPinInt);
//                    TODO Show PIN confirmation UI
                                pinEntryEditText.setText("");
                                pinEntryTitle.setText(mContext.getString(R.string.parental_text_confirm_pin));
                                pinEntryMessage.setText(mContext.getString(R.string.parental_text_confirm_pin_message));
                                positiveButton.setText(mContext.getString(R.string.text_activate));
                                return false;
                            }
                            if (reEnteringPin && isRightPinEntered(entPinInt)) {
                                isValidPin = true;
                                PrefUtils.getInstance().setPrefParentalControlPIN(entPinInt);
                            } else {
//                    TODO Show wrong PIN UI
                                pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                                pinEntryEditText.setText("");
                                CleverTap.eventParentalControlStatus(CleverTap.INCORRECT_PIN);
                                pinEntryMessage.setText(mContext.getString(R.string.parental_text_reenter_wrong_pin));
                                return false;
                            }
                        } else if (availablePinInt == entPinInt) {
                            isValidPin = true;
                        }
                        if (!isValidPin) {
                            CleverTap.eventParentalControlStatus(CleverTap.INCORRECT_PIN);
                            pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                            pinEntryMessage.setText(mContext.getString(R.string.parental_text_wrong_pin_to_change));
                            pinEntryEditText.setText("");
                            forgotPINNote.setVisibility(View.VISIBLE);
                            return false;
                        }
                        if (parentalControlOptionUpdateListener != null)
                            parentalControlOptionUpdateListener.onUpdateOption(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (parentalControlOptionUpdateListener != null)
                            parentalControlOptionUpdateListener.onUpdateOption(false);
                        return true;
                    }
                    return true;
                }

            });

        pinEntryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    positiveButton.performClick();
                }
                return false;
            }
        });


    }


    private void storePINConfirmationInfo(int entPinInt) {
        previousPinNo = entPinInt;
        reEnteringPin = true;
    }

    private boolean isRightPinEntered(int entPinInt) {
        return previousPinNo == entPinInt;
    }


    public void showConfirmPINDialog() throws Exception {
        if (mContext == null
                || !(mContext instanceof Activity
                || ((Activity) mContext).isFinishing())) {
            throw new IllegalStateException("Invalid Context");
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View customView = inflater.inflate(R.layout.layout_pin_entry, null);
        final PinEntryEditText pinEntryEditText = (PinEntryEditText) customView.findViewById(R.id.txt_pin_entry);
        pinEntryEditText.setLongClickable(false);
        pinEntryEditText.setTextIsSelectable(false);
        final TextView pinEntryTitle = (TextView) customView.findViewById(R.id.txt_pin_entry_title);
        pinEntryTitle.setVisibility(View.VISIBLE);
        pinEntryTitle.setText(mContext.getString(R.string.text_parental_text));
        final TextView pinEntryMessage = (TextView) customView.findViewById(R.id.txt_pin_entry_message);
        pinEntryMessage.setVisibility(View.VISIBLE);
        pinEntryMessage.setText(mContext.getString(R.string.parental_text_enter_pin_to_watch));
        final TextView forgotPINNote = (TextView) customView.findViewById(R.id.txt_forgot_pin_note);
        final TextView negativeButton = (TextView) customView.findViewById(R.id.negative_button);
        final TextView positiveButton = (TextView) customView.findViewById(R.id.positive_button);
        positiveButton.setText(mContext.getString(R.string.otp_submit));
        forgotPINNote.setVisibility(View.GONE);
        String textForgotPin = StringEscapeUtils.unescapeJava(mContext.getString(R.string.parental_text_forgot_pin));
        Spannable wordtoSpan = new SpannableString(textForgotPin);
        ClickableSpan span1 = new ClickableSpan() {
            @SuppressLint("ClickableViewAccessibility")

            @Override
            public void onClick(View textView) {

                // do some thing
                pinEntryEditText.setSecure(false);
                isHintShowing = true;
                positiveButton.setText(mContext.getString(R.string.text_okay));
                pinEntryEditText.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (isHintShowing) {
                            isHintShowing = false;
                            pinEntryEditText.setOnTouchListener(null);
                            pinEntryEditText.setSecure(true);
                            pinEntryEditText.setText("");
                            positiveButton.setText(mContext.getString(R.string.otp_submit));
                        }
                        return false;
                    }
                });
                String hint = getHintText();
                String pin = String.valueOf(PrefUtils.getInstance().getPrefParentalControlPIN());
                CleverTap.eventParentalControlHint(hint, pin);
                pinEntryEditText.setText(hint);
            }

            private String getHintText() {
                int passward = PrefUtils.getInstance().getPrefParentalControlPIN();
                int firstDigit = firstDigit(passward);
                int lastDigit = passward % 10;
                SDKLogger.debug("password- " + passward + " firstDigit- " + firstDigit + " lastDigit- " + lastDigit);
                StringBuilder passwordText = new StringBuilder(firstDigit + "*" + "*" + lastDigit);
                return passwordText.toString();
            }

            // Find the first digit
            int firstDigit(int n) {
                // Remove last digit from number
                // till only one digit is left
                while (n >= 10)
                    n /= 10;

                // return the first digit
                return n;
            }

            @Override
            public void updateDrawState(final TextPaint textPaint) {
//                textPaint.setColor(Color.WHITE);
                textPaint.bgColor = mContext.getResources().getColor(R.color.app_theme_color);
                textPaint.setUnderlineText(true);

            }
        };

        forgotPINNote.setMovementMethod(LinkMovementMethod.getInstance());
        wordtoSpan.setSpan(span1, textForgotPin.indexOf("Get a"), textForgotPin.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLUE), tncString.indexOf("Terms"), tncString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forgotPINNote.setText(wordtoSpan);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, com.myplex.sdk.R.style.AppCompatAlertDialogStyle);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setView(customView);
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                pinEntryEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        pinEntryEditText.requestFocus();
                    }
                });
            }
        });

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                pinEntryEditText.post(new Runnable() {
                    @Override
                    public void run() {
                        pinEntryEditText.requestFocus();
                    }
                });
            }
        });

        if (alertDialog != null) {
            alertDialog.show();
        }

        if (negativeButton != null)
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialog1Click()) {
                        alertDialog.dismiss();
                    }
                }

                public boolean onDialog1Click() {
                    if (parentalControlOptionUpdateListener != null)
                        parentalControlOptionUpdateListener.onUpdateOption(false);
                    return true;
                }
            });

        if (positiveButton != null)
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDialog2Click()) {
                        alertDialog.dismiss();
                    }
                }

                public boolean onDialog2Click() {
                    try {
                        if (pinEntryEditText == null
                                || pinEntryEditText.getText() == null || TextUtils.isEmpty(pinEntryEditText.getEditableText())) {
                            pinEntryEditText.setVisibility(View.VISIBLE);
                            pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                            pinEntryMessage.setText(mContext.getString(R.string.parental_text_wrong_pin_four_digits));
                            SDKLogger.debug("invalid edit text");
                            return false;
                        }
                        if (isHintShowing) {
                            isHintShowing = false;
                            pinEntryEditText.setOnTouchListener(null);
                            pinEntryEditText.setSecure(true);
                            pinEntryEditText.setText("");
                            positiveButton.setText(mContext.getString(R.string.otp_submit));
                            return false;
                        }
                        String pinText = String.valueOf(pinEntryEditText.getText());
                        SDKLogger.debug("pinText- " + pinText);
                        int entPinInt = Integer.parseInt(pinText);
                        int availablePinInt = PrefUtils.getInstance().getPrefParentalControlPIN();
                        boolean isValidPin = false;
                        SDKLogger.debug("availablePinInt- " + availablePinInt);
                        if (availablePinInt == -1) {
                            SDKLogger.debug("setting pin by double confirmation");
                            if (!reEnteringPin) {
                                storePINConfirmationInfo(entPinInt);
//                    TODO Show PIN confirmation UI
                                pinEntryEditText.setText("");
                                pinEntryTitle.setText(mContext.getString(R.string.parental_text_confirm_pin));
                                pinEntryMessage.setText(mContext.getString(R.string.parental_text_confirm_pin_message));
                                positiveButton.setText(mContext.getString(R.string.text_activate));
                                return false;
                            }
                            if (reEnteringPin && isRightPinEntered(entPinInt)) {
                                isValidPin = true;
                                PrefUtils.getInstance().setPrefParentalControlPIN(entPinInt);
                            } else {
//                    TODO Show wrong PIN UI
                                pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                                pinEntryMessage.setText(mContext.getString(R.string.parental_text_reenter_wrong_pin));
                                pinEntryEditText.setText("");
                                CleverTap.eventParentalControlStatus(CleverTap.INCORRECT_PIN);
                                return false;
                            }
                        } else if (availablePinInt == entPinInt) {
                            isValidPin = true;
                        }
                        if (!isValidPin) {
                            pinEntryMessage.setTextColor(UiUtil.getColor(mContext, R.color.color_eb1d24));
                            pinEntryMessage.setText(mContext.getString(R.string.parental_text_wrong_pin_to_watch));
                            forgotPINNote.setVisibility(View.VISIBLE);
                            pinEntryEditText.setText("");
                            CleverTap.eventParentalControlStatus(CleverTap.INCORRECT_PIN);
                            return false;
                        }
                        if (parentalControlOptionUpdateListener != null)
                            parentalControlOptionUpdateListener.onUpdateOption(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (parentalControlOptionUpdateListener != null)
                            parentalControlOptionUpdateListener.onUpdateOption(false);
                        return true;
                    }
                    return true;
                }

            });

        pinEntryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    positiveButton.performClick();
                }
                return false;
            }
        });

    }

}
