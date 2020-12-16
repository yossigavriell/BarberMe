package dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog extends Dialog{

    Activity parent;
    private Button send;
    private EditText email;

    public ForgotPasswordDialog(Activity parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.forgot_password_dialog);
        send = findViewById(R.id.send_bt);
        email = findViewById(R.id.forgot_password_email);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().length() > 0) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(parent, ForgotPasswordDialog.this.getContext().getResources().getString(R.string.reset_password_email), Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    }
                                    else {
                                        Toast.makeText(parent, ForgotPasswordDialog.this.getContext().getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else
                    Toast.makeText(parent, ForgotPasswordDialog.this.getContext().getResources().getString(R.string.empty_textboxes), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
