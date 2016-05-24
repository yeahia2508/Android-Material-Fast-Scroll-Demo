package com.y34h1a.project.androidfastscrolldemo.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.y34h1a.project.androidfastscrolldemo.Pojo.Contact;
import com.y34h1a.project.androidfastscrolldemo.R;
import com.y34h1a.project.androidfastscrolldemo.utils.CircularContactView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lb.library.SearchablePinnedHeaderListViewAdapter;
import lb.library.StringArrayAlphabetIndexer;

/**
 * Created by Yeahia Mahammad Arif on 25-May-16.
 */

public class PhoneContactsAdapter extends SearchablePinnedHeaderListViewAdapter<Contact> {

    private Context context;
    private ArrayList<Contact> mContacts;
    private final int[] PHOTO_TEXT_BACKGROUND_COLORS;
    private LayoutInflater mInflater;


    public PhoneContactsAdapter(final ArrayList<Contact> contacts, Context context,LayoutInflater inflater) {
        this.context = context;
        this.mInflater = inflater;
        setData(contacts);
        PHOTO_TEXT_BACKGROUND_COLORS = this.context.getResources().getIntArray(R.array.contacts_text_background_colors);
    }

    public void setData(final ArrayList<Contact> contacts) {
        this.mContacts = contacts;
        final String[] generatedContactNames = generateContactNames(contacts);
        setSectionIndexer(new StringArrayAlphabetIndexer(generatedContactNames, true));
    }

    private String[] generateContactNames(final List<Contact> contacts) {
        final ArrayList<String> contactNames = new ArrayList<String>();
        if (contacts != null)
            for (final Contact contactEntity : contacts)
                contactNames.add(contactEntity.displayName);
        return contactNames.toArray(new String[contactNames.size()]);
    }


    @Override
    public CharSequence getSectionTitle(int sectionIndex) {
        return ((StringArrayAlphabetIndexer.AlphaBetSection) getSections()[sectionIndex]).getName();
    }

    @Override
    public boolean doFilter(final Contact item, final CharSequence constraint) {
        if (TextUtils.isEmpty(constraint))
            return true;
        final String displayName = item.displayName;
        return !TextUtils.isEmpty(displayName) && displayName.toLowerCase(Locale.getDefault())
                .contains(constraint.toString().toLowerCase(Locale.getDefault()));
    }

    @Override
    public ArrayList<Contact> getOriginalList() {
        return mContacts;
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final ViewHolder holder;
        final View rootView;
        if (convertView == null) {
            holder = new ViewHolder();
            rootView = mInflater.inflate(R.layout.listview_item, parent, false);
            holder.cvContactImage = (CircularContactView) rootView
                    .findViewById(R.id.cvDisplayNameCaracter);
            holder.cvContactImage.getTextView().setTextColor(0xFFffffff);
            holder.userName = (TextView) rootView
                    .findViewById(R.id.tv_contact_name);
            holder.headerView = (TextView) rootView.findViewById(R.id.header_text);
            rootView.setTag(holder);
        } else {
            rootView = convertView;
            holder = (ViewHolder) rootView.getTag();
        }

        final Contact contact = getItem(position);
        final String displayName = contact.displayName;
        holder.userName.setText(displayName);

        final int backgroundColorToUse = PHOTO_TEXT_BACKGROUND_COLORS[position
                % PHOTO_TEXT_BACKGROUND_COLORS.length];
        if (TextUtils.isEmpty(displayName))
            holder.cvContactImage.setImageResource(R.drawable.ic_person_white_120dp,
                    backgroundColorToUse);
        else {
            final String characterToShow = TextUtils.isEmpty(displayName) ? "" : displayName.substring(0, 1).toUpperCase(Locale.getDefault());
            holder.cvContactImage.setTextAndBackgroundColor(characterToShow, backgroundColorToUse);
        }

        bindSectionHeader(holder.headerView, null, position);
        return rootView;
    }

    private static class ViewHolder {
        public CircularContactView cvContactImage;
        TextView userName, headerView;
    }
}
