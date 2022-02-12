package com.kermitye.autoscrollqua;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private LinearSmoothScroller mScroller;
    private Disposable mAutoTask;
    private AutoScrollRecyclerView mRvStep;
    private AutoScrollRecyclerView mRvFlowScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add("测试数据" + i);
        }

        NoticeRecyclerViewAdapter adapter = new NoticeRecyclerViewAdapter(data);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Toast.makeText(view.getContext(), "you click item  "+i, Toast.LENGTH_SHORT).show();
            }
        });
        adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {
                Toast.makeText(view.getContext(), "you click child item  "+i, Toast.LENGTH_SHORT).show();

            }
        });

        mRvFlowScroll = findViewById(R.id.am_rv1);//流式滚动
        mRvFlowScroll.setLayoutManager(new LinearLayoutManager(this));
        mRvFlowScroll.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRvFlowScroll.setAdapter(adapter);

        mRvStep = findViewById(R.id.am_rv);
        mRvStep.setLayoutManager(new LinearLayoutManager(this));
        mRvStep.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRvStep.setAdapter(adapter);


        //item滚动步骤1：自定义LinearSmoothScroller，重写方法，滚动item至顶部，控制滚动速度
        mScroller = new LinearSmoothScroller(this) {

            //将移动的置顶显示
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            //控制速度，这里注意当速度过慢的时候可能会形成流式的效果，因为这里是代表移动像素的速度，
            // 当定时器中每隔的2秒之内正好或者还未移动一个item的高度的时候会出现，前一个还没移动完成又继续移动下一个了，就形成了流滚动的效果了
            // 这个问题后续可通过重写另外一个方法来进行控制，暂时就先这样了
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 3f / displayMetrics.density;
            }
        };
    }


    @Override
    protected void onStart() {
        super.onStart();
        //item滚动步骤3：开始滚动
        startAuto();
        //流式滚动效果
        mRvFlowScroll.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //item滚动步骤4：结束滚动
        stopAuto();
        mRvFlowScroll.stop();
    }

    //item滚动步骤2：设置定时器自动滚动
    public void startAuto() {
        if (mAutoTask!= null && !mAutoTask.isDisposed()) {
            mAutoTask.dispose();
        }
        mAutoTask = Observable.interval(1, 2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                //滚动到指定item
                mScroller.setTargetPosition(aLong.intValue());
                mRvStep.getLayoutManager().startSmoothScroll(mScroller);
            }
        });
    }

    private void stopAuto() {
        if (mAutoTask!= null && !mAutoTask.isDisposed()) {
            mAutoTask.dispose();
            mAutoTask = null;
        }
    }
}
