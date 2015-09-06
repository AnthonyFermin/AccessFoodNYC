package rayacevedo45.c4q.nyc.accessfoodnyc;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.List;

import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Business;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Location;
import rayacevedo45.c4q.nyc.accessfoodnyc.vendor.PicDialog;


public class DetailsFragment extends Fragment {

    private static final String TAG = DetailsFragment.class.getName();

    private TextView mVendorNameText;
    private ImageView mVendorPicImage;
    private ImageView mVendorRatingImage;
    private ImageButton add;
    private ParseObject selectedVendor;

    private ImageView yelpLogo;

    private static List <String> addList;

    private TextView mCategoriesText;
    private static String mCategories;

    private TextView mPhoneText;
    private TextView mSnippetText;
    private TextView abouttv;

    private static String mId;

    private RecyclerView mRecyclerViewPictures;
    private RecyclerView mRecyclerViewReview;
    private ReviewAdapter mAdapter;
    private PicturesAdapter mPicturesAdapter;

    private boolean isYelp;
    private String objectId;

    private ImageButton cb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        add = (ImageButton) rootView.findViewById(R.id.button_add);
        cb = (ImageButton) rootView.findViewById(R.id.cbid);
        yelpLogo = (ImageView) rootView.findViewById(R.id.yelp_logo);
        mRecyclerViewPictures = (RecyclerView) rootView.findViewById(R.id.recyclerView_details_pictures);
        abouttv = (TextView) rootView.findViewById(R.id.aboutId);

        objectId = getArguments().getString(Constants.EXTRA_KEY_OBJECT_ID);
        isYelp = getArguments().getBoolean(Constants.EXTRA_KEY_IS_YELP);

        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PicDialog picDialog = new PicDialog();

                Bundle bundle = new Bundle();
                bundle.putString(Constants.EXTRA_KEY_OBJECT_ID, objectId);
                bundle.putBoolean(Constants.EXTRA_KEY_IS_YELP, isYelp);

                picDialog.setArguments(bundle);


                picDialog.show(getActivity().getSupportFragmentManager(), "picD");
            }
        });

        mRecyclerViewReview = (RecyclerView) rootView.findViewById(R.id.recyclerView_friends_review);
        //mRecyclerViewReview.setHasFixedSize(true);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        lm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerViewReview.setLayoutManager(lm);

