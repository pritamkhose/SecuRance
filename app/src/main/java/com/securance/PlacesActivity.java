package com.securance;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


import com.securance.adaptor.PagerAdapter;
import com.securance.fragment.Hospital_Fragment;
import com.securance.fragment.Police_Fragment;
import com.securance.util.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PlacesActivity extends AppCompatActivity {

  ViewPager viewPager;
  SharedPrefUtils sh;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_places);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
    sh = new SharedPrefUtils(this);

    List<android.support.v4.app.Fragment> fragments = new Vector<Fragment>();

    // only service display
    fragments.add(android.support.v4.app.Fragment.instantiate(this, Police_Fragment.class.getName()));
    fragments.add(android.support.v4.app.Fragment.instantiate(this, Hospital_Fragment.class.getName()));

    PagerAdapter mPagerAdapter = new PagerAdapter(this.getSupportFragmentManager(), fragments);

    viewPager = (ViewPager) findViewById(R.id.viewpager);
    viewPager.setAdapter(mPagerAdapter);

    viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        try {
          ((Spinner) findViewById(R.id.spinner)).setSelection(position);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });


    List<String> datas = new ArrayList<>();
    datas.add("Police");
    datas.add("Hospital");

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinnertextcenter, datas);
    adapter.setDropDownViewResource(R.layout.spinnertextcenter);
    Spinner spinner = (Spinner) findViewById(R.id.spinner);
    spinner.setAdapter(adapter);

    final int[] iCurrentSelection1 = {((Spinner) findViewById(R.id.spinner)).getSelectedItemPosition()};
    ((Spinner) findViewById(R.id.spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
        if (iCurrentSelection1[0] != position) {
          //mTabHost.setCurrentTab(position);
          viewPager.setCurrentItem(position);
          ((Spinner) findViewById(R.id.spinner)).setSelection(position);
        }
        iCurrentSelection1[0] = position;
      }

      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
        return;
      }

    });

  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.

    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_refresh, menu);
    // (menu.findItem(R.id.action_refresh)).setVisible(false);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.action_refresh: {
        sh.saveSharedPrefString("hospitalJson", "");
        sh.saveSharedPrefString("policeJson", "");
        onBackPressed();
      }
      return true;
      case R.id.action_exit: {
        finishAffinity();
      }
      return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

}
