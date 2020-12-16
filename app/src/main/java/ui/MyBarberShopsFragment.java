package ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import adapter.BarberShopAdapter;
import model.Consumer;
import model.DatabaseFetch;
import userData.BarberShop;

public class MyBarberShopsFragment extends Fragment implements BarberShopAdapter.MyBarberShopListener{

    FloatingActionButton addBarberShop;
    RecyclerView myBarbersList;
    List<BarberShop> barbers;
    TextView textView;

    @Override
    public void onBarberShopClick(int position, View view) {
        Intent intent = new Intent(MyBarberShopsFragment.this.getContext(),BarberShopActivity.class);
        intent.putExtra("Barbershop",barbers.get(position));
        startActivity(intent);
    }

    @Override
    public void onEditBarberShopClick(int position, View view) {
        Intent intent = new Intent(MyBarberShopsFragment.this.getContext(),EditBarberShopActivity.class);
        intent.putExtra("Barbershop",barbers.get(position));
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_my_barbershops, container, false);


        addBarberShop = rootView.findViewById(R.id.add_button);
        addBarberShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyBarberShopsFragment.this.getContext(), AddBarberShopActivity.class);
                startActivity(intent);
            }
        });
        myBarbersList = rootView.findViewById(R.id.my_barbershops_recycler);
        myBarbersList.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        Consumer<List<BarberShop>> consumerList = new Consumer<List<BarberShop>>() {
            @Override
            public void apply(List<BarberShop> param) {
                barbers = param;
                BarberShopAdapter barberShopAdapter = new BarberShopAdapter(barbers, true, false);
                ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        removeFromDatabase(barbers.get(viewHolder.getAdapterPosition()));
                        barbers.remove(viewHolder.getAdapterPosition());
                        barberShopAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                    }
                };
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(myBarbersList);
                myBarbersList.setAdapter(barberShopAdapter);
                barberShopAdapter.setListener(MyBarberShopsFragment.this);
            }
        };
        DatabaseFetch databaseFetch = new DatabaseFetch();
        databaseFetch.fetchUserBarberShops(consumerList, FirebaseAuth.getInstance().getUid());
        return rootView;
    }

    private void removeFromDatabase(BarberShop barberShop) {
        FirebaseFirestore.getInstance().collection("shops").document(barberShop.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MyBarberShopsFragment.this.getContext(), MyBarberShopsFragment.this.getContext().getResources().getString(R.string.barber_remove), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyBarberShopsFragment.this.getContext(), MyBarberShopsFragment.this.getContext().getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
