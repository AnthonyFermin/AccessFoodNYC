package rayacevedo45.c4q.nyc.accessfoodnyc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rayacevedo45.c4q.nyc.accessfoodnyc.R;

public class ReviewsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_reviews, container, false);

        return result;
    }
}
