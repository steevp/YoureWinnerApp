package com.yourewinner.yourewinner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubForumsAdapter extends BaseExpandableListAdapter {

    private List<String> mCategories;
    private Map<String,Object[]> mChildren;
    private Object[] mDataSet;

    public SubForumsAdapter() {
        mCategories = new ArrayList<>();
        mChildren = new HashMap<>();
    }

    public void updateData(Object[] data) {
        mDataSet = data;
        mCategories.clear();
        mChildren.clear();
        for (Object item : mDataSet) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cat = (Map<String, Object>) item;
            String catName = new String((byte[]) cat.get("forum_name"), Charset.forName("UTF-8"));
            Object[] child = (Object[]) cat.get("child");
            mCategories.add(catName);
            mChildren.put(catName, child);
        }
        notifyDataSetChanged();
    }

    public Object[] getData() {
        return mDataSet;
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

        TextView textView;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_forums_category, parent, false);
            textView = (TextView) convertView.findViewById(R.id.sub_forums_category);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }

        textView.setText(categoryTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Map<String,Object> child = (Map<String,Object>) getChild(groupPosition, childPosition);
        final String boardText = new String((byte[]) child.get("forum_name"), Charset.forName("UTF-8"));

        TextView textView;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sub_forums_board, parent, false);
            textView = (TextView) convertView.findViewById(R.id.sub_forums_board);
            convertView.setTag(textView);
        } else {
            textView = (TextView) convertView.getTag();
        }

        textView.setText(boardText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
