package au.com.shamal.nutrisnap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class DashboardActivity extends AppCompatActivity {

    private static final String APP = "NutriSnap";
    GoogleSignInClient mGoogleSignInClient;
    private Button historyLookupBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // just the bottom button
//        Button historyBtn = (Button) findViewById(R.id.history_btn);
//        String userLevel = getIntent().getStringExtra("USER_LEVEL");
//
//        if (userLevel.equals("Guest")) {
//            historyBtn.setVisibility(View.INVISIBLE);
//        } else {
//            historyBtn.setVisibility(View.VISIBLE);
//        }
//
//        historyBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(DashboardActivity.this, "History", Toast.LENGTH_SHORT).show();
//            }
//        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();;
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        historyLookupBtn = (Button) findViewById(R.id.history_lookup_btn);
        historyLookupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        Button logoutBtn = (Button) findViewById(R.id.logout_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.getInstance().signOut();
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(DashboardActivity.this,
                        new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Sign In failed, update UI appropriately
                        Log.w(APP, "Revoked Access");
                    }
                });
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
            }
        });

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null) {
            Toast.makeText(this, signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
        }

        Button takeSnapBtn = (Button) findViewById(R.id.take_snap_btn);
        takeSnapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ResultsActivity.class);
                intent.putExtra("used_button", "camera");
                startActivity(intent);
            }
        });

        Button openGalleryBtn = (Button) findViewById(R.id.open_gallery_btn);
        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ResultsActivity.class);
                intent.putExtra("used_button", "gallery");
                startActivity(intent);
            }
        });
    }

    // Return the list of top 3 suggestions and add to the list with the for loop
//    private static ArrayList<String> convertResponseToTopResults(BatchAnnotateImagesResponse response) {
//        ArrayList<String> foodList = new ArrayList<>();
//        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
//        if (labels != null) {
//            for (int x = 0; x < 3; x++) {
//                foodList.add(labels.get(x).getDescription());
//            }
//        } else {
//            foodList.clear();
//        }
//        return foodList;
//    }


}