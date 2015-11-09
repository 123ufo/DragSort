package com.cifpay.draglistview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.util.List;

/**
 * 作者： XuDiWei
 * <p/>
 * 日期：2015/8/30  17:40.
 * <p/>
 * 文件描述: 列表排序 --- 点击
 */
public class DragListView<T> extends ListView {

    /**
     * 点击选中的条目索引
     */
    private int clickPosition;
    /**
     * 要拖动到的所在位置条目的索引
     */
    private int descPosition;
    /**
     * 当前选中的条目
     */
    private View clickItemView;
    /**
     * 当前点击相对当前条目的Y坐标
     */
    private int relativeYInCurrentItem;
    /**
     * 当前点击相对当前屏幕的Y坐标
     */
    private int relativeYInScreen;
    private int upBounce;
    private int downBounce;
    private ImageView dragImageView;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;

    private List<T> mList;


    public DragListView(Context context) {
        super(context);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int downX = (int) ev.getX();
                int downY = (int) ev.getY();
                //根据坐标获取当前是哪个条目
                clickPosition = pointToPosition(downX, downY);
                if (clickPosition == INVALID_POSITION) {
                    return super.onInterceptTouchEvent(ev);
                }
                //获取当前条目的view
                clickItemView = getChildAt(clickPosition - getFirstVisiblePosition());
                //获取相对当前条目中的Y坐标
                relativeYInCurrentItem = downY - clickItemView.getTop();
                //获取List相对屏幕的Y坐标
                relativeYInScreen = (int) (ev.getRawY() - downY);
                //上界限
                upBounce = getHeight() / 3;
                //下界限
                downBounce = getHeight() / 3 * 2;
                clickItemView.setDrawingCacheEnabled(true);
                //获取当前条目
                Bitmap currentItemBitmap = clickItemView.getDrawingCache();
                startDrag(currentItemBitmap, downY);
                return false;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 开始拖动
     *
     * @param currentItemBitmap
     * @param downY
     */
    private void startDrag(Bitmap currentItemBitmap, int downY) {

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP;
        //X坐标为0
        params.x = 0;
        //条目在屏幕上的Y坐标
        params.y = downY - relativeYInCurrentItem + relativeYInScreen;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
//        params.windowAnimations = com.android.internal.R.style.Animation_Toast;
        params.windowAnimations = 0;
        params.alpha = 0.8f;
//        params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        //创建一个ImageView然后添加到窗体中
        dragImageView = new ImageView(getContext());
        dragImageView.setImageBitmap(currentItemBitmap);
        windowManager.addView(dragImageView, params);
        System.out.println("按下时的Y坐标:" + downY);
        System.out.println("参数的Y:" + params.y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //如果当前的图片不为空.而且目录位置的索引是一个有效的索引
        if (null != dragImageView && descPosition != INVALID_POSITION) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    doDrag((int) ev.getY());
//                    System.out.println("move");
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag((int) ev.getY());

                    break;
            }
            return true;

        }
        return super.onTouchEvent(ev);
    }

    /**
     * 停止拖动时
     */
    private void stopDrag(int upY) {
        if (null != dragImageView) {
            windowManager.removeView(dragImageView);
            dragImageView = null;
        }

        //判断松开手机从标所对应的条目是否为有效
        int upPosition = pointToPosition(0, upY);
        if (upPosition != INVALID_POSITION) {
            descPosition = upPosition;
        }

        if (upY < getChildAt(1).getTop()) {
            descPosition = 0;
        } else if (upY > getChildAt(getChildCount() - 1).getTop()) {
            descPosition = getAdapter().getCount() - 1;
        }

        //执行删除操作把原始的删除,再添加到拖动的地方去
        if (descPosition >= 0 && descPosition < getAdapter().getCount()) {
            ListAdapter adapter = getAdapter();
            adapter.getItem(clickPosition);
            //todo
            System.out.println("原索引:"+clickPosition);
            System.out.println("目标索引:"+descPosition);
            System.out.println("数据:"+mList);
            if (null != mList && mList.size() > 0) {
                T t = mList.get(clickPosition);
                mList.remove(clickPosition);
                mList.add(descPosition, t);
                System.out.println(mList);
                BaseAdapter baseAdapter = (BaseAdapter) getAdapter();
                baseAdapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * 执行拖动
     *
     * @param moveY
     */
    private void doDrag(int moveY) {
        if (null != dragImageView) {
            params.y = moveY - relativeYInCurrentItem + relativeYInScreen;
//            params.y = moveY - relativeYInCurrentItem;
            windowManager.updateViewLayout(dragImageView, params);
        }
        int currentMovePosition = pointToPosition(0, moveY);
        if (currentMovePosition != INVALID_POSITION) {
            descPosition = currentMovePosition;
        }

        //控制listview滚动
        int scrllHeight = 0;
        if (moveY < upBounce) {
            //向上滚动
            scrllHeight = 10;
        } else if (moveY > downBounce) {
            scrllHeight = -10;
        }

        //改变目标条目的Y坐标.达到滚动效果
        int descItemY = getChildAt(descPosition - getFirstVisiblePosition()).getTop();
        setSelectionFromTop(descPosition, descItemY + scrllHeight);
    }

    /**
     * 把数据设置进来
     *
     * @param list
     */
    public void setData(List<T> list) {
        mList = list;
    }
}
