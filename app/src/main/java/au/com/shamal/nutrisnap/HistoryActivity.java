package au.com.shamal.nutrisnap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity {

    public static final String PREFS = "Prefs";
    private final static String APP = "NutriSnap";
    GoogleSignInClient mGoogleSignInClient;
    List<HistoryResultsData> historyResultsDataList;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;
    private ListView historyListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyResultsDataList = new ArrayList<>();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();;
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("history");

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Iterable<DataSnapshot> snapshotIterator = snapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();

                HistoryResultsData resultsData = new HistoryResultsData();
                while (iterator.hasNext()) {
                    DataSnapshot next = (DataSnapshot) iterator.next();
                    Log.i(APP, "Key = " + next.getValue());
                    Log.i(APP, "Value = " + next.getKey());
                    if (next.getKey().equals("date")){
                        resultsData.setDate(next.getValue().toString());
                    } else if (next.getKey().equals("calories")){
                        float calories = Float.parseFloat(Objects.requireNonNull(next.getValue()).toString());
                        resultsData.setCalories(calories);
                    } else if (next.getKey().equals("food")){
                        resultsData.setFood(next.getValue().toString());
                    } else if (next.getKey().equals("email")){
                        resultsData.setEmail(next.getValue().toString());
                    }
                }
                historyResultsDataList.add(resultsData);
                populateListView();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addChildEventListener(childEventListener);

        historyListView = findViewById(R.id.search_history_list_view);

        Button gotoConsumptionChartBtn = findViewById(R.id.consumption_chart_btn);
        gotoConsumptionChartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, DailyConsumptionActivity.class);
                intent.putExtra("info", (Serializable) historyResultsDataList);
                startActivity(intent);
            }
        });

    }

    private void populateListView() {

        ArrayList<String> listData = new ArrayList<>();

        int i = 0;
        while(i < historyResultsDataList.size()){
            listData.add(historyResultsDataList.get(i).getFood()
                    + " searched on "
                    + historyResultsDataList.get(i).getDate()
                    + " which contained "
                    + historyResultsDataList.get(i).getCalories()
                    + " kcal");
            i++;
        }

        ListAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listData);
        historyListView.setAdapter(adapter);
    }
}