package rayacevedo45.c4q.nyc.accessfoodnyc;

import android.graphics.drawable.Drawable;
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

import com.parse.CountCallback;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Business;
import rayacevedo45.c4q.nyc.accessfoodnyc.api.yelp.models.Location;
import rayacevedo45.c4q.nyc.accessfoodnyc.vendor.PicDialog;


public class DetailsFragment extends Fragment {

    private static final String TAG = DetailsFragment.class.getName();


    private TextView mVendorNameText;
    private TextView mTextViewVendorAddress;
    private TextView mSnippetText;

    private ImageView mVendorPicImage;
    private Button add;
    private ParseObject selectedVendor;
    private ImageView yelpLogo;
    private static List <String> addList;

    private static String mId;
    private RecyclerView mRecyclerViewPictures;
    private PicturesAdapter mPicturesAdapter;
    private boolean isYelp;
    private String objectId;
    private Button cb;

    private LinearLayout mParentFavoritedFriends;
    private NoScrollAdapter<ParseUser> mFavoritedFriendsAdapter;

    private TextView countFavs;
    //private TextView numberOfRatings;
    private TextView ratings;

    private LinearLayout mParentLayout;
    private NoScrollAdapter<ParseObject> mNoScrollAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        add = (Button) rootView.findViewById(R.id.button_add);
        cb = (Button) rootView.findViewById(R.id.cbid);

        yelpLogo = (ImageView) rootView.findViewById(R.id.yelp_logo);
        mVendorNameText = (TextView) rootView.findViewById(R.id.vendor_name);
        mVendorPicImage = (ImageView) rootView.findViewById(R.id.vendor_pic);
        mSnippetText = (TextView) rootView.findViewById(R.id.snippet_text);
        mTextViewVendorAddress = (TextView) rootView.findViewById(R.id.vendor_address);


        mRecyclerViewPictures = (RecyclerView) rootView.findViewById(R.id.recyclerView_details_pictures);
        countFavs = (TextView) rootView.findViewById(R.id.count_favs);
        //numberOfRatings = (TextView) rootView.findViewById(R.id.number_of_ratings);
        ratings = (TextView) rootView.findViewById(R.id.ratings);
        mParentLayout = (LinearLayout) rootView.findViewById(R.id.review_container);
        mParentFavoritedFriends = (LinearLayout) rootView.findViewById(R.id.parent_favorited_friends);

        objectId = getArguments().getString(Constants.EXTRA_KEY_OBJECT_ID);
        isYelp = getArguments().getBoolean(Constants.EXTRA_KEY_IS_YELP);

        mNoScrollAdapter = new NoScrollAdapter<>(getActivity(), mParentLayout);
        mFavoritedFriendsAdapter = new NoScrollAdapter<>(getActivity(), mParentFavoritedFriends);

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

        LinearLayoutManager lm2 = new LinearLayoutManager(getActivity());
        lm2.setOrientation(LinearLayoutManager.HORIZONTAL);
        //mRecyclerViewPictures.setLayoutParams(params);
        mRecyclerViewPictures.setLayoutManager(lm2);
       // mRecyclerViewPictures.setPadding(0,0,0,0);
      //  mRecyclerViewPictures.fling(10,0);
        mPicturesAdapter = new PicturesAdapter(getActivity());
        mRecyclerViewPictures.setAdapter(mPicturesAdapter);



        final ParseUser user = ParseUser.getCurrentUser();
        final ParseRelation<ParseUser> relation = user.getRelation("friends");