//        RecyclerView.LayoutParams params = new    RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
//        params.setMargins(0,0,0,0);


        LinearLayoutManager lm2 = new LinearLayoutManager(getActivity());
        lm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        //mRecyclerViewPictures.setLayoutParams(params);
        mRecyclerViewPictures.setLayoutManager(lm2);
       // mRecyclerViewPictures.setPadding(0,0,0,0);
      //  mRecyclerViewPictures.fling(10,0);
        mPicturesAdapter = new PicturesAdapter(getActivity());
        mRecyclerViewPictures.setAdapter(mPicturesAdapter);

        ParseUser user = ParseUser.getCurrentUser();
        final ParseRelation<ParseUser> relation = user.getRelation("friends");
        if (isYelp) {

            yelpLogo.setVisibility(View.VISIBLE);
            ParseQuery<ParseObject> findVendor = ParseQuery.getQuery("Vendor");
            findVendor.whereEqualTo("yelpId", objectId).findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (list.size() != 0) {
                        final ParseObject vendor = list.get(0);
                        relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> list, ParseException e) {
                                if (list.size() != 0) {
                                    ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Review");
                                    query1.include("writer");
                                    query1.whereEqualTo("vendor", vendor).whereContainedIn("writer", list);
                                    query1.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> list, ParseException e) {
                                            if (list.size() != 0) {
                                                mAdapter = new ReviewAdapter(getActivity(), list);
                                                mRecyclerViewReview.setAdapter(mAdapter);
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        } else {


            mVendorPicImage = (ImageView) rootView.findViewById(R.id.vendor_pic);
            mVendorNameText = (TextView) rootView.findViewById(R.id.vendor_name);
            mSnippetText = (TextView) rootView.findViewById(R.id.snippet_text);

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject vendor, ParseException e) {

                    ParseRelation<ParseObject> pictures = vendor.getRelation("pictures");
                    pictures.getQuery().findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {

                            if (list.size() != 0) {
                                mPicturesAdapter = new PicturesAdapter(getActivity(), list);
                                mRecyclerViewPictures.setAdapter(mPicturesAdapter);
                                mVendorPicImage.setVisibility(View.GONE);
                                mRecyclerViewPictures.setVisibility(View.VISIBLE);

                                RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                                p.addRule(RelativeLayout.BELOW, R.id.vendor_name);

                                mSnippetText.setLayoutParams(p);
//                                for (ParseObject item : list) {
//                                    ParseFile file = item.getParseFile("data");
//
//                                    file.getDataInBackground(new GetDataCallback() {
//                                        @Override
//                                        public void done(byte[] bytes, ParseException e) {
//                                            mPicturesAdapter.addPicture(bytes);
//                                        }
//                                    });
//                                }
                            } else {
                                abouttv.setVisibility(View.GONE);
                                Picasso.with(getActivity()).load(vendor.getString("profile_url")).centerCrop().resize(350, 350).noFade().into(mVendorPicImage);
                            }


                        }
                    });

                    mSnippetText.setText(vendor.getString("description"));



                }
            });

            relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> list, ParseException e) {

                    ParseQuery<ParseObject> findVendorQuery = ParseQuery.getQuery("Vendor");
                    findVendorQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject vendor, ParseException e) {
                            relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                                @Override
                                public void done(List<ParseUser> list, ParseException e) {
                                    if (list.size() != 0) {
                                        ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Review");
                                        query1.include("writer");
                                        query1.whereEqualTo("vendor", vendor).whereContainedIn("writer", list);
                                        query1.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> list, ParseException e) {
                                                if (list.size() != 0) {
                                                    mAdapter = new ReviewAdapter(getActivity(), list);
                                                    mRecyclerViewReview.setAdapter(mAdapter);
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });

                }
            });
        }

        if (!isYelp) {

        }







        return rootView;
    }

    public void onYelpData(Business business) {

        Log.d(TAG, "inside onYelpData.");
        mVendorNameText = (TextView)getActivity().findViewById(R.id.vendor_name);
        mVendorNameText.setText(business.getName());

        mVendorNameText = (TextView)getActivity().findViewById(R.id.category);
        catListIterator(business);
        mVendorNameText.setText(mCategories);

        mVendorPicImage = (ImageView)getActivity().findViewById(R.id.vendor_pic);
        String businessImgUrl = (business.getImageUrl());
        Picasso.with(getActivity()).load(businessImgUrl).centerCrop().resize(350, 350).noFade().into(mVendorPicImage);

        mVendorRatingImage = (ImageView)getActivity().findViewById(R.id.vendor_rating);
        String vendorRatingUrlLarge = business.getRatingImgUrlLarge();
        Picasso.with(getActivity()).load(vendorRatingUrlLarge).into(mVendorRatingImage);

//        mVendorRatingNum = (TextView)getActivity().findViewById(R.id.rating_num);
//        mVendorRatingNum.setText(String.valueOf(business.getRating()));

        addressGenerator(business);
        LinearLayout linearLayout = (LinearLayout)getActivity().findViewById(R.id.address);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for( int i = 0; i < addList.size(); i++ )
        {
            TextView textView = new TextView(getActivity());
            textView.setText(addList.get(i));
            linearLayout.addView(textView);
        }

        mPhoneText = (TextView)getActivity().findViewById(R.id.phone);
        mPhoneText.setText(business.getDisplayPhone());

        mSnippetText = (TextView)getActivity().findViewById(R.id.snippet_text);
        mPhoneText.setText(business.getSnippetText());

        mId = business.getId();



    }

    public static String catListIterator (Business business){
        List<List<String>> catListOfLists = business.getCategories();
        int i = 0;
        List<String> catList = null;
        mCategories = "";

        while (i < catListOfLists.size()) {
            catList = catListOfLists.get(i);
            mCategories= mCategories + " " + (catList.get(0));
            i++;
        }

        return mCategories;
    }

    public static List<String> addressGenerator(Business business){

        Location bizLocation = business.getLocation();
        addList = bizLocation.getDisplayAddress();

        return addList;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "finished onYelpData.");

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add.setImageResource(R.drawable.favorite_white_oneeightdp);
                final ParseUser user = ParseUser.getCurrentUser();
                final ParseRelation<ParseObject> relation = user.getRelation("favorite");

                //check if the yelpID is already in parse.com or not
                if (isYelp) {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
                    query.whereStartsWith("yelpId", mId);

                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        public void done(final ParseObject object, ParseException e) {
                            if (e == null) {
                                //object exists
                                selectedVendor = object;
                                relation.add(selectedVendor);

                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            ParsePush.subscribeInBackground(object.getObjectId());
                                            Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            } else {
                                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    //object doesn't exist

                                    //add yelpID as new vendor in parse.com
                                    selectedVendor = new ParseObject("Vendor");
                                    selectedVendor.put("yelpId", mId);
                                    selectedVendor.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            relation.add(selectedVendor);

                                            user.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if (e == null) {
                                                        ParsePush.subscribeInBackground(selectedVendor.getObjectId());
                                                        Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    //unknown error, debug
                                }
                            }
                        }
                    });
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
                    query.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject vendor, ParseException e) {
                            relation.add(vendor);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    ParsePush.subscribeInBackground(objectId);
                                    Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }

            }
        });
    }
}

