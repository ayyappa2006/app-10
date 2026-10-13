package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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

public class OrgChatListActivity extends AppCompatActivity {
    private ListView lvChats;
    private ImageView ivBack;
    private ChatListAdapter adapter;
    private List<JSONObject> chatsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_chat_list);

        lvChats = findViewById(R.id.lvChats);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        adapter = new ChatListAdapter();
        lvChats.setAdapter(adapter);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        fetchChats();
    }

    private void fetchChats() {
        SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE);
        int orgId = prefs.getInt("org_id", 0);
        
        String endpoint = "get_chat_list.php?org_id=" + orgId;
        ApiClient.get(endpoint, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    if(json.getString("status").equals("success")) {
                        JSONArray data = json.getJSONArray("data");
                        chatsList.clear();
                        for(int i=0; i<data.length(); i++){
                            chatsList.add(data.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                Toast.makeText(OrgChatListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ChatListAdapter extends BaseAdapter {
        @Override public int getCount() { return chatsList.size(); }
        @Override public Object getItem(int position) { return chatsList.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(OrgChatListActivity.this).inflate(R.layout.item_organizer, parent, false);
            }
            TextView tvOrgName = convertView.findViewById(R.id.tvOrgName);
            TextView tvOrgCity = convertView.findViewById(R.id.tvOrgCity);
            android.widget.Button btnMessage = convertView.findViewById(R.id.btnMessage);
            
            btnMessage.setVisibility(View.GONE);

            try {
                JSONObject chat = chatsList.get(position);
                String userName = chat.getString("user_name");
                String lastMessage = chat.getString("last_message");
                int unreadCount = chat.optInt("unread_count", 0);
                int userId = chat.getInt("user_id");

                if (unreadCount > 0) {
                    tvOrgName.setText(userName + " (" + unreadCount + " unread)");
                } else {
                    tvOrgName.setText(userName);
                }
                tvOrgCity.setText(lastMessage);

                convertView.setOnClickListener(v -> {
                    Intent intent = new Intent(OrgChatListActivity.this, ChatActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.putExtra("chat_title", userName);
                    intent.putExtra("viewer_type", "org");
                    startActivity(intent);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }
}
