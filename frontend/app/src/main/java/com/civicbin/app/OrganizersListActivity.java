package com.civicbin.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

public class OrganizersListActivity extends AppCompatActivity {
    private ListView lvOrganizers;
    private EditText etCityFilter;
    private ImageView ivBack;
    private OrganizerAdapter adapter;
    private List<JSONObject> orgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizers_list);

        lvOrganizers = findViewById(R.id.lvOrganizers);
        etCityFilter = findViewById(R.id.etCityFilter);
        ivBack = findViewById(R.id.ivBack);

        ivBack.setOnClickListener(v -> finish());

        adapter = new OrganizerAdapter();
        lvOrganizers.setAdapter(adapter);

        etCityFilter.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                fetchOrganizers(s.toString().trim());
            }
        });

        fetchOrganizers("");
    }

    private void fetchOrganizers(String city) {
        String endpoint = "get_organizers.php";
        if (!city.isEmpty()) {
            endpoint += "?city=" + city;
        }

        ApiClient.get(endpoint, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    if(json.getString("status").equals("success")) {
                        JSONArray data = json.getJSONArray("data");
                        orgList.clear();
                        for(int i=0; i<data.length(); i++){
                            orgList.add(data.getJSONObject(i));
                        }
                        adapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                Toast.makeText(OrganizersListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class OrganizerAdapter extends BaseAdapter {
        @Override public int getCount() { return orgList.size(); }
        @Override public Object getItem(int position) { return orgList.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(OrganizersListActivity.this).inflate(R.layout.item_organizer, parent, false);
            }
            TextView tvOrgName = convertView.findViewById(R.id.tvOrgName);
            TextView tvOrgCity = convertView.findViewById(R.id.tvOrgCity);
            Button btnMessage = convertView.findViewById(R.id.btnMessage);

            try {
                JSONObject org = orgList.get(position);
                String name = org.getString("org_name");
                String city = org.optString("city", "Location not specified");
                int orgId = org.getInt("id");

                tvOrgName.setText(name);
                tvOrgCity.setText(city);

                btnMessage.setOnClickListener(v -> {
                    Intent intent = new Intent(OrganizersListActivity.this, ChatActivity.class);
                    intent.putExtra("org_id", orgId);
                    intent.putExtra("chat_title", name);
                    intent.putExtra("viewer_type", "user");
                    startActivity(intent);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }
}
