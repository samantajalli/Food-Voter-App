package com.android.summer.csula.foodvoter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.summer.csula.foodvoter.demos.FirebasePollBusinesses;
import com.android.summer.csula.foodvoter.models.UserVote;
import com.android.summer.csula.foodvoter.yelpApi.models.Business;
import com.android.summer.csula.foodvoter.yelpApi.models.Coordinate;
import com.android.summer.csula.foodvoter.yelpApi.models.Location;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ListActivity extends AppCompatActivity implements RVoteAdapter.ListItemClickListener, RVoteAdapter.SwitchListener {

    private RVoteAdapter rVoteAdapter;
    private RecyclerView rVoteRecyclerView;
    private List<Business> rChoiceData;
    private Toast mToast;
    public static final String ANONYMOUS = "anonymous";
    //private Button mSendVoteBtn;
    private final static String TAG = "ListActivity";

    private Business votedBusiness;
    private String mUsername = ANONYMOUS;

    private static final int ATASK_LOADER_ID = 0;

    //for testing
    private String latitude = "34.066530";
    private String longtitude = "-118.166560";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        //mSendVoteBtn = (Button) findViewById(R.id.rv_vote_btn);

        rVoteRecyclerView = (RecyclerView) findViewById(R.id.rv_vote_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rVoteRecyclerView.setLayoutManager(layoutManager);

        rVoteRecyclerView.setHasFixedSize(true);

        try{
            //ToDo: remove this line when using firebase
            //rChoiceData = DataRetriever.getRestaurants(latitude,longtitude).getBusinesses();

            //Todo: get data from firebase

            FirebasePollBusinesses.execute(new FirebasePollBusinesses.OnFirebaseResultListener() {

                @Override

                public void onResult(List<Business> businesses) {

                    for(Business business : businesses){
                        Log.d("coordinates", business.getCoordinate().toString());
                    }
                    if(rChoiceData == null){
                        rChoiceData = businesses;
                    }else{
                        rVoteAdapter.swapData(businesses);
                    }
                }


            });
            rVoteAdapter = new RVoteAdapter(this,rChoiceData,this,this);

        }catch(Exception e){

            rVoteAdapter = new RVoteAdapter(this,null,this,this);
            e.printStackTrace();
            Log.d(TAG, "Error: Failed to get data for rVoteAdapter");
        }
        rVoteRecyclerView.setAdapter(rVoteAdapter);

    }
    /**
     //-----------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        getSupportLoaderManager().restartLoader(ATASK_LOADER_ID, null, this);
    }

    //ToDo: remove AsyncTaskLoader when we use firebase
    @Override
    public Loader<Yelp> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Yelp>(this) {

            Yelp mCacheData = null;

            @Override
            protected void onStartLoading() {
                if (mCacheData != null) {
                    // Delivers any previously loaded data immediately
                    deliverResult(mCacheData);
                } else {
                    // Force a new load
                    forceLoad();
                }
            }

            @Override
            public Yelp loadInBackground() {
                try{
                    //sync data -- pull data from api in json form then insert into database
                    Yelp yelp = DataRetriever.getRestaurants(latitude,longtitude);

                    //get cursor by querying database
                    return yelp;

                }catch (Exception e){
                    Log.d(TAG, "Failed to load data in the backgroud");
                    e.printStackTrace();
                    return null;
                }
            }

            public void deliverResult(Yelp yelp) {
                mCacheData = yelp;
                super.deliverResult(yelp);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Yelp> loader, Yelp data) {
        rVoteAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<Yelp> loader) {
        rVoteAdapter.swapData(null);
    }
    // end of AsyncTaskLoader-------------------------------------------------------------------------------------
    */
    @Override
    public void onListItemClick(Business business) {

        /**if(mToast != null){
            mToast.cancel();
        }
        String toastMessage = "Item " + business.getName() + " has been clicked.";
        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();**/

        Intent intent = new Intent(this, DetailActivity.class);

        Log.d(TAG, "intent passed details " + business.getCoordinate().getLatitude() + " " + business.getCoordinate().getLongitude());

        Location location = new Location();
        Coordinate coordinate = new Coordinate();
        location = business.getLocation();
        coordinate = business.getCoordinate();
        String address = location.getDisplayAddress().toString().substring(1,(location.getDisplayAddress().toString().length())-1);
        String rating = String.valueOf(business.getRating());

        intent.putExtra("display_phone", business.getDisplayPhone());
        intent.putExtra("name", business.getName());
        intent.putExtra("image_url", business.getImageUrl());
        intent.putExtra("url", business.getUrl());
        intent.putExtra("display_address", address);
        intent.putExtra("ratings", rating);
        intent.putExtra("price", business.getPrice());

        intent.putExtra("latitude", String.valueOf(coordinate.getLatitude()));
        intent.putExtra("longtitude", String.valueOf( coordinate.getLatitude()));



        Log.d("ppp", (business.getPrice()));
//        Log.d("ppp", String.valueOf(lon));

        startActivity(intent);

    }



    @Override
    public void onSwitchSwiped(Business business, boolean swiped) {
        if(mToast != null){
            mToast.cancel();
        }
        String toastMessage = "";
        if(swiped){
            toastMessage = "Voted for " + business.getName();
        }else{
            toastMessage = "Switched off for " + business.getName();
        }
        votedBusiness = business;
        Log.d(TAG, "Voted for: " + votedBusiness);

        mToast = Toast.makeText(this, toastMessage, Toast.LENGTH_LONG);
        mToast.show();
    }

    //For SendMyVote Button
    public void sendVote(View v){
        String message = "Send my vote for " + votedBusiness.getName();
        //mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        //mToast.show();
        DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            String userid = user.getUid();
            UserVote userVote = new UserVote(userid, votedBusiness);
            dbReference.push().setValue(userVote);
        }else{
            mToast = Toast.makeText(this, "Cannot find user. Failed to vote...", Toast.LENGTH_LONG);
            mToast.show();
        }

    }

}
