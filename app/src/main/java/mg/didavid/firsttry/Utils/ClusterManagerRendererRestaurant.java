package mg.didavid.firsttry.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import mg.didavid.firsttry.Models.ClusterMarkerRestaurant;
import mg.didavid.firsttry.Models.ModelResto;
import mg.didavid.firsttry.R;

public class ClusterManagerRendererRestaurant extends DefaultClusterRenderer<ClusterMarkerRestaurant> {

    private final IconGenerator iconGenerator;
    private ImageView imageView, imageView_marker;
    private final int markerWidth, markerHeight;
    private  View mCustomDefaultMarkerView;

    public ClusterManagerRendererRestaurant(Context context, GoogleMap map,
                                            ClusterManager<ClusterMarkerRestaurant> clusterManager) {
        super(context, map, clusterManager);

        iconGenerator = new IconGenerator(context.getApplicationContext());
//        iconGenerator.setBackground(context.getResources().getDrawable(R.drawable.marker_pin_black));

        mCustomDefaultMarkerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_restaurant_layout, null);
        imageView_marker = mCustomDefaultMarkerView.findViewById(R.id.imageView_marker);

        imageView = new ImageView(context.getApplicationContext());
        markerWidth = 90;
        markerHeight = 90;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = 2;
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull final ClusterMarkerRestaurant item, @NonNull final MarkerOptions markerOptions) {
        ModelResto resto = item.getResto();
        String logeUrl = resto.getLogo_resto();

        Picasso.get().load(logeUrl)
                .resize(100, 100)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        // Todo: Do something with your bitmap here

                        imageView_marker.setImageBitmap(bitmap);

//                        Bitmap icon = iconGenerator.makeIcon();
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomDefaultMarkerView, bitmap))).title(item.getTitle());
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
    }

    @Override
    protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMarkerRestaurant> cluster) {
        return false;
    }

    @Override
    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<ClusterMarkerRestaurant> listener) {
        super.setOnClusterItemClickListener(listener);
    }

    //GET THE BITMAP FROM PICASSO AND PUT IT TO THE INFLATED VIEW FOR THE MARKER
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

//        imageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }
}