        if (isYelp) {

            yelpLogo.setVisibility(View.VISIBLE);
            ParseQuery<ParseObject> findVendor = ParseQuery.getQuery("Vendor");
            findVendor.whereEqualTo("yelpId", objectId).getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject vendor, ParseException e) {
                    if (vendor != null) {

                        getCountFavs(vendor);
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

                                                mNoScrollAdapter.addReviews(list);

//                                                mAdapter = new ReviewAdapter(getActivity(), list);
//                                                mRecyclerViewReview.setAdapter(mAdapter);
                                            }
                                        }
                                    });

                                    ParseQuery<ParseObject> favorites = ParseQuery.getQuery("Favorite");
                                    favorites.include("follower");
                                    favorites.whereEqualTo("vendor", vendor).whereContainedIn("follower", list);
                                    favorites.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> list, ParseException e) {
                                            if (list.size() != 0) {
                                                List<ParseUser> friends = new ArrayList<ParseUser>();
                                                for (ParseObject favorite : list) {
                                                    ParseUser friend = favorite.getParseUser("follower");
                                                    friends.add(friend);
                                                    //mFriendsAdapter.addFriend(friend);
                                                }
                                                mFavoritedFriendsAdapter.addFavoritedFriends(friends);
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

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject vendor, ParseException e) {

                    getCountFavs(vendor);
                    setRatings(vendor.getDouble("rating"));

                    mVendorNameText.setText(vendor.getString("name"));
                    mTextViewVendorAddress.setText(vendor.getString("address"));
                    mSnippetText.setText(vendor.getString("description"));


                    ParseQuery<ParseObject> pictures = ParseQuery.getQuery("Picture");
                    pictures.whereEqualTo("vendor", vendor).findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> list, ParseException e) {
                            if (list.size() != 0) {
                                mPicturesAdapter = new PicturesAdapter(getActivity(), list);
                                mRecyclerViewPictures.setAdapter(mPicturesAdapter);
                                mRecyclerViewPictures.setVisibility(View.VISIBLE);
                                mVendorPicImage.setVisibility(View.GONE);

                            } else {
                                Picasso.with(getActivity()).load(vendor.getString("profile_url")).into(mVendorPicImage);
                            }
                        }
                    });


                }
            });

            relation.getQuery().findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(final List<ParseUser> list, ParseException e) {

                    ParseQuery<ParseObject> findVendorQuery = ParseQuery.getQuery("Vendor");
                    findVendorQuery.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(final ParseObject vendor, ParseException e) {

                            if (list.size() != 0) {
                                ParseQuery<ParseObject> query1 = ParseQuery.getQuery("Review");
                                query1.include("writer");
                                query1.whereEqualTo("vendor", vendor).whereContainedIn("writer", list);
                                query1.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> list, ParseException e) {
                                        if (list.size() != 0) {
                                            mNoScrollAdapter.addReviews(list);
                                        }
                                    }
                                });

                                ParseQuery<ParseObject> favorites = ParseQuery.getQuery("Favorite");
                                favorites.include("follower");
                                favorites.whereEqualTo("vendor", vendor).whereContainedIn("follower", list);
                                favorites.findInBackground(new FindCallback<ParseObject>() {
                                    @Override
                                    public void done(List<ParseObject> list, ParseException e) {
                                        if (list.size() != 0) {
                                            List<ParseUser> friends = new ArrayList<ParseUser>();
                                            for (ParseObject favorite : list) {
                                                ParseUser friend = favorite.getParseUser("follower");
                                                friends.add(friend);
                                                //mFriendsAdapter.addFriend(friend);
                                            }
                                            mFavoritedFriendsAdapter.addFavoritedFriends(friends);

                                        }
                                    }
                                });

                                ParseQuery<ParseObject> favorites2 = ParseQuery.getQuery("Favorite");
                                favorites2.whereEqualTo("vendor", vendor).whereEqualTo("follower", user);
                                favorites2.getFirstInBackground(new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject parseObject, ParseException e) {
                                        if (parseObject == null) {
                                            addButtonUnfavorited();
                                        } else {
                                            addButtonFavorited();
                                        }
                                    }
                                });
                            }

                        }
                    });

                }
            });
        }


        return rootView;
    }

    public void onYelpData(Business business) {
        mVendorNameText.setText(business.getName());
        mVendorPicImage.setVisibility(View.VISIBLE);
        String businessImgUrl = (business.getImageUrl());
        Picasso.with(getActivity()).load(businessImgUrl).into(mVendorPicImage);
        List<String> address = addressGenerator(business);
        if (address.size() >= 2) {
            mTextViewVendorAddress.append(address.get(0) + ", " + address.get(1));
        } else if (address.size() == 1) {
            mTextViewVendorAddress.append(address.get(0));
        }
        mSnippetText = (TextView)getActivity().findViewById(R.id.snippet_text);
        mId = business.getId();
        //numberOfRatings.setText(" " + business.getReviewCount());
        setRatings(business.getRating());
    }

    public void setRatings(Double rating) {
        ratings.setText(rating + "");
        if (rating >= 4.5) {
            ratings.setBackgroundResource(R.drawable.circle_5);
        } else if (rating >= 4.0) {
            ratings.setBackgroundResource(R.drawable.circle_4);
        } else if (rating >= 3.5) {
            ratings.setBackgroundResource(R.drawable.circle_3);
        } else if (rating >= 3.0) {
            ratings.setBackgroundResource(R.drawable.circle_2);
        } else {
            ratings.setBackgroundResource(R.drawable.circle_1);
        }
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
            public void onClick(final View v) {


                final ParseUser user = ParseUser.getCurrentUser();

                String currentText = add.getText().toString();
                if (currentText.equals("ADD TO FAVORITE")) {

                    if (isYelp) {
                        ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
                        query.whereStartsWith("yelpId", mId);
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            public void done(final ParseObject vendor, ParseException e) {
                                if (e == null) {
                                    //object exists
                                    addToFavorite(user, vendor);
                                } else {
                                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                        //object doesn't exist
                                        //add yelpID as new vendor in parse.com
                                        selectedVendor = new ParseObject("Vendor");
                                        selectedVendor.put("yelpId", mId);
                                        selectedVendor.saveInBackground(new SaveCallback() {
                                            @Override
                                            public void done(ParseException e) {
                                                addToFavorite(user, selectedVendor);
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
                                addToFavorite(user, vendor);
                            }
                        });
                    }

                } else {

                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Vendor");
                    query.getInBackground(objectId, new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject vendor, ParseException e) {
                            removeFromFavorite(user, vendor);
                            
                        }
                    });

                }

            }
        });
    }

    private void removeFromFavorite(ParseUser user, ParseObject vendor) {
        ParsePush.unsubscribeInBackground(vendor.getObjectId());
        final ParseQuery<ParseObject> favorites = ParseQuery.getQuery("Favorite");
        favorites.whereEqualTo("follower", user).whereEqualTo("vendor", vendor);
        favorites.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject favorite, ParseException e) {
                if (favorite != null) {
                    favorite.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            addButtonUnfavorited();
                            Toast.makeText(getActivity(), "Removed from favorites!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void addToFavorite(ParseUser user, ParseObject vendor) {
        ParsePush.subscribeInBackground(vendor.getObjectId());
        ParseObject favorite = new ParseObject("Favorite");
        favorite.put("follower", user);
        favorite.put("vendor", vendor);
        favorite.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                addButtonFavorited();
                Toast.makeText(getActivity(), "Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCountFavs(ParseObject vendor) {
        ParseQuery<ParseObject> fav = ParseQuery.getQuery("Favorite");
        fav.whereEqualTo("vendor", vendor).countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                countFavs.setText(i + "");
            }
        });
    }

    private void addButtonFavorited() {
        add.setText("FAVORITED!");
        add.setTextColor(getResources().getColor(R.color.white));
        add.setBackgroundResource(R.drawable.rounded_corner);
        add.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white_24dp, 0, 0, 0);
        add.setPaddingRelative(40,0,0,0);

    }

    private void addButtonUnfavorited() {
        add.setText("ADD TO FAVORITE");
        add.setTextColor(getResources().getColor(R.color.accentColor));
        add.setBackgroundResource(R.drawable.buttons);
        add.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_black_24dp, 0, 0, 0);
        add.setPaddingRelative(40, 0, 0, 0);
    }
}

