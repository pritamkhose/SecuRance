package com.securance;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.securance.adaptor.CustomListAdapter;
import com.securance.util.SharedPrefUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactListActivity extends AppCompatActivity {


    ArrayList<HashMap<String, Object>> aList = new ArrayList<>();
    View activityView;
    View promptsView;
    AlertDialog alertDialog;
    //Dialog alertDialog;
    ListView listView;
    CustomListAdapter adapter;
    Boolean isStartup = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        activityView = getWindow().getDecorView().getRootView();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPopup(0, false);
            }
        });


        getContactList();

        Intent intent = getIntent();
        String openPopup  = intent.getStringExtra("openPopup");
        if(isStartup && openPopup != null && openPopup.length() > 2) {
            isStartup = false;
            openPopup(0, false);
        }
    }

    private void getContactList() {

        aList = new ArrayList<>();
        SharedPrefUtils sh = new SharedPrefUtils(this);

        String s = sh.getSharedPrefString("EmergencyNo");
        if (s != null && s.length() > 0) {
            ((TextView) findViewById(R.id.details_item)).setText(s);
        }
        ((TextView) findViewById(R.id.title_item)).setText("Emergency No.");
        ((LinearLayout) findViewById(R.id.LinearLayout)).setBackgroundColor(getResources().getColor(R.color.green));

        ((CardView) findViewById(R.id.card_view)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openCallPopup();
            }
        });

        aList = sh.getSharedPrefArrayList("ContactList");
        ListViewRefresh();
    }


    private void ListViewRefresh() {
        if (aList.size() == 0) {
            Snackbar.make(activityView, "No Contact Found", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        // Getting a reference to listview of main.xml layout file
        listView = (ListView) findViewById(R.id.ListView);
        adapter = new CustomListAdapter(this, aList);
        // adapter.notifyDataSetChanged();
        // Setting the adapter to the listView
        listView.setAdapter(adapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                openPopup(position, true);
            }
        });

    }

    private void openCallPopup() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        promptsView = inflater.inflate(R.layout.contact_detail, null);
        dialogBuilder.setView(promptsView);

        alertDialog = dialogBuilder.create();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        ((EditText) promptsView.findViewById(R.id.et_fname)).setText("Emergency No.");
        ((EditText) promptsView.findViewById(R.id.et_fname)).setEnabled(false);

        SharedPrefUtils sh = new SharedPrefUtils(this);
        String s = sh.getSharedPrefString("EmergencyNo");
        if (s != null && s.length() > 0) {
            ((TextView) findViewById(R.id.details_item)).setText(s);
        }
        ((EditText) promptsView.findViewById(R.id.et_phone)).setText(s);
        ((Button) promptsView.findViewById(R.id.btn_delete)).setVisibility(View.GONE);


        ((Button) promptsView.findViewById(R.id.btn_submit)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String sPh = ((EditText) promptsView.findViewById(R.id.et_phone)).getText().toString();
                if (sPh != null && sPh.length() > 2) {
                    // close popup it
                    alertDialog.dismiss();
                    SharedPrefUtils sh = new SharedPrefUtils(ContactListActivity.this);
                    sh.saveSharedPrefString("EmergencyNo", sPh);
                    ((TextView) findViewById(R.id.details_item)).setText(sPh);

                } else {
                    ((EditText) promptsView.findViewById(R.id.et_phone)).requestFocus();
                    Toast.makeText(ContactListActivity.this, "Please enter a valid " + getResources().getString(R.string.et_phone), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // show it
        alertDialog.show();
    }

    private void openPopup(int position, boolean oldContact) {
//        LayoutInflater li = LayoutInflater.from(this);
//        promptsView = li.inflate(R.layout.contact_detail, null);

        //alertDialog = new Dialog(this, android.R.style.Theme_Holo_Light_NoActionBar);
        // alertDialog.setContentView(promptsView);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        promptsView = inflater.inflate(R.layout.contact_detail, null);
        dialogBuilder.setView(promptsView);

        alertDialog = dialogBuilder.create();

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {

            }
        });

        if (oldContact && aList != null) {
            ((TextView) promptsView.findViewById(R.id.tv_postion)).setText(String.valueOf(position));
            ((EditText) promptsView.findViewById(R.id.et_fname)).setText(aList.get(position).get(getResources().getString(R.string.et_fname)).toString());
            ((EditText) promptsView.findViewById(R.id.et_phone)).setText(aList.get(position).get(getResources().getString(R.string.et_phone)).toString());

        } else {
            ((TextView) promptsView.findViewById(R.id.tv_postion)).setText("-1");
            ((EditText) promptsView.findViewById(R.id.et_fname)).setText("");
            ((EditText) promptsView.findViewById(R.id.et_phone)).setText("");
            ((Button) promptsView.findViewById(R.id.btn_delete)).setVisibility(View.GONE);
        }

        ((Button) promptsView.findViewById(R.id.btn_submit)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveContact();
            }
        });

        ((Button) promptsView.findViewById(R.id.btn_delete)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteContact();
            }
        });

        // show it
        alertDialog.show();
    }

    private void deleteContact() {
        Integer position = Integer.parseInt(((TextView) promptsView.findViewById(R.id.tv_postion)).getText().toString());

        HashMap<String, Object> hm = aList.get(position);
        aList.remove(hm);

        // close popup it
        alertDialog.dismiss();
        SharedPrefUtils sh = new SharedPrefUtils(this);
        sh.saveSharedPrefArrayList("ContactList", aList);
        getContactList();

    }


    private void saveContact() {
        String s;

        EditText etFname = ((EditText) promptsView.findViewById(R.id.et_fname));
        EditText etPhone = ((EditText) promptsView.findViewById(R.id.et_phone));

        s = etFname.getText().toString();
        if (s != null && s.length() > 0) {
            HashMap<String, Object> hm = new HashMap<>();
            hm.put(getResources().getString(R.string.et_fname), s);

            s = etPhone.getText().toString();
            if (s != null && s.length() > 6) {
                hm.put(getResources().getString(R.string.et_phone), s);

                if (((Button) promptsView.findViewById(R.id.btn_delete)).getVisibility() == View.VISIBLE ) {
                    Integer position = Integer.parseInt(((TextView) promptsView.findViewById(R.id.tv_postion)).getText().toString());
                    aList.set(position, hm);
                } else {
                    aList.add(hm);
                }

                // close popup it
                alertDialog.dismiss();
                SharedPrefUtils sh = new SharedPrefUtils(this);
                sh.saveSharedPrefArrayList("ContactList", aList);
                getContactList();

            } else {
                etPhone.requestFocus();
                Toast.makeText(this, "Please enter a valid " + getResources().getString(R.string.et_phone), Toast.LENGTH_SHORT).show();
            }
        } else {
            etFname.requestFocus();
            Toast.makeText(this, "Enter " + getResources().getString(R.string.et_fname), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
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
            finishAffinity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
