package com.civicbin.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ListView lvMessages;
    private EditText etMessage;
    private ImageView btnSend, ivBack;
    private TextView tvChatTitle;
    private MessageAdapter adapter;
    private List<JSONObject> messagesList = new ArrayList<>();
    
    private int userId;
    private int orgId;
    private String viewerType;
    
    private Handler pollHandler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        lvMessages = findViewById(R.id.lvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        ivBack = findViewById(R.id.ivBack);
        tvChatTitle = findViewById(R.id.tvChatTitle);

        ivBack.setOnClickListener(v -> finish());
        
        viewerType = getIntent().getStringExtra("viewer_type");
        tvChatTitle.setText(getIntent().getStringExtra("chat_title"));
        
        SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE);
        
        if("user".equals(viewerType)) {
            userId = prefs.getInt("user_id", 0);
            orgId = getIntent().getIntExtra("org_id", 0);
        } else {
            orgId = prefs.getInt("org_id", 0);
            userId = getIntent().getIntExtra("user_id", 0);
        }

        adapter = new MessageAdapter();
        lvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        pollRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                pollHandler.postDelayed(this, 3000);
            }
        };
        pollHandler.post(pollRunnable);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        pollHandler.removeCallbacks(pollRunnable);
    }

    private void loadMessages() {
        String endpoint = "get_messages.php?user_id=" + userId + "&org_id=" + orgId + "&viewer_type=" + viewerType + "&mark_read=true";
        ApiClient.get(endpoint, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    if(json.getString("status").equals("success")) {
                        JSONArray data = json.getJSONArray("data");
                        messagesList.clear();
                        for(int i=0; i<data.length(); i++){
                            messagesList.add(data.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {}
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if(text.isEmpty()) return;
        
        etMessage.setText("");
        
        try {
            JSONObject req = new JSONObject();
            req.put("user_id", userId);
            req.put("org_id", orgId);
            req.put("sender_type", viewerType);
            req.put("message", text);
            
            ApiClient.post("send_message.php", req, new ApiClient.ApiCallback() {
                @Override public void onSuccess(JSONObject response) {
                    loadMessages();
                }
                @Override public void onError(String error) {
                    Toast.makeText(ChatActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private class MessageAdapter extends BaseAdapter {
        @Override public int getCount() { return messagesList.size(); }
        @Override public Object getItem(int position) { return messagesList.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.item_message, parent, false);
            }
            TextView tvMessage = convertView.findViewById(R.id.tvMessage);

            try {
                JSONObject msg = messagesList.get(position);
                String senderType = msg.getString("sender_type");
                String text = msg.getString("message");
                
                boolean isMe = senderType.equals(viewerType);
                
                tvMessage.setText(text);
                
                if(isMe) {
                    tvMessage.setBackgroundResource(R.drawable.bg_rounded_button);
                    tvMessage.setTextColor(0xFFFFFFFF);
                    ((android.widget.LinearLayout)tvMessage.getParent()).setGravity(android.view.Gravity.END);
                } else {
                    tvMessage.setBackgroundColor(0xFFE2E8F0);
                    tvMessage.setTextColor(0xFF0F172A);
                    ((android.widget.LinearLayout)tvMessage.getParent()).setGravity(android.view.Gravity.START);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return convertView;
        }
    }
}
