package adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.barberme.R;

import java.util.ArrayList;

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.PictureViewHolder> {

    ArrayList<Uri> pictures;
    Context context;
    PictureListener pictureListener;
    public PictureAdapter(ArrayList<Uri> pictures) {
        this.pictures = pictures;
    }

    public interface PictureListener
    {
        void onClickPicture(int position,View view);
    }

    public void setListener(PictureListener pictureListener)
    {
        this.pictureListener=pictureListener;
    }

    class PictureViewHolder extends RecyclerView.ViewHolder
    {
        ImageView picture;
        public PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            picture = itemView.findViewById(R.id.picture_box);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(pictureListener!=null)
                    {
                        pictureListener.onClickPicture(getAdapterPosition(),v);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pictures_cardview, parent, false);
        PictureViewHolder pictureViewHolder = new PictureViewHolder(view);
        context = parent.getContext();
        return pictureViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Uri picture = pictures.get(position);
        Glide.with(context).load(picture).into(holder.picture);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }
}
