package domashna.com.domashna3;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {


        ArrayList<String> avatar = new ArrayList<String>();
        ArrayList<String> title = new ArrayList<String>();
        ListView list;

        JSONArray cities = null;
        JSONArray users = null;

        //URL to get JSON Array
        private SwipeRefreshLayout mSwipeRefreshLayout = null;

        private String url = "";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            list = (ListView) rootView.findViewById(R.id.listView);
            new JSONParse().execute();
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //Refreshing data on server
                    new JSONParse().execute();
                    avatar.clear();
                }
            });

            return rootView;
        }

        private void updateList() {
            final BaseAdapter adapter = new BaseAdapter2(getActivity(), avatar,title);
            adapter.notifyDataSetChanged();
            list.setAdapter(adapter);


            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        private class JSONParse extends AsyncTask<String, String, JSONObject> {


            @Override
            protected JSONObject doInBackground(String... args) {
                JSONparser jParser = new JSONparser();

                // Getting JSON from URL
                JSONObject json = jParser.getJSONFromUrl(url);
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject json) {
                try {
                    users = json.getJSONArray("users");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject sys = null;
                try {
                    sys = json.getJSONObject("topic_list");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                assert sys != null;


                try {
                    // Getting JSON Array from URL
                    cities = sys.getJSONArray("topics");
                    users = json.getJSONArray("users");

                    for (int i = 0; i < cities.length(); i++) {

                        JSONObject c = cities.getJSONObject(i);

                        String post_title = c.getString("title");
                        JSONArray weather = c.getJSONArray("posters");

                        JSONObject weather1 = weather.getJSONObject(0);

                        String home = weather1.getString("user_id");

                        title.add(post_title);

                        for (int j = 0; j < users.length(); j++) {

                            JSONObject b = users.getJSONObject(j);

                            // Storing  JSON item in a Variable
                            String name = b.getString("id");
                            String img_url = b.getString("avatar_template");

                            if (home.matches(name)) {
                                avatar.add("http://frm.hackafe.org" + img_url);
                            }

                        }

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                updateList();

            }

        }


        public class BaseAdapter2 extends BaseAdapter {

            private Activity activity;

            private ArrayList avatar;
            private ArrayList title;
            private LayoutInflater inflater = null;

            public BaseAdapter2(Activity a, ArrayList b, ArrayList c) {
                activity = a;
                this.avatar = b;
                this.title = c;

                inflater = (LayoutInflater) activity
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            }

            public int getCount() {
                return avatar.size();
            }

            public Object getItem(int position) {
                return position;
            }

            public long getItemId(int position) {
                return position;
            }

            public View getView(int position, View convertView, ViewGroup parent) {
                View rootRowView;
                if (convertView != null) {
                    rootRowView = convertView;
                } else {
                    rootRowView = inflater.inflate(R.layout.row_listitem, parent, false);
                }

                TextView title2 = (TextView) rootRowView.findViewById(R.id.post_title); // title
                String song = title.get(position).toString();
                title2.setText(song);

                TextView line = (TextView) rootRowView.findViewById(R.id.line); // title

                Random rand = new Random();
                int color = Color.argb(255, rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
                line.setBackgroundColor(color);

                String sentence = (String) avatar.get(position);
                sentence = sentence.replace("{size}", "80x80");

                ImageLoader imageLoader = ImageLoader.getInstance();
                DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                        .cacheOnDisc(true).resetViewBeforeLoading(true).build();


                ImageView imageView = (ImageView) rootRowView.findViewById(R.id.icon);

                imageLoader.displayImage(sentence, imageView, options);

                return rootRowView;
            }
        }




    }
}
