package com.myplex.myplex.ui.fragment;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.myplex.myplex.R;
import com.myplex.util.AlertDialogUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentValidateRegister#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentValidateRegister extends BaseFragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = FragmentValidateRegister.class.getSimpleName();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public FragmentValidateRegister() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ValidateRegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentValidateRegister newInstance(String param1, String param2) {
        FragmentValidateRegister fragment = new FragmentValidateRegister();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private CountDownTimer countDownTimer;
    String strValue = "";
    long Mmin, Ssec;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_validate_register, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {
        ImageView iv_back_navigation = view.findViewById(R.id.iv_back_navigation);
        AppCompatEditText et_mobile_number = view.findViewById(R.id.et_mobile_number);
        AppCompatEditText et_otp = view.findViewById(R.id.et_otp);
        TextView btn_resend_otp = view.findViewById(R.id.btn_resend_otp);
        Button btn_register = view.findViewById(R.id.btn_register);
        TextView tv_note_text = view.findViewById(R.id.tv_note_text);
        tv_note_text.setText(getString(R.string.note_text));


        iv_back_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackClicked();
            }
        });

        btn_resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_mobile_number.getText().toString().equals("") || et_mobile_number.getText().toString().length() < 10){
                    AlertDialogUtil.showToastNotification(getString(R.string.otp_msg_invalid_mobile_no));
                }else {
                    resendOtp();
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(et_mobile_number.getText().toString().equals("") || et_mobile_number.getText().toString().length() < 10){
                    AlertDialogUtil.showToastNotification(getString(R.string.otp_msg_invalid_mobile_no));
                }else if(et_otp.getText().toString().equals("")){
                    AlertDialogUtil.showToastNotification(getString(R.string.otp_msg_invalid_otp));
                }else {
                    ValidateOtp();
                }
            }
        });
        NumberFormat f = new DecimalFormat("00");

        countDownTimer =  new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {


                try {
                    Mmin = (millisUntilFinished / 1000) / 60;
                    Ssec = (millisUntilFinished / 1000) % 60;

                    strValue =  f.format(Mmin) + ":" + f.format(Ssec);

                    Log.d(TAG, "onTick strValue: Mmin "+ Mmin + " Ssec :"+ Ssec);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                btn_resend_otp.setText("Resend OTP in "+strValue+" Secs");
                btn_resend_otp.setEnabled(false);

            }
            // When the task is over it will print 00:00:00 there
            public void onFinish() {
                btn_resend_otp.setText("Resend OTP");
                btn_resend_otp.setEnabled(true);
            }
        }.start();

    }

    private void ValidateOtp() {

    }

    private void resendOtp() {
        countDownTimer.start();
    }

    @Override
    public boolean onBackClicked() {
        getActivity().onBackPressed();
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(countDownTimer!=null)
            countDownTimer.cancel();
    }
}