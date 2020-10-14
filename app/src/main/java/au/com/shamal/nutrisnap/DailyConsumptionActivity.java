package au.com.shamal.nutrisnap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
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

public class DailyConsumptionActivity extends AppCompatActivity {


    public static final String PREFS = "Prefs";
    private final static String APP = "NutriSnap";
    List<HistoryResultsData> historyResultsDataList;
    private AnyChartView anyChartView;
    //    GoogleSignInClient mGoogleSignInClient;
//    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_consumption);

        anyChartView = findViewById(R.id.any_chart_view);
        anyChartView.setProgressBar(findViewById(R.id.any_chart_progress_bar));

        List<HistoryResultsData> info = (List<HistoryResultsData>) getIntent().getSerializableExtra("info");
        Cartesian cartesian = AnyChart.column();
        List<DataEntry> data = new ArrayList<>();

        for (HistoryResultsData historyData : info) {
            data.add(new ValueDataEntry(historyData.getFood(), historyData.getCalories()));
        }
        Column column = cartesian.column(data);

        column.tooltip()
//                        .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d);
//                        .format("${%Value}");

        cartesian.animation(true);
        cartesian.title("Daily Consumption");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels();// .format("${%Value}");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Consumption");
        cartesian.yAxis(0).title("Calories");

        anyChartView.setChart(cartesian);


        Button gotoDashboardBtn = findViewById(R.id.back_to_dashboard_from_daily_consumption);
        gotoDashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DailyConsumptionActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });
        historyResultsDataList = new ArrayList<>();
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();;
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//        mAuth = FirebaseAuth.getInstance();
//        databaseReference = FirebaseDatabase.getInstance().getReference();

//        firebaseDatabase = FirebaseDatabase.getInstance();
//        databaseReference = firebaseDatabase.getReference("history");
//        ChildEventListener childEventListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                Iterable<DataSnapshot> snapshotIterator = snapshot.getChildren();
//                Iterator<DataSnapshot> iterator = snapshotIterator.iterator();
//
//                HistoryResultsData resultsData = new HistoryResultsData();
//                while (iterator.hasNext()) {
//                    DataSnapshot next = (DataSnapshot) iterator.next();
//                    Log.i(APP, "Key = " + next.getValue());
//                    Log.i(APP, "Value = " + next.getKey());
//                    if (next.getKey().equals("date")){
//                        resultsData.setDate(next.getValue().toString());
//                    } else if (next.getKey().equals("calories")){
//                        float calories = Float.parseFloat(Objects.requireNonNull(next.getValue()).toString());
//                        resultsData.setCalories(calories);
//                    } else if (next.getKey().equals("food")){
//                        resultsData.setFood(next.getValue().toString());
//                    } else if (next.getKey().equals("email")){
//                        resultsData.setEmail(next.getValue().toString());
//                    }
//                }
//                historyResultsDataList.add(resultsData);
//                Cartesian cartesian = AnyChart.column();
//                List<DataEntry> data = new ArrayList<>();
//
//                for (HistoryResultsData historyData : historyResultsDataList) {
//                    data.add(new ValueDataEntry(historyData.getFood(), historyData.getCalories()));
//                }
//                Column column = cartesian.column(data);
//
//                column.tooltip()
////                        .titleFormat("{%X}")
//                        .position(Position.CENTER_BOTTOM)
//                        .anchor(Anchor.CENTER_BOTTOM)
//                        .offsetX(0d)
//                        .offsetY(5d);
////                        .format("${%Value}");
//
//                cartesian.animation(true);
//                cartesian.title("Daily Consumption");
//
//                cartesian.yScale().minimum(0d);
//
//                cartesian.yAxis(0).labels();// .format("${%Value}");
//
//                cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
//                cartesian.interactivity().hoverMode(HoverMode.BY_X);
//
//                cartesian.xAxis(0).title("Consumption");
//                cartesian.yAxis(0).title("Calories");
//
//                anyChartView.setChart(cartesian);
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        };
//        databaseReference.addChildEventListener(childEventListener);



    }
}