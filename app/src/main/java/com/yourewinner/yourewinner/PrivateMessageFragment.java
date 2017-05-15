package com.yourewinner.yourewinner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PrivateMessageFragment extends Fragment
        implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    public final static String ARG_BOXID = "BOXID";

    private final static String ARG_CURPAGE = "ARG_CURPAGE";
    private final static String ARG_LASTCOUNT = "ARG_LASTCOUNT";

    private Forum mForum;
    private String mBoxID;
    private ListView mMessageList;
    private PrivateMessageAdapter mAdapter;
    private View mFooter;
    private ProgressDialog mDialog;
    private DataFragment mDataFragment;

    private int lastCount;
    private int currentPage;
    private boolean isLoading;
    private boolean userScrolled;
    private boolean ignoreClicks;

    InboxRefreshListener mCallback;

    public interface InboxRefreshListener {
        public void onInboxRefresh();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        mMessageList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mMessageList.setItemChecked(position, true);
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (InboxRefreshListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement InboxRefreshListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = Forum.getInstance();
        mBoxID = getArguments().getString(ARG_BOXID);
        mDataFragment = (DataFragment) getFragmentManager().findFragmentByTag(mBoxID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDataFragment.setData(mAdapter.getData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_message, container, false);
        mMessageList = (ListView) view.findViewById(R.id.message_list);
        mMessageList.setOnScrollListener(this);
        mAdapter = new PrivateMessageAdapter(getActivity(), getActivity().getLayoutInflater(), mBoxID);
        mMessageList.setAdapter(mAdapter);
        mMessageList.setOnItemClickListener(this);
        mMessageList.setOnItemLongClickListener(this);

        mMessageList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int mSelected = 0;

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    mSelected++;
                } else {
                    mSelected--;
                }
                if (mSelected > 0) {
                    mode.setTitle(mSelected + " selected");
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.menu_pm_contextual, menu);
                ignoreClicks = true;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete:
                        deleteCheckedMessages();
                        mode.finish();
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mSelected = 0;
                mMessageList.clearChoices();
                ignoreClicks = false;
                //mMessageList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            }
        });

        mFooter = inflater.inflate(R.layout.loading, null);

        if (savedInstanceState != null) {
            lastCount = savedInstanceState.getInt(ARG_LASTCOUNT);
            currentPage = savedInstanceState.getInt(ARG_CURPAGE);
        } else {
            lastCount = 0;
            currentPage = 1;
        }

        userScrolled = false;
        isLoading = true;
        ignoreClicks = false;

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        loadData();
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_LASTCOUNT, lastCount);
        outState.putInt(ARG_CURPAGE, currentPage);
    }

    /**
     * Load data from data fragment or web
     */
    private void loadData() {
        if (mDataFragment == null) {
            // Create data fragment
            mDataFragment = new DataFragment();
            getFragmentManager().beginTransaction().add(mDataFragment, mBoxID).commit();
            getBox();
        } else {
            final Object[] messages = mDataFragment.getData();
            if (messages != null) {
                mAdapter.updateData(messages);
                mMessageList.removeFooterView(mFooter);
                isLoading = false;
            } else {
                // Data fragment empty? Try fetching data
                getBox();
            }
        }
    }

    private void getBox() {
        mForum.getBox(mBoxID, currentPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final int messageCount = (int) r.get("total_message_count");
                final Object[] messages = (Object[]) r.get("list");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messages (" + messageCount + ")");
                        mAdapter.updateData(messages);
                        mMessageList.removeFooterView(mFooter);
                        isLoading = false;
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageList.removeFooterView(mFooter);
                        isLoading = false;
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMessageList.removeFooterView(mFooter);
                        isLoading = false;
                    }
                });
            }
        });
    }

    private void deleteCheckedMessages() {
        SparseBooleanArray checked = mMessageList.getCheckedItemPositions();
        ArrayList<Object> messages = new ArrayList<Object>();

        for (int i=0, size=checked.size(); i<size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                messages.add(mAdapter.getItem(key));
            }
        }

        if (messages.size() > 0) {
            mDialog.show();
            new DeleteMessagesTask().execute(messages);
        }
    }

    private class DeleteMessagesTask extends AsyncTask<ArrayList<Object>, Void, Boolean> {

        private ArrayList<Object> mMessages;

        @Override
        protected Boolean doInBackground(ArrayList<Object>... params) {
            mMessages = params[0];

            if (mMessages.size() > 0) {
                Map<String, Object> msg = (Map<String, Object>) mMessages.get(0);
                String msgID = (String) msg.get("msg_id");
                try {
                    boolean result = mForum.deleteMessage(msgID, mBoxID);
                    return result;
                } catch (XMLRPCException e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (mMessages.size() > 0 && result) {
                mAdapter.removeItem(mMessages.get(0));
                mMessages.remove(0);
                // Do the next one
                new DeleteMessagesTask().execute(mMessages);
            } else if (result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), "Success!", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // Fail
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getActivity(), "An error occurred!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
            userScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int lastItem = firstVisibleItem + visibleItemCount - mMessageList.getFooterViewsCount();
        if (userScrolled && lastItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
            isLoading = true;
            lastCount = totalItemCount;
            addMoreItems();
        }
    }

    private void addMoreItems() {
        currentPage++;
        mMessageList.addFooterView(mFooter);
        getBox();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!ignoreClicks) {
            Map<String, Object> pm = (Map<String, Object>) mAdapter.getItem(position);

            if (pm != null) {
                String msgID = (String) pm.get("msg_id");
                //getMessage(msgID);
                Intent intent = new Intent(getActivity(), PrivateMessageActivity.class);
                intent.putExtra(PrivateMessageActivity.ARG_MSGID, msgID);
                intent.putExtra(PrivateMessageActivity.ARG_BOXID, mBoxID);
                startActivityForResult(intent, 0);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            // Refresh
            mCallback.onInboxRefresh();
        }
    }
}
