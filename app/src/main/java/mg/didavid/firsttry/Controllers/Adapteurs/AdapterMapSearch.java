package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.Models.UserLocation;
import mg.didavid.firsttry.R;

import static android.graphics.BlendMode.COLOR;

public class AdapterMapSearch extends RecyclerView.Adapter<AdapterMapSearch.MyHolder>{

    final private String TAG = "adapteurUSerList";

    Context context;
    List<Object> objectList;

    String mCurrentUserId;
    CollectionReference collectionReference_resto;

    private AdapterMapSearch.OnMapSearchListner onMapSearchListner;

    public AdapterMapSearch(Context context, List<Object> objectList, AdapterMapSearch.OnMapSearchListner onMapSearchListner) {
        this.context = context;
        this.objectList = objectList;
        this.onMapSearchListner = onMapSearchListner;

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        collectionReference_resto = FirebaseFirestore.getInstance().collection("Resto");
    }

    public AdapterMapSearch() {
    }

    @NonNull
    @Override
    public AdapterMapSearch.MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_map_search, parent, false);
        //return new AdapteurMessage.MyMessageHolder(view);
        return new AdapterMapSearch.MyHolder(view, onMapSearchListner);
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final AdapterMapSearch.MyHolder holder, final int position) {
        //get data
        String name ="null";
        String picture = "null";

        Object object = objectList.get(position);

        try {
            if(object instanceof ModelResto){
                name = ((ModelResto) object).getName_resto();
                picture = ((ModelResto) object).getLogo_resto();

                holder.imageView_type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_culinary_speciality_icon_dark));
                holder.imageView_type.setBackgroundResource(R.drawable.black_background);
            }else if(object instanceof UserLocation){
                name = ((UserLocation) object).getName();
                picture = ((UserLocation) object).getProfile_image();

                holder.imageView_type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_image_profile_icon_dark));
                holder.imageView_type.setBackgroundColor(0xFFFFFF);
            }

            holder.textView_name.setText(name);
            Picasso.get().load(picture).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_picture);
        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return objectList.size();
    }

    //view holder class
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //views from row_userlist.xml
        TextView textView_name;
        ImageView imageView_picture, imageView_type;
        AdapterMapSearch.OnMapSearchListner onMapSearchListner;

        public MyHolder(@NonNull View itemView, AdapterMapSearch.OnMapSearchListner onMapSearchListner) {
            super(itemView);

            this.onMapSearchListner = onMapSearchListner;

            //init views
            textView_name = itemView.findViewById(R.id.textView_name);
            imageView_picture = itemView.findViewById(R.id.imageView_picture);
            imageView_type = itemView.findViewById(R.id.imageView_type);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onMapSearchListner.onMapSearchClick(getAdapterPosition());
        }
    }

    public interface OnMapSearchListner{
        void onMapSearchClick(int position);
    }
}