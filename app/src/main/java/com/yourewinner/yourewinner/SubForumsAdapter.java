package com.yourewinner.yourewinner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubForumsAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mCategories;
    private HashMap<String,Object[]> mChildren;

    public SubForumsAdapter(Context context, LayoutInflater inflater, Object[] data) {
        mContext = context;
        mInflater = inflater;

        mCategories = new ArrayList<String>();
        mChildren = new HashMap<String,Object[]>();
        for (int i=0;i<data.length;i++) {
            Map<String,Object> cat = (Map<String,Object>) data[i];
            String catName = new String((byte[]) cat.get("forum_name"), StandardCharsets.UTF_8);
            Object[] child = (Object[]) cat.get("child");
            mCategories.add(catName);
            mChildren.put(catName, child);
        }
    }

    @Override
    public int getGroupCount() {
        return mCategories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildren.get(mCategories.get(groupPosition)).length;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mCategories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildren.get(mCategories.get(groupPosition))[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryTitle = (String) getGroup(groupPosition);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sub_forums_category, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.sub_forums_category);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(categoryTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Map<String,Object> child = (Map<String,Object>) getChild(groupPosition, childPosition);
        final String boardText = new String((byte[]) child.get("forum_name"), StandardCharsets.UTF_8);

        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.sub_forums_board, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.sub_forums_board);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(boardText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class ViewHolder {
        public TextView textView;
    }
}
