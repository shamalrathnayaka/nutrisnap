package au.com.shamal.nutrisnap;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {

    static private TextView suggestedFoods;
    private ImageView takenSnap;
    static private RadioButton suggestionOne;
    static private RadioButton suggestionTwo;
    static private RadioButton suggestionThree;
    private RadioGroup radioButtonGroup;
    private TextView selectFoodTextView;


    private final static int IMAGE_REQUEST_CODE = 100;
    public static final int REQUEST_CAMERA_PERMISSION = 1982;
    public static final int REQUEST_CAMERA_IMAGE = 1983;
    public static final String SNAP = "snap.jpg";
    private static final int REQUEST_GALLERY_PERMISSIONS = 1980;
    private static final int REQUEST_GALLERY_IMAGE = 1981;
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;
    private static final String VISION_API_KEY = "AIzaSyCysq_cl-DSNT4G7NnqX0s1-Wo_VQxGib4";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final String APP = "NutriSnap";
    public static String identifiedFoodNames = "";
    static HistoryResultsData historyResultsData = new HistoryResultsData();
    static LottieAnimationView loadingFoodAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        suggestedFoods = findViewById(R.id.suggested_food_txt);
        takenSnap = findViewById(R.id.taken_snap_imageview);

        radioButtonGroup = findViewById(R.id.radio_button_group);
        suggestionOne = findViewById(R.id.suggestion_one_radio);
        suggestionTwo = findViewById(R.id.suggestion_two_radio);
        suggestionThree = findViewById(R.id.suggestion_three_radio);
        selectFoodTextView = findViewById(R.id.textView2);

        loadingFoodAnimation = (LottieAnimationView)findViewById(R.id.loading_food_animation);
        loadingFoodAnimation.playAnimation();
        loadingFoodAnimation.setMinAndMaxProgress(0f, 0.97f);
        loadingFoodAnimation.setVisibility(View.GONE);
//        String[] foods = identifiedFoodNames.split("\\|");
//        suggestionOne.setText(foods[0]);
//        suggestionTwo.setText(foods[1]);
//        suggestionThree.setText(foods[2]);



        Button getNutritionInfo = findViewById(R.id.nutrition_information_btn);
        getNutritionInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultsActivity.this, NutritionInfoActivity.class);
                String selectedFood = ((RadioButton) findViewById(radioButtonGroup.getCheckedRadioButtonId())).getText().toString();

//                String[] foods = identifiedFoodNames.split("\\|");
                intent.putExtra("food_name", selectedFood);
                startActivity(intent);
            }
        });
//        String results = getIntent().getStringExtra("result");
//        Bitmap imageBitmap = getIntent().getParcelableExtra("image_bitmap");
//
//        suggestedFoods.setText(results);
//        takenSnap.setImageBitmap(imageBitmap);


        String usedButton = getIntent().getStringExtra("used_button");
        if (usedButton.equals("gallery")){
            openGallery();
            Log.d("Results: " , "usedButton: " + usedButton);
        } else if(usedButton.equals("camera")) {
            openCamera();
            Log.d("Results: " , "usedButton: " + usedButton);
        }


    }

    private void openCamera() {
        Log.d(APP,"Permission1");
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, IMAGE_REQUEST_CODE);
        if (SetupPermissions.requestPermission(
                ResultsActivity.this,
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Log.d(APP,"Permission");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CAMERA_IMAGE);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, SNAP);
    }

    public void openGallery() {
        if (SetupPermissions.requestPermission(this, REQUEST_GALLERY_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(
                    intent,
                    "Please select a photo"
            ), REQUEST_GALLERY_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK && data != null) {
            uploadSnap(data.getData());
        } else if (requestCode == REQUEST_CAMERA_IMAGE && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadSnap(photoUri);
        }
    }

    public void uploadSnap(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleDownBitmap(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                takenSnap.setImageBitmap(bitmap);
                takenSnap.getLayoutParams().height = 750;
            } catch (IOException e) {
                Log.d(APP, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(APP, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.app_name, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap scaleDownBitmap(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private void callCloudVision(Bitmap bitmap) {
        suggestedFoods.setText("Please wait...");
        loadingFoodAnimation.setVisibility(View.VISIBLE);
        radioButtonGroup.setVisibility(View.GONE);
        selectFoodTextView.setVisibility(View.GONE);


        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(APP, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuffer foodSuggestions = new StringBuffer();
        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

        if (labels != null) {
            for (int x = 0; x < 3; x++) {
                foodSuggestions.append(labels.get(x).getDescription() + "|");
            }
        }
        // historyResultsData.setIdentifiedFoods(foodSuggestions.toString());
        return foodSuggestions.toString();
    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<ResultsActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(ResultsActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(APP, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(APP, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(APP, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        @Override
        protected void onPostExecute(String result) {
            ResultsActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                // System.out.println(result);
                identifiedFoodNames = result;


                String[] foods = identifiedFoodNames.split("\\|");
                suggestionOne.setText(foods[0]);
                suggestionTwo.setText(foods[1]);
                suggestionThree.setText(foods[2]);
//                Intent intent = new Intent(activity, ResultsActivity.class);
//                intent.putExtra("result", result);
                if(result !="") {
                    suggestedFoods.setText("");
                }else{
                    suggestedFoods.setText("Couldn't identify the food");
                }
                loadingFoodAnimation.setVisibility(View.GONE);
                radioButtonGroup.setVisibility(View.VISIBLE);
                selectFoodTextView.setVisibility(View.VISIBLE);

            }
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(final Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManager.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(APP, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }
}