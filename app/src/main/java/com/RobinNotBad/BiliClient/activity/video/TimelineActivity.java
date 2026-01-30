package com.RobinNotBad.BiliClient.activity.video;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.RobinNotBad.BiliClient.R;
import com.RobinNotBad.BiliClient.activity.base.InstanceActivity;
import com.RobinNotBad.BiliClient.adapter.TimelineAdapter;
import com.RobinNotBad.BiliClient.api.TimelineApi;
import com.RobinNotBad.BiliClient.model.Timeline;
import com.RobinNotBad.BiliClient.util.CenterThreadPool;
import com.RobinNotBad.BiliClient.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends InstanceActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private View emptyView;

    private List<Timeline.DayInfo> dayInfoList;
    private TimelineAdapter adapter;
    private String types = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_refresh);

        setPageName("时间线");

        emptyView = findViewById(R.id.emptyTip);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(true);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dayInfoList = new ArrayList<>();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            dayInfoList.clear();
            loadTimeline();
        });

        loadTimeline();
    }

    private void loadTimeline() {
        swipeRefreshLayout.setRefreshing(true);
        CenterThreadPool.run(() -> {
            try {
                List<Timeline.DayInfo> result = TimelineApi.getTimeline(types, 7, 7);
                runOnUiThread(() -> {
                    dayInfoList.addAll(result);
                    if (adapter == null) {
                        adapter = new TimelineAdapter(this, dayInfoList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    if (dayInfoList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    report(e);
                    MsgUtil.showMsgLong("加载失败");
                });
            }
        });
    }
}
