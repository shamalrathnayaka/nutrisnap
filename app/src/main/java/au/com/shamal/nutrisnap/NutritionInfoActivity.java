package au.com.shamal.nutrisnap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class NutritionInfoActivity extends AppCompatActivity {

    public static final String PREFS = "Prefs";
    public static final String DISPLAY_NAME_KEY = "username";
    private final static String APP = "NutriSnap";
    TextView nutritionInfo;
    TextView nutritionLabel;
    TextView dailyIntake;
    TextView lblType;
    TextView lblNutrition;
    TextView lblDI;
    String foodIdentified;
    private TextView foodName;
    private Context context;
    private double caloryCount;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    static LottieAnimationView loadingNutritionDataAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_info);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        context = this;
        foodName = findViewById(R.id.food_name_txt);
//        caloryCount = findViewById(R.id.calory_count_txt);
        Intent intent = getIntent();
        String food = intent.getExtras().getString("food_name");
        foodName.setText(food);

        nutritionInfo = findViewById(R.id.lblNutritionInfo);
        nutritionLabel = findViewById(R.id.lblNutritionLabel);
        dailyIntake = findViewById(R.id.lblDailyIntake);

        lblType = findViewById(R.id.lable_type_txt_view);
        lblNutrition = findViewById(R.id.textViewNutrition);
        lblDI = findViewById(R.id.textViewDI);
        nutritionInfo.setMovementMethod(new ScrollingMovementMethod());
        nutritionLabel.setMovementMethod(new ScrollingMovementMethod());
        dailyIntake.setMovementMethod(new ScrollingMovementMethod());

        getNutritionData("1 "+ food);


        loadingNutritionDataAnimation = (LottieAnimationView)findViewById(R.id.loading_nutrition_data_animation);
        loadingNutritionDataAnimation.playAnimation();
        loadingNutritionDataAnimation.setMinAndMaxProgress(0f, 0.97f);
        loadingNutritionDataAnimation.setVisibility(View.GONE);

        Button backToDashboardBtn = findViewById(R.id.back_to_dashboard_from_nutri_info);
        backToDashboardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NutritionInfoActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

    }

    private void getNutritionData(final String food) {

        RequestParams requestParams = new RequestParams();
        NutritionClient.get(food, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    loadingNutritionDataAnimation.setVisibility(View.VISIBLE);
                    String nutritionType = "";
                    String nutritionText = "";
                    String dailyIntakeText = "";

                    // Nutrition Information
//                    String

                    //int calories = response.getInt("calories");
                    double totalWeight = response.getDouble("totalWeight");

                    JSONObject totalNutrients = response.getJSONObject("totalNutrients");
                    JSONArray keysNutri = totalNutrients.names();

                    if (keysNutri == null){
                        AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(context);
                        }
                        builder.setTitle("Unrecognized Food Item")
                                .setMessage("Do you want to try again with another food ?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mainIntent = new Intent(context, DashboardActivity.class);
                                        startActivity(mainIntent);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        // finishApp();
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }else{
                        JSONObject totalDaily = response.getJSONObject("totalDaily");
                        JSONArray keysDaily = totalDaily.names();

                        NumberFormat formatter = new DecimalFormat("#0.000");

                        for (int i = 0; i < keysNutri.length (); ++i) {
                            String key = keysNutri.getString (i);
                            System.out.println(key);

                            if(key.equals("ENERC_KCAL") || key.equals("FAT") || key.equals("FASAT") ||
                                    key.equals("CHOCDF") || key.equals("FIBTG") || key.equals("SUGAR") ||
                                    key.equals("PROCNT") || key.equals("CHOLE") || key.equals("FE") ||
                                    key.equals("VITA_RAE") || key.equals("VITB12") || key.equals("VITD") ||
                                    key.equals("VITK1"))
                            {

                                JSONObject obj = new JSONObject(totalNutrients.getString(key));
                                String label = obj.getString("label");
                                double qty = obj.getDouble("quantity");
                                String unit = obj.getString("unit");
                                if (key.equals("ENERC_KCAL")){
                                    caloryCount = obj.getDouble("quantity");
                                }

                                Log.d(APP, "Calories: " + caloryCount);

                                nutritionType = nutritionType + label + "\n";
                                nutritionText = nutritionText + formatter.format(qty) + " " + unit + "\n";

                            }
                        }

                        SharedPreferences.Editor editor = getSharedPreferences(PREFS, 0).edit();
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        String emailAddress = currentUser.getEmail();
                        Float calories = (float) caloryCount;
                        String date = String.valueOf(new Date());
                        editor.putString(DISPLAY_NAME_KEY, emailAddress);
//                                editor.putFloat("food", foodIdentified);
                        editor.putFloat("calories", calories);
                        editor.putString("date", date);
                        editor.putString("food", food);
                        editor.apply();
                        HistoryResultsData historyResultsData = new HistoryResultsData(emailAddress, calories, date, food);
                        databaseReference.child("history").push().setValue(historyResultsData);

                        for (int i = 0; i < keysDaily.length (); ++i) {
                            String key = keysDaily.getString (i);
                            System.out.println(key);


                            if(key.equals("ENERC_KCAL") || key.equals("FAT") || key.equals("FASAT") ||
                                    key.equals("CHOCDF") || key.equals("FIBTG") || key.equals("SUGAR") ||
                                    key.equals("PROCNT") || key.equals("CHOLE") || key.equals("FE") ||
                                    key.equals("VITA_RAE") || key.equals("VITB12") || key.equals("VITD") ||
                                    key.equals("VITK1"))
                            {
                                JSONObject obj = new JSONObject(totalDaily.getString(key));
                                double qty = obj.getDouble("quantity");

                                dailyIntakeText = dailyIntakeText + formatter.format(qty) + " %" + "\n";
                            }
                        }
                        nutritionText = nutritionText.replaceAll("\uFFFD", "\u00B5");

                        lblType.setText("Typical Values");
                        lblType.setTextSize(20.0f);
                        lblType.setBackgroundColor(Color.rgb(51, 181, 229));

                        lblNutrition.setText("Per " + (int)totalWeight + " g");
                        lblNutrition.setTextSize(20.0f);
                        lblNutrition.setBackgroundColor(Color.rgb(51, 181, 229));

                        lblDI.setText("Daily Intake");
                        lblDI.setTextSize(20.0f);
                        lblDI.setBackgroundColor(Color.rgb(51, 181, 229));

                        nutritionLabel.setText(nutritionType);
                        nutritionLabel.setTextSize(20.0f);

                        nutritionInfo.setText(nutritionText);
                        nutritionInfo.setTextSize(20.0f);

                        dailyIntake.setText(dailyIntakeText);
                        dailyIntake.setTextSize(20.0f);
                        loadingNutritionDataAnimation.setVisibility(View.GONE);


                        //helper.addData(foodIdentified);
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
                // Pull out the first event on the public timeline

            }
        });
    }
}