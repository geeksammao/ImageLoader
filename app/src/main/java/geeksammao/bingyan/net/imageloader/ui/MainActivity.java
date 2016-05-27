package geeksammao.bingyan.net.imageloader.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import geeksammao.bingyan.net.imageloader.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.e("onSaved", "saved");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("onDestroy", "quit");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public static String[] getThreadNames() {
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        ThreadGroup parent = null;
        while ((parent = group.getParent()) != null) {
            group = parent;
        }
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads);
        java.util.HashSet set = new java.util.HashSet();
        for (int i = 0; i < threads.length; ++i) {
            if (threads[i] != null && threads[i].isAlive()) {
                try {
                    set.add(threads[i].getThreadGroup().getName() + ","
                            + threads[i].getName() + ","
                            + threads[i].getPriority());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        String[] result = (String[]) set.toArray(new String[0]);
        java.util.Arrays.sort(result);
        return result;
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
}
