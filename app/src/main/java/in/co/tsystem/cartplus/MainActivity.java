package in.co.tsystem.cartplus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {

    private static final int[] IMAGES = { R.drawable.button, R.drawable.pause };
    private int mPosition = 0;
    private ImageSwitcher mImageSwitcher;

    private DataHelper dbHelper = null;
    private GridViewAdapter ga;
    myAsyncTask tsk;
    checkDbVer parent_tsk;
    int newCatDbVer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Calling onCreate","");

        setContentView(R.layout.activity_main);
        String image_url = null;


        dbHelper = new DataHelper(this);

        parent_tsk = new checkDbVer(this);
        tsk = new myAsyncTask(this);
        parent_tsk.execute();

        mImageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(MainActivity.this);
                return imageView;
            }
        });
        mImageSwitcher.setInAnimation(this, android.R.anim.slide_in_left);
        mImageSwitcher.setOutAnimation(this, android.R.anim.slide_out_right);

        onSwitch(null);

        mImageSwitcher.postDelayed(new Runnable() {
            int i = 0;
            public void run() {
                mImageSwitcher.setBackgroundResource(IMAGES[mPosition]);
                mPosition = (mPosition + 1) % IMAGES.length;
                mImageSwitcher.postDelayed(this, 3000);
            }
        }, 3000);
    }

    public void onSwitch(View view) {
        mImageSwitcher.setBackgroundResource(IMAGES[mPosition]);
        mPosition = (mPosition + 1) % IMAGES.length;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public class GridViewAdapter extends BaseAdapter {
        private Context context;

        public GridViewAdapter(Context context) {
            this.context = context;
        }

        private int[] icons = {
                android.R.drawable.btn_star_big_off,
                android.R.drawable.btn_star_big_on,
                android.R.drawable.alert_light_frame,
                android.R.drawable.alert_dark_frame,
                android.R.drawable.arrow_down_float,
                android.R.drawable.gallery_thumb,
                android.R.drawable.ic_dialog_map,
                android.R.drawable.ic_popup_disk_full,
                android.R.drawable.star_big_on,
                android.R.drawable.star_big_off,
                android.R.drawable.star_big_on
        };

        @Override
        public int getCount() {
            // need to traverse database and get length
            // may be populate an array
            return icons.length;
            //return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(600,400));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(10, 10, 10, 10);
            } else {
                imageView = (ImageView) convertView;
            }
            //imageView.setImageResource(icons[position]);
            imageView.setImageBitmap(dbHelper.getBitmap(position + 1));
            return imageView;
        }


    }

    public class RestService {

        public JSONObject doGet(String url) {
            JSONObject json = null;

            HttpClient httpclient = new DefaultHttpClient();
            // Prepare a request object
            HttpGet httpget = new HttpGet(url);
            // Accept JSON
            httpget.addHeader("accept", "application/json");
            // Execute the request
            HttpResponse response;

            try {
                response = httpclient.execute(httpget);
                // Get the response entity
                // Log.e("myApp", "Issue is here...!");
                HttpEntity entity = response.getEntity();
                // If response entity is not null
                if (entity != null) {
                    // get entity contents and convert it to string
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    // construct a JSON object with result
                    json=new JSONObject(result);
                    // Closing the input stream will trigger connection release
                    instream.close();
                }
            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // Return the json
            return json;
        }

        private String convertStreamToString(InputStream is) {

            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();

            String line;
            try {

                br = new BufferedReader(new InputStreamReader(is));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return sb.toString();
        }
    }

    private class myAsyncTask extends AsyncTask< Void, Void, Bitmap > {

        JSONObject jb;
        private Context mContext;
        public myAsyncTask (Context context){
            mContext = context;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            /*String url_new = null;
            JSONArray categories;
            JSONObject item;*/

            /*
            try {
                shop = jb.getJSONArray("shops");
                for (int i = 0; i < shop.length(); i++) {
                    item = shop.getJSONObject(i);
                    url_new = item.getString("shop_uri");
                    //Log.d("Type", shop.getString(i););
                    tv.append(url_new + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
            /*try {
                categories = jb.getJSONArray("categories");
                for (int i = 0; i < categories.length(); i++) {
                    item = categories.getJSONObject(i);
                    url_new = item.getString("image_link");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return url_new;*/

            GridView gridview = (GridView) findViewById(R.id.gridview1);
            ga = new GridViewAdapter(mContext);
            gridview.setAdapter(ga);

            gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent,
                                        View view, int position, long id) {
                    Toast.makeText(MainActivity.this, "" + position,
                            Toast.LENGTH_SHORT).show();
                }
            });

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {

            Bitmap bitmap=null;
            String url_new = null;
            JSONArray categories;
            JSONObject item;
            ArrayList<String> urls = new ArrayList<String>();

            //String url = "http://10.0.0.17/opencart/?route=feed/web_api/products&category=27&key=key1";
            // uri is actually file location on disk
            /*
            String url = "http://10.0.0.104/landing/categories.php";
            RestService re = new RestService();
            jb = re.doGet(url);
            try {
                categories = jb.getJSONArray("categories");
                for (int i = 0; i < categories.length(); i++) {
                    item = categories.getJSONObject(i);
                    url_new = item.getString("image_link");
                    //Log.d("Type", shop.getString(i););
                    //tv.setText(url_new);
                    //tv.append(url_new + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            //urls.add("http://10.0.0.112/landing/images/bitmap_01.png");
            //urls.add("http://10.0.0.112/landing/images/home.jpeg");
            //urls.add("http://10.0.0.112/landing/images/tiger.png");
            //urls.add("http://10.0.0.112/landing/images/d.png");

            urls.add("http://10.0.0.112/landing/images/groceries.png");

            for (String url_to_open : urls) {
                try {
                    // Download the image
                    URL url = new URL(url_to_open);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream is = connection.getInputStream();
                    // Decode image to get smaller image to save memory
                    final BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeStream(is, null, options);
                    is.close();
                    dbHelper.insertBitmap(bitmap);
                } catch (IOException e) {
                    return null;
                }
            }
            //return url_new;
            //dbHelper.DATABASE_VERSION = newCatDbVer;
            return bitmap;


            //return url_new;
        }
    }


    private class checkDbVer extends AsyncTask< Void, Void, Integer > {

        JSONObject jb;
        private Context mContext;
        BufferedReader br;

        public checkDbVer(Context context) {
            mContext = context;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Integer db_ver_stored = 0;

            try {
                BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                        openFileInput("CartDbVer")));
                String inputString;
                //StringBuffer stringBuffer = new StringBuffer();
                while ((inputString = inputReader.readLine()) != null) {
                    //stringBuffer.append(inputString + "\n");
                    db_ver_stored = Integer.parseInt(inputString
                    );
                }

            } catch (IOException e) {
                //e.printStackTrace();
                db_ver_stored = 0;
            }

            Log.d("DB_VER_AND", dbHelper.DATABASE_VERSION + "" );
            Log.d("DB_VER_STORED is "+ db_ver_stored , "");
            //if (result != dbHelper.getVersionnew()) {
            if (result != db_ver_stored) {
                // download catalog db
                newCatDbVer = result;

                //write version to file
                try {
                    FileOutputStream fos = openFileOutput("CartDbVer", Context.MODE_PRIVATE);
                    fos.write(result.toString().getBytes());
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dbHelper.onUpgrade(dbHelper.getDb(),db_ver_stored,result);
                tsk.execute();
            } else {
                // populate grid view from database

                GridView gv = (GridView)findViewById(R.id.gridview1);
                GridViewAdapter ga1 = new GridViewAdapter(mContext);
                gv.setAdapter(ga1);

                gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent,
                                            View view, int position, long id) {
                        Toast.makeText(MainActivity.this, "" + position,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... arg0) {

            String url_new = null, ver = null;
            int version = 0;

            url_new = "http://10.0.0.112/landing/category_db_ver_check.php";
            RestService re = new RestService();
            jb = re.doGet(url_new);
            try {
                ver = jb.getString("db_ver");
                version = Integer.parseInt(ver);
                Log.i("DB_VER", version + "");
             } catch (Exception e) {
                e.printStackTrace();
            }
            return version;
        }
    }
}
