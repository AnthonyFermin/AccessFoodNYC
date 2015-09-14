package c4q.nyc.take2.accessfoodnyc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import c4q.nyc.take2.accessfoodnyc.api.yelp.models.Business;
import c4q.nyc.take2.accessfoodnyc.api.yelp.service.ServiceGenerator;
import c4q.nyc.take2.accessfoodnyc.api.yelp.service.YelpBusinessSearchService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UserReviewActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private UserReviewAdapter mAdapter;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_review);
        mToolbar = (Toolbar) findViewById(R.id.toolbar_user_reviews);
        setSupportActionBar(mToolbar);
        ParseUser user = ParseUser.getCurrentUser();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(user.getString("first_name") + " " + user.getString("last_name") + "'s Reviews");

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_user_reviews);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(lm);

        mAdapter = new UserReviewAdapter(getApplicationContext());
        mRecyclerView.setAdapter(mAdapter);

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        final String today = "day" + Integer.toString(day);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Review");
        query.include("vendor");
        query.whereEqualTo("writer", user).findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                for (final ParseObject review : list) {

                    final ParseObject vendor = review.getParseObject("vendor");
                    if (vendor.getParseGeoPoint("location") == null) {

                        YelpBusinessSearchService yelpBizService = ServiceGenerator.createYelpBusinessSearchService();
                        yelpBizService.searchBusiness(vendor.getString("yelpId"), new Callback<Business>() {
                            @Override
                            public void success(Business business, Response response) {
                                Vendor truck = new Vendor.Builder(business.getId())
                                        .setRating(business.getRating())
                                        .setPicture(business.getImageUrl())
                                        .setAddress(DetailsFragment.addressGenerator(business).get(0))
                                        .isYelp(true).setName(business.getName()).build();
                                final Review item = new Review();
                                item.setTitle(review.getString("title"));
                                item.setDescription(review.getString("description"));
                                item.setRating(review.getInt("rating"));
                                item.setDate(review.getCreatedAt());
                                item.setVendor(truck);
                                mAdapter.addReview(item);
                            }

                            @Override
                            public void failure(RetrofitError error) {

                            }
                        });

                    } else {

                        HashMap<String, Object> params = new HashMap<String, Object>();
                        params.put("vendor", vendor);
                        ParseCloud.callFunctionInBackground("averageRatings", params, new FunctionCallback<Float>() {
                            @Override
                            public void done(Float rate, ParseException e) {
                                if (rate == null) {
                                    rate = 4.0f;
                                }
                                Vendor truck = new Vendor.Builder(vendor.getObjectId())
                                        .setRating(rate)
                                        .setPicture(vendor.getString("profile_url"))
                                        .setAddress(vendor.getString("address"))
                                        .setHours(vendor.getString(today))
                                        .isYelp(false).setName(vendor.getString("name")).build();
                                final Review item = new Review();
                                item.setTitle(review.getString("title"));
                                item.setDescription(review.getString("description"));
                                item.setRating(review.getInt("rating"));
                                item.setDate(review.getCreatedAt());
                                item.setVendor(truck);
                                mAdapter.addReview(item);

                            }
                        });


                    }

                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_review, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
