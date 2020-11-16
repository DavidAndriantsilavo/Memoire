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
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import mg.didavid.firsttry.Models.ClusterMarkerUser;
import mg.didavid.firsttry.Models.User;
import mg.didavid.firsttry.Models.UserLocation;
import mg.didavid.firsttry.Models.UserSingleton;
import mg.didavid.firsttry.R;

public class ClusterManagerRendererUser extends DefaultClusterRenderer<ClusterMarkerUser> {

private final IconGenerator iconGenerator;
private ImageView imageView, imageView_marker;
private CardView cardView_border;
private final int markerWidth, markerHeight;
private View mCustomDefaultMarkerView;
private User currentUser;

private final IconGenerator mClusterIconGenerator;
private Context mContext;

public ClusterManagerRendererUser(Context context, GoogleMap map,
                                  ClusterManager<ClusterMarkerUser> clusterManager) {
        super(context, map, clusterManager);

        currentUser = ((UserSingleton) context.getApplicationContext()).getUser();

        mClusterIconGenerator = new IconGenerator(context.getApplicationContext());
        mContext = context;

        iconGenerator = new IconGenerator(context.getApplicationContext());
//        iconGenerator.setBackground(context.getResources().getDrawable(R.drawable.marker_pin_black));

        mCustomDefaultMarkerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_user_layout, null);
        imageView_marker = mCustomDefaultMarkerView.findViewById(R.id.imageView_marker);
        cardView_border = mCustomDefaultMarkerView.findViewById(R.id.cardView_border);

        imageView = new ImageView(context.getApplicationContext());
        markerWidth = 90;
        markerHeight = 90;
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = 2;
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setContentView(imageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(@NonNull final ClusterMarkerUser item, @NonNull final MarkerOptions markerOptions) {
                UserLocation userLocation = item.getUserLocation();
                String logeUrl = userLocation.getProfile_image();
                String user_id = userLocation.getUser_id();

                //change the marker's border color
                if(user_id.equals(currentUser.getUser_id())){
                        cardView_border.setCardBackgroundColor(Color.rgb(00, 85, 77));
                }else{
                        cardView_border.setCardBackgroundColor(Color.BLACK);
                }

                Picasso.get().load(logeUrl)
                .resize(100, 100)
                .transform(new CropCircleTransformation())
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

//        @Override
//        protected boolean shouldRenderAsCluster(@NonNull Cluster<ClusterMarkerUser> cluster) {
//                return true;
//                }

        @Override
        protected void onBeforeClusterRendered(@NonNull Cluster<ClusterMarkerUser> cluster, @NonNull MarkerOptions markerOptions) {
                super.onBeforeClusterRendered(cluster, markerOptions);

//                mClusterIconGenerator.setBackground(ContextCompat.getDrawable(mContext, R.drawable.background_circle));mClusterIconGenerator.setTextAppearance(R.style.AppTheme_WhiteTextAppearance);final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<ClusterMarkerUser> listener) {
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
