package it.reyboz.bustorino.backend;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public abstract class utils {
    private static final double EarthRadius = 6371e3;
    public static Double measuredistanceBetween(double lat1,double long1,double lat2,double long2){
        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);

        final double deltaPhi = Math.toRadians(lat2-lat1);
        final double deltaTheta = Math.toRadians(long2-long1);

        final double a = Math.sin(deltaPhi/2)*Math.sin(deltaPhi/2)+
                Math.cos(phi1)*Math.cos(phi2)*Math.sin(deltaTheta/2)*Math.sin(deltaTheta/2);
        final double c = 2*Math.atan2(Math.sqrt(a),Math.sqrt(1-a));

        return Math.abs(EarthRadius*c);

    }
    /*
    public static int convertDipToPixels(Context con,float dips)
    {
        return (int) (dips * con.getResources().getDisplayMetrics().density + 0.5f);
    }
     */

    public static float convertDipToPixels(Context con, float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,con.getResources().getDisplayMetrics());
    }
    public static int calculateNumColumnsFromSize(View containerView, int pixelsize){
        int width = containerView.getWidth();
        float ncols = ((float)width)/pixelsize;
        return (int) Math.floor(ncols);
    }

    /**
     * Check if there is an internet connection
     * @param con context object to get the system service
     * @return true if we are
     */
    public static boolean isConnected(Context con) {
        ConnectivityManager connMgr = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    ///////////////////// INTENT HELPER ////////////////////////////////////////////////////////////

    /**
     * Try to extract the bus stop ID from a URi
     *
     * @param uri The URL
     * @return bus stop ID or null
     */
    public static String getBusStopIDFromUri(Uri uri) {
        String busStopID;

        // everithing catches fire when passing null to a switch.
        String host = uri.getHost();
        if (host == null) {
            Log.e("ActivityMain", "Not an URL: " + uri);
            return null;
        }

        switch (host) {
            case "m.gtt.to.it":
                // http://m.gtt.to.it/m/it/arrivi.jsp?n=1254
                busStopID = uri.getQueryParameter("n");
                if (busStopID == null) {
                    Log.e("ActivityMain", "Expected ?n from: " + uri);
                }
                break;
            case "www.gtt.to.it":
            case "gtt.to.it":
                // http://www.gtt.to.it/cms/percorari/arrivi?palina=1254
                busStopID = uri.getQueryParameter("palina");
                if (busStopID == null) {
                    Log.e("ActivityMain", "Expected ?palina from: " + uri);
                }
                break;
            default:
                Log.e("ActivityMain", "Unexpected intent URL: " + uri);
                busStopID = null;
        }
        return busStopID;
    }

    /**
     * Open an URL in the default browser.
     *
     * @param url URL
     */
    public static void openIceweasel(String url, Context context) {
        Intent browserIntent1 = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent1);
    }
}
