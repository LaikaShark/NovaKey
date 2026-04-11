/*
 * NovaKey - An alternative touchscreen input method
 * Copyright (C) 2019  Viviano Cantu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 *
 * Any questions about the program or source may be directed to <strellastudios@gmail.com>
 */

package hyperobject.keyboard.novakey.core.view.themes;


import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import hyperobject.keyboard.novakey.core.R;
import hyperobject.keyboard.novakey.core.utils.Util;

/**
 * Named color triplet that lets the keyboard tint itself to match the
 * foreground app. Each entry stores an Android package name alongside
 * three brand colors (primary / accent / contrast) which
 * {@link BaseMasterTheme#setPackage} copies into the master theme when
 * the user switches apps.
 * <p>
 * The table is loaded from {@code app_colors.json} in the app's private
 * storage and refreshed from a URL hosted in resources. The JSON schema
 * is a flat array of objects with the keys {@code "App Name"},
 * {@code "App Package"}, {@code "Primary"}, {@code "Accent"}, and
 * {@code "Contrast"}.
 */
public class AppTheme {

    public final String name, pk;
    public final int color1, color2, color3;


    /**
     * Builds one app entry from a five-element parameter list in order
     * {@code [name, package, primary, accent, contrast]}. The three
     * color strings are parsed via {@link Util#webToColor}.
     */
    public AppTheme(String... params) {
        name = params[0];
        pk = params[1];
        color1 = Util.webToColor(params[2]);
        color2 = Util.webToColor(params[3]);
        color3 = Util.webToColor(params[4]);
    }


    private static ArrayList<AppTheme> themes;
    private static final String filename = "app_colors.json";


    /**
     * Finds the app entry for a given package name.
     *
     * @param pk Android package name to look up
     * @return the matching entry, or {@code null} if not in the table
     */
    public static AppTheme fromPk(String pk) {
        for (AppTheme t : themes) {
            if (t.pk.equals(pk))
                return t;
        }
        return null;
    }


    /**
     * Populates the static {@code themes} table. First reads the cached
     * JSON file out of private storage (if present) so colors are
     * available immediately, then kicks off an {@link AppColorTask} to
     * refresh the table from the URL stored in
     * {@code R.string.app_color_url}.
     *
     * @param context used to open the private cache file
     * @param res     used to read the refresh URL
     */
    public static void load(Context context, Resources res) {
        themes = new ArrayList<>();

        try {
            Log.d("App Colors", "Loading saved colors...");
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            loadFromJSON(sb.toString());
        } catch (FileNotFoundException e) {
            Log.e("FileNotFoundException", "app_colors.json does not exist");
        } catch (IOException e) {
            Log.e("IOException", "app_colors.json is empty");
        }

        //if file not found download from web
        AppColorTask at = new AppColorTask(context);
        Log.d("App Colors", "Fetching colors from Network...");
        at.execute(res.getString(R.string.app_color_url));
    }


    /**
     * Async task that downloads the app-color JSON from a URL and,
     * once the response comes back on the main thread, hands the body
     * to {@link #loadFromJSON} and kicks off a {@link SaveColorsTask}
     * to cache the payload on disk for next launch.
     */
    private static class AppColorTask extends AsyncTask<String, Integer, String> {

        private String data = null;
        private final Context context;


        /**
         * @param context used to open the private cache file for writing
         *                once the download completes
         */
        AppColorTask(Context context) {
            this.context = context;
        }


        /**
         * Background thread entry point. Delegates to
         * {@link #downloadUrl} and stashes the response for
         * {@link #onPostExecute}. Errors are logged but not rethrown so
         * a network outage never crashes the IME load path.
         */
        @Override
        protected String doInBackground(String... params) {
            try {
                data = downloadUrl(params[0]);
                //Log.d("App Colors", "Data received from network: " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }


        /**
         * Main thread callback. Parses the freshly downloaded JSON into
         * the static {@code themes} list and then starts a
         * {@link SaveColorsTask} to overwrite the on-disk cache so the
         * next cold start has the updated data.
         */
        @Override
        protected void onPostExecute(String str) {
            try {
                loadFromJSON(str);
                SaveColorsTask st = new SaveColorsTask(
                        context.openFileOutput(filename, Context.MODE_PRIVATE));
                st.execute(str);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Async task that writes the already-downloaded JSON string to a
     * caller-supplied {@link FileOutputStream} so the next launch can
     * read it back before any network call completes.
     */
    private static class SaveColorsTask extends AsyncTask<String, Integer, String> {

        private String data = null;
        private final FileOutputStream outputStream;


        /**
         * @param outputStream private-mode output stream to the cache
         *                     file; owned by the caller but closed by
         *                     this task when writing finishes
         */
        SaveColorsTask(FileOutputStream outputStream) {
            this.outputStream = outputStream;
        }


        /**
         * Writes the first parameter string through an
         * {@link OutputStreamWriter}, flushing and closing the stream
         * when done. All IO exceptions are swallowed — the cache is
         * best-effort.
         */
        @Override
        protected String doInBackground(String... params) {
            data = params[0];
            try {
                OutputStreamWriter osw = new OutputStreamWriter(outputStream);
                osw.write(data);
                osw.flush();
                osw.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return data;
        }
    }


    /**
     * Blocking helper that opens an {@link HttpURLConnection} to the
     * given URL and reads the full response body into a string.
     * Exceptions are caught and logged, and the method returns whatever
     * was read before the failure (possibly the empty string).
     *
     * @param strUrl URL of the JSON document to fetch
     * @return the downloaded body, or empty on failure
     */
    private static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();

        } catch (Exception e) {
            Log.e("Exception ", e.toString());
        } finally {
            if (iStream == null)
                Log.d("App Colors", "iStream is null");
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     * Parses a JSON array of app-color objects and installs it as the
     * new static {@code themes} table. Builds the replacement list in
     * a local variable first and only assigns to the field once parsing
     * succeeds so a malformed payload cannot wipe out an already-loaded
     * table. Logs and discards any parse failure.
     */
    private static void loadFromJSON(String str) {
        try {
            JSONArray arr = new JSONArray(str);
            ArrayList<AppTheme> temp = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject curr = arr.getJSONObject(i);
                temp.add(
                        new AppTheme(
                                curr.get("App Name").toString(),
                                curr.get("App Package").toString(),
                                curr.get("Primary").toString(),
                                curr.get("Accent").toString(),
                                curr.get("Contrast").toString()
                        ));
            }
            themes = temp;
        } catch (JSONException e) {
            Log.e("JSONException", e.toString());
        } catch (NullPointerException e) {
            Log.e("NullPointerException", e.toString());
        }
    }

}
