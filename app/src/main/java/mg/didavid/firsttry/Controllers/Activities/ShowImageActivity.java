package mg.didavid.firsttry.Controllers.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import mg.didavid.firsttry.R;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ShowImageActivity extends AppCompatActivity {

    ImageView showImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        //init view
        showImage = findViewById(R.id.showImage);

        //get data from intent
        Intent intent = getIntent();
        String imageUri = "" + intent.getStringExtra("showImage");
        Log.d("valeur d'arrivé", ""+imageUri);

        if (!imageUri.equals("noImage")){
            try {
                //load image to the view
                Picasso.get().load(imageUri).into(showImage);
                //zomm in/out image
                PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(showImage);
                photoViewAttacher.update();
            }catch (Exception e){
                Toast.makeText(this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
