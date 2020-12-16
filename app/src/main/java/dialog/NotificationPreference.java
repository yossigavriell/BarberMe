package dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.example.barberme.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import model.Consumer;
import model.DatabaseFetch;
import userData.User;

public class NotificationPreference extends Preference {

    DatabaseFetch databaseFetch = new DatabaseFetch();
    User currentUser;

    public NotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        CheckBox checkBox = (CheckBox) holder.findViewById(R.id.check_box_notif);
        Consumer<User> consumer = new Consumer<User>() {
            @Override
            public void apply(User param) {
                currentUser = param;
                checkBox.setChecked(currentUser.isGettingNotifications());
            }
        };
        databaseFetch.findUserData(consumer, FirebaseAuth.getInstance().getCurrentUser().getUid());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    currentUser.setGettingNotifications(true);
                    updateOnDatabase();
                }
                else
                {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    currentUser.setGettingNotifications(false);
                    updateOnDatabase();
                }
            }
        });
    }

    private void updateOnDatabase() {
        FirebaseFirestore.getInstance().collection("users").document(currentUser.getId())
                .set(currentUser, SetOptions.merge());
    }
}
