package com.microsoft.office365.connectmicrosoftgraph;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by baek on 12/2/2015.
 * ArrayAdapter for ListView which contains ContactInfo Object.
 * Using custom layout contact_item (Display Name, Name, Email)
 */
public class ContactArrayAdapter extends ArrayAdapter<ContactInfo> {
    public ContactArrayAdapter
            (Context context, int resource, List<ContactInfo> contactInfos) {
        super(context, resource, contactInfos);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.contact_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ContactInfo contactInfo = getItem(position);
        viewHolder.displayName.setText(contactInfo.getDisplayName());
        viewHolder.name.setText(contactInfo.getName());
        viewHolder.address.setText(contactInfo.getEmail());
        return convertView;
    }
    static class ViewHolder {
        ViewHolder(View view) {
            displayName = (TextView) view.findViewById(R.id.displayNameTextView);
            name = (TextView) view.findViewById(R.id.nameTextView);
            address = (TextView) view.findViewById(R.id.emailTextView);
        }
        public TextView displayName;
        public TextView name;
        public TextView address;
    }
}
