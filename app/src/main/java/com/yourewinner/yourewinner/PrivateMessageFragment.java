package com.yourewinner.yourewinner;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class PrivateMessageFragment extends Fragment {

    public final static String ARG_BOXID = "BOXID";

    private Forum mForum;
    private int mPage;
    private String mBoxID;
    private ListView mMessageList;
    private PrivateMessageAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForum = Forum.getInstance();
        mPage = 1;
        mBoxID = getArguments().getString(ARG_BOXID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_private_message, container, false);
        mMessageList = (ListView) view.findViewById(R.id.message_list);
        getBox();
        return view;
    }

    private void getBox() {
        mForum.getBox(mBoxID, mPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final int messageCount = (int) r.get("total_message_count");
                final Object[] messages = (Object[]) r.get("list");

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messages (" + messageCount + ")");
                        mAdapter = new PrivateMessageAdapter(getActivity(), getActivity().getLayoutInflater(), messages);
                        mMessageList.setAdapter(mAdapter);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
            }
        });
    }
}
