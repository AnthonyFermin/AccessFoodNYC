package rayacevedo45.c4q.nyc.accessfoodnyc;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Business;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Coordinate;


public class VendorListAdapter extends RecyclerView.Adapter<VendorListAdapter.VendorViewHolder> {

    private Context mContext;
    private List<Vendor> mList;
    private ParseGeoPoint mPoint;

    public VendorListAdapter(Context context, ParseGeoPoint point) {
        mContext = context;
        mList = new ArrayList<>();
        mPoint = point;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addVendor(Vendor vendor) {
        mList.add(vendor);
        notifyItemInserted(mList.size() - 1);
        notifyItemChanged(mList.size() - 1);
    }

    public int getPosition(Marker marker) {
        for (int i = 0; i < mList.size(); i++) {
            Vendor vendor = mList.get(i);
            if (vendor.getMarker().equals(marker))
                return i;
        }
        return -1;
    }

    public Vendor getItem(int position) {
        return mList.get(position);
    }

    public void sortByDistance() {
        Collections.sort(mList, new Comparator<Vendor>() {
            @Override
            public int compare(Vendor lhs, Vendor rhs) {
                double distanceLhs = mPoint.distanceInMilesTo(lhs.getLocation());
                double distanceRhs = mPoint.distanceInMilesTo(rhs.getLocation());
                return Double.compare(distanceLhs, distanceRhs);
            }
        });
        notifyDataSetChanged();
    }

    public void sortByRating() {
        Collections.sort(mList, new Comparator<Vendor>() {
            @Override
            public int compare(Vendor lhs, Vendor rhs) {
                return Double.compare(rhs.getRating(), lhs.getRating());
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public VendorViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.list_item_vendor, parent, false);
        return new VendorViewHolder(row);
    }


    @Override
    public void onBindViewHolder(VendorViewHolder holder, int position) {
        DecimalFormat df = new DecimalFormat("#.0");

        Vendor vendor = getItem(position);

        holder.name.setText(vendor.getName());
        holder.address.setText(vendor.getAddress());

        double rate = vendor.getRating();
        String rating = df.format(rate);
        holder.rating.setText(rating);
        holder.category.setText(vendor.getCategory());
        ParseGeoPoint location = vendor.getLocation();
        double distance = mPoint.distanceInMilesTo(location);
        holder.distance.setText(df.format(distance) + " miles away");

        Picasso.with(mContext).load(vendor.getPicture()).into(holder.thumbnail);

        String hour = vendor.getHours();
        if (!hour.equals("closed")) {
            try {
                JSONObject info = new JSONObject(hour);
                String opening = info.getString("openAt");
                String closing = info.getString("closeAt");
                long current = System.currentTimeMillis();
                Date open = new Date(current);
                Date close = new Date(current);
                Date now = new Date(current);
                open.setHours(Integer.valueOf(opening.substring(0,2)));
                open.setMinutes(Integer.valueOf(opening.substring(2)));
                close.setHours(Integer.valueOf(closing.substring(0,2)));
                close.setMinutes(Integer.valueOf(closing.substring(2)));
                String minutes = close.getMinutes() + "";
                if (now.after(open) && now.before(close)) {
                    if (close.getMinutes() == 0) {
                        minutes = "00";
                    }
                    if (close.getHours() > 12) {
                        holder.hour.setText("Open until " + (close.getHours() - 12) + ":" + minutes + " PM");
                    } else {
                        holder.hour.setText("Open until " + close.getHours() + ":" + minutes + " AM");
                    }
                } else {
                    holder.hour.setText("Closed now");
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        } else {
            holder.hour.setText("Closed today");
        }

        if (vendor.isYelp()) {
            holder.icon.setVisibility(View.GONE);
            holder.hour.setVisibility(View.GONE);
            holder.yelpLogo.setVisibility(View.VISIBLE);
        } else {
            holder.icon.setVisibility(View.VISIBLE);
            holder.hour.setVisibility(View.VISIBLE);
            holder.yelpLogo.setVisibility(View.GONE);
        }


//        switch (friends.size()) {
//            case 5:
//                holder.friend5.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(4).getParseUser("follower").getString("profile_url")).into(holder.friend5);
//                holder.favorited.setVisibility(View.VISIBLE);
//            case 4:
//                holder.friend4.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(3).getParseUser("follower").getString("profile_url")).into(holder.friend4);
//            case 3:
//                holder.friend3.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(2).getParseUser("follower").getString("profile_url")).into(holder.friend3);
//            case 2:
//                holder.friend2.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(1).getParseUser("follower").getString("profile_url")).into(holder.friend2);
//            case 1:
//                holder.friend1.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(0).getParseUser("follower").getString("profile_url")).into(holder.friend1);
//                holder.favorited.setText(" favorited this place!");
//                break;
//            case 0:
//                holder.friend5.setVisibility(View.GONE);
//                holder.friend4.setVisibility(View.GONE);
//                holder.friend3.setVisibility(View.GONE);
//                holder.friend2.setVisibility(View.GONE);
//                holder.friend1.setVisibility(View.GONE);
//                holder.more.setVisibility(View.GONE);
//                break;
//            default:
//                holder.friend5.setVisibility(View.VISIBLE);
//                holder.friend4.setVisibility(View.VISIBLE);
//                holder.friend3.setVisibility(View.VISIBLE);
//                holder.friend2.setVisibility(View.VISIBLE);
//                holder.friend1.setVisibility(View.VISIBLE);
//                Picasso.with(mContext).load(friends.get(4).getParseUser("follower").getString("profile_url")).into(holder.friend5);
//                Picasso.with(mContext).load(friends.get(3).getParseUser("follower").getString("profile_url")).into(holder.friend4);
//                Picasso.with(mContext).load(friends.get(2).getParseUser("follower").getString("profile_url")).into(holder.friend3);
//                Picasso.with(mContext).load(friends.get(1).getParseUser("follower").getString("profile_url")).into(holder.friend2);
//                Picasso.with(mContext).load(friends.get(0).getParseUser("follower").getString("profile_url")).into(holder.friend1);
//                holder.more.setVisibility(View.VISIBLE);
//        }
//        List<String> address = DetailsFragment.addressGenerator(business);
//        holder.address.setText(address.get(0) + ", " + address.get(1));
    }

    public static class VendorViewHolder extends RecyclerView.ViewHolder {

        protected ImageView thumbnail;
        protected TextView name;
        protected TextView address;
        protected ImageView yelpLogo;
        protected TextView hour;
        protected TextView rating;
        protected ImageView icon;
        protected TextView distance;

        protected TextView category;
        protected ImageView friend1;
        protected ImageView friend2;
        protected ImageView friend3;
        protected ImageView friend4;
        protected ImageView friend5;
        protected ImageView more;
        protected TextView favorited;

        public VendorViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.imageView_vendor);
            name = (TextView) itemView.findViewById(R.id.vendor_name);
            address = (TextView) itemView.findViewById(R.id.textView_address);
            yelpLogo = (ImageView) itemView.findViewById(R.id.yelp_logo);
            hour = (TextView) itemView.findViewById(R.id.textView_hour);
            rating = (TextView) itemView.findViewById(R.id.maps_vendor_rating);
            icon = (ImageView) itemView.findViewById(R.id.schedule_icon);
            distance = (TextView) itemView.findViewById(R.id.maps_distance);

            category = (TextView) itemView.findViewById(R.id.vendor_category);
            friend1 = (ImageView) itemView.findViewById(R.id.friend1);
            friend2 = (ImageView) itemView.findViewById(R.id.friend2);
            friend3 = (ImageView) itemView.findViewById(R.id.friend3);
            friend4 = (ImageView) itemView.findViewById(R.id.friend4);
            friend5 = (ImageView) itemView.findViewById(R.id.friend5);
            more = (ImageView) itemView.findViewById(R.id.friend_more);
            favorited = (TextView) itemView.findViewById(R.id.friend_favorited);
        }

    }
}
