package com.y34h1a.project.androidfastscrolldemo.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.y34h1a.project.androidfastscrolldemo.Pojo.Contact;
import com.y34h1a.project.androidfastscrolldemo.R;
import com.y34h1a.project.androidfastscrolldemo.adapter.PhoneContactsAdapter;
import com.y34h1a.project.androidfastscrolldemo.utils.ContactsQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lb.library.PinnedHeaderListView;

/**
 * Created by Yeahia Mahammad Arif on 25-May-16.
 */

public class MainActivity extends AppCompatActivity {
    ArrayList<Contact> contacts = new ArrayList<>();
    public Context context;
    public LayoutInflater mInflater; //Inflator for listview item
    public PhoneContactsAdapter mAdapter; //Adpater for listview
    private PinnedHeaderListView mListView;
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124; // for android 6.0 permission handle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getting inflator from activity
        mInflater = LayoutInflater.from(MainActivity.this);

        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        //toolbar setup
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get Contacts from Contacts Uri and sort the display name

        if (Build.VERSION.SDK_INT >= 23) {
            getContactWithPermission(); // For android 6.0
        } else {
            getContacts(); // For android below 6.0
        }

        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                char lhsFirstLetter = TextUtils.isEmpty(lhs.displayName) ? ' ' : lhs.displayName.charAt(0);
                char rhsFirstLetter = TextUtils.isEmpty(rhs.displayName) ? ' ' : rhs.displayName.charAt(0);
                int firstLetterComparison = Character.toUpperCase(lhsFirstLetter) - Character.toUpperCase(rhsFirstLetter);
                if (firstLetterComparison == 0)
                    return lhs.displayName.compareTo(rhs.displayName);
                return firstLetterComparison;
            }
        });


        mListView = (PinnedHeaderListView) findViewById(android.R.id.list);
        mAdapter = new PhoneContactsAdapter(contacts,context, mInflater);

        int pinnedHeaderBackgroundColor = ContextCompat.getColor(getApplicationContext(),getResIdFromAttribute(this, android.R.attr.colorBackground));
        mAdapter.setPinnedHeaderBackgroundColor(pinnedHeaderBackgroundColor);
        mAdapter.setPinnedHeaderTextColor(ContextCompat.getColor(getApplicationContext(),R.color.pinned_header_text));
        mListView.setPinnedHeaderView(mInflater.inflate(R.layout.pinned_header_listview_side_header, mListView, false));
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mAdapter);
        mListView.setEnableHeaderTransparencyChanges(true);
    }

    //get background Color resource id (libarary can't get resource id directly)
    public static int getResIdFromAttribute(final Activity activity, final int attr) {
        if (attr == 0)
            return 0;
        final TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.resourceId;
    }


    // GETTING CONTACTS USING CONTACTS URI
    private void getContacts() {
        Uri uri = ContactsQuery.CONTENT_URI;
        final Cursor cursor = getContentResolver().query(uri, ContactsQuery.PROJECTION, ContactsQuery.SELECTION, null, ContactsQuery.SORT_ORDER);
        if (cursor == null)
            return;
        ArrayList<Contact> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            Contact contact = new Contact();
            contact.contactUri = ContactsContract.Contacts.getLookupUri(
                    cursor.getLong(ContactsQuery.ID),
                    cursor.getString(ContactsQuery.LOOKUP_KEY));
            contact.displayName = cursor.getString(ContactsQuery.DISPLAY_NAME);
            result.add(contact);
        }

         contacts = result;
    }


    /********************************************************************************************
     * FOR ANDROID 6.0 PERMISSION HANDLE
    * ********************************************************************************************/


    @TargetApi(Build.VERSION_CODES.M)
    private void getContactWithPermission() {
        List<String> permissionsNeeded = new ArrayList<>();
        final List<String> permissionsList = new ArrayList<>();

        if (!addPermission(permissionsList, Manifest.permission.READ_CONTACTS))
            permissionsNeeded.add("READ CONTACTS");
        if(permissionsList.size() > 0){
            if(permissionsNeeded.size()>0){
                // Need Rationale
                String message = "You need to grant access to " + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);
                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            getContacts();
        }

        getContacts();

    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
            {
                Map<String, Integer> perms = new HashMap<>();

                // Initial
                perms.put(Manifest.permission.READ_CONTACTS,PackageManager.PERMISSION_GRANTED);

                //Fill with results
                for(int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i],grantResults[i]);

                if(perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    getContacts();
                }else {
                    //Permission Denied
                    Toast.makeText(MainActivity.this,"Some Permission is Denied",Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
