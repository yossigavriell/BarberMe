package ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.barberme.R;

public class SignUpFragment extends Fragment {

    EditText firstName;
    EditText lastName;
    EditText email;
    EditText password;
    EditText confirmPassword;
    EditText birthday;
    EditText address;
    RadioGroup radioGroup;
    String radioGender;
    Button backToSignIn;
    Button signUp;
    SignUpListener signUpListener;

    interface SignUpListener {
        void onSignUpFragmentLoginClick();
        void onSignUpFragmentRegisterClick(String firstName,String lastName, String email, String password, String repeatPassword,String gender,String birthday,String address);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        signUpListener = (SignUpListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, container, false);

        firstName=rootView.findViewById(R.id.first_name_et);
        lastName=rootView.findViewById(R.id.last_name_et);
        email = rootView.findViewById(R.id.email_et);
        password = rootView.findViewById(R.id.password_et);
        confirmPassword = rootView.findViewById(R.id.confirm_password_et);
        signUp = rootView.findViewById(R.id.signup_bt);
        birthday=rootView.findViewById(R.id.birthday);
        address =rootView.findViewById(R.id.location);
        radioGender=SignUpFragment.this.getContext().getResources().getString(R.string.male);

        addListenerOnButton(rootView);

        signUp.setOnClickListener(view -> {
            if(signUpListener!=null)
                signUpListener.onSignUpFragmentRegisterClick(firstName.getText().toString(),lastName.getText().toString(), email.getText().toString(), password.getText().toString(), confirmPassword.getText().toString(),radioGender,birthday.getText().toString(),address.getText().toString());
        });

        return  rootView;
    }

    public void addListenerOnButton (View view) {

        radioGroup = (RadioGroup)view.findViewById(R.id.radio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioMale:
                        radioGender=SignUpFragment.this.getContext().getResources().getString(R.string.male);
                        break;
                    case R.id.radioFemale:
                        radioGender=SignUpFragment.this.getContext().getResources().getString(R.string.female);
                        break;
                }
            }
        });

    }
}
