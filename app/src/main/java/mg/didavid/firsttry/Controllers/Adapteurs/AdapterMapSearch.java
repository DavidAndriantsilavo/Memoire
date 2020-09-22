package mg.didavid.firsttry.Controllers.Adapteurs;

import android.annotation.SuppressLint;
import android.content.Context;
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
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.R;

public class AdapterMapSearch extends RecyclerView.Adapter<AdapterMapSearch.MyHolder>{

    final private String TAG = "adapteurUSerList";

    Context context;
    List<ModelResto> modelRestoList;

    String mCurrentUserId;
    CollectionReference collectionReference_resto;

    private AdapterMapSearch.OnMapSearchListner onMapSearchListner;

    public AdapterMapSearch(Context context, List<ModelResto> modelRestoList, AdapterMapSearch.OnMapSearchListner onMapSearchListner) {
        this.context = context;
        this.modelRestoList = modelRestoList;
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
        String restoName = modelRestoList.get(position).getName_resto();
        String restoLogo = modelRestoList.get(position).getLogo_resto();


        try {
            //set data
            holder.textView_restoName.setText(restoName);
            Picasso.get().load(restoLogo).resize(100, 100).transform(new CropCircleTransformation()).into(holder.imageView_restoLogo);

        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return modelRestoList.size();
    }

    //view holder class
    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //views from row_userlist.xml
        TextView textView_restoName;
        ImageView imageView_restoLogo;
        AdapterMapSearch.OnMapSearchListner onMapSearchListner;

        public MyHolder(@NonNull View itemView, AdapterMapSearch.OnMapSearchListner onMapSearchListner) {
            super(itemView);

            this.onMapSearchListner = onMapSearchListner;

            //init views
            textView_restoName = itemView.findViewById(R.id.textView_restoName);
            imageView_restoLogo = itemView.findViewById(R.id.imageView_restoLogo);

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