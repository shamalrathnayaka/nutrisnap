package au.com.shamal.nutrisnap;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class NutritionClient {

    private static final String APP_ID = "b49a88e7";
    private static final String APP_KEY = "4b755ffb1e23f313f6d772a4d92a6d13";
    private static String BASE_URL = "https://api.edamam.com/api/nutrition-data?app_id=" + APP_ID
            + "&app_key=" + APP_KEY + "&ingr=";
    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + encode(relativeUrl);
    }

    public static String encode(String url)
    {
        try {
            String encodeURL = URLEncoder.encode( url, "UTF-8" );
            return encodeURL;
        } catch (UnsupportedEncodingException e) {
            return "Issue occurred while encoding" +e.getMessage();
        }
    }
}
