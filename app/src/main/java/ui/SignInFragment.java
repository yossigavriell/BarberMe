package ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.barberme.R;
import com.google.android.material.textfield.TextInputEditText;

public class SignInFragment extends Fragment {

    EditText email;
    EditText password;
    TextView forgotPassword;
    Button signin;
    Button signup;
    Button guestSignin;
    SignInListener signInListener;

    interface SignInListener {
        void onSignInFragmentLoginClick(String email, String password);
        void onSignInFragmentRegisterClick();
        void onForgotPasswordClick();
        void onGuestLoginClick();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        signInListener = (SignInListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        email = rootView.findViewById(R.id.email_et);
        password = rootView.findViewById(R.id.password_et);
        signin = rootView.findViewById(R.id.login_bt);
        signup = rootView.findViewById(R.id.signup_bt);
        forgotPassword = rootView.findViewById(R.id.forgot_password_tv);
        guestSignin = rootView.findViewById(R.id.guest_login_bt);

        signin.setOnClickListener(view -> {
            if(signInListener != null)
                signInListener.onSignInFragmentLoginClick(email.getText().toString(), password.getText().toString());
        });

        signup.setOnClickListener(view -> {
            if(signInListener != null)
                signInListener.onSignInFragmentRegisterClick();
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signInListener != null)
                    signInListener.onForgotPasswordClick();
            }
        });
        guestSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(signInListener != null)
                    signInListener.onGuestLoginClick();
            }
        });

        return  rootView;
    }
}
