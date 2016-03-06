package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PrivateMessageFragment extends Fragment implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {

    public final static String ARG_BOXID = "BOXID";

    private Forum mForum;
    private String mBoxID;
    private ListView mMessageList;
    private PrivateMessageAdapter mAdapter;
    private View mFooter;
    private ProgressDialog mDialog;

    private int lastCount;
    private int currentPage;
    private boolean isLoading;
    private boolean userScrolled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = Forum.getInstance();
        mBoxID = getArguments().getString(ARG_BOXID);
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
        mFooter = inflater.inflate(R.layout.loading, null);

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        getBox();
        return view;
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
        Map<String,Object> pm = (Map<String,Object>) mAdapter.getItem(position);

        if (pm != null) {
            String msgID = (String) pm.get("msg_id");
            //getMessage(msgID);
            Intent intent = new Intent(getActivity(), PrivateMessageActivity.class);
            intent.putExtra(PrivateMessageActivity.ARG_MSGID, msgID);
            intent.putExtra(PrivateMessageActivity.ARG_BOXID, mBoxID);
            startActivity(intent);
        }
    }
}
