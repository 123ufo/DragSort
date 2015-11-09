package com.cifpay.draglistview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * 作者：xudiwei
 * <p/>
 * 日期： Administrator on 2015/8/31.
 * <p/>
 * 文本描述：列表拖动排序 ---长按拖动
 */
public class MyListView<T> extends ListView implements AdapterView.OnItemLongClickListener {

    /**
     * 条目的图片映射
     */
    private Bitmap dragBitmap;
    /**
     * 窗口管理器
     */
    private WindowManager windowManager;
    /**
     * 窗口管理器的参数
     */
    private WindowManager.LayoutParams params;
    /**
     * 要拖动图片
     */
    private ImageView dragImageView;
    /**
     * 拖动到上面时触发listview滚动的上边界
     */
    private int upBounce;
    /**
     * 拖动到下面时触发listview滚动时的下边界
     */
    private int downBounce;
    /**
     * 原选中条目
     */
    private int srcPosition;
    /**
     * 目标条目
     */
    private int descPosition;
    /**
     * 当前listview的数据
     */
    private List<T> listData;

    public MyListView(Context context) {
        super(context);
        initView();
    }

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        this.setOnItemLongClickListener(this);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        params = new WindowManager.LayoutParams();
        params.gravity = Gravity.TOP;
        //X坐标为0
        params.x = 0;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
        params.windowAnimations = 0;
        params.alpha = 0.8f;
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //计算上边界和下边界
        upBounce = getHeight() / 3;
        downBounce = getHeight() * 2 / 3;
        //记录下选中的条目,此时的目标条目也应该是选中的条目
        descPosition = srcPosition = position;
        //获取当前条目的bitmap
        view.setDrawingCacheEnabled(true);
        dragBitmap = view.getDrawingCache();
        //创建存放选中item条目的bitmap的ImageView
        dragImageView = new ImageView(getContext());
        dragImageView.setImageBitmap(dragBitmap);
        //添加到窗体
        windowManager.addView(dragImageView, params);
        dragImageView.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(null != dragBitmap){
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    doDrag((int) ev.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    stopDrag((int)ev.getY());
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 停上拖动
     * @param upY
     */
    private void stopDrag(int upY) {
        windowManager.removeViewImmediate(dragImageView);
        dragBitmap = null;
        if(null != listData){
            T t = listData.get(srcPosition);
            listData.remove(srcPosition);
            listData.add(descPosition, t);
            ListAdapter adapter = getAdapter();
            if(null != adapter &&  adapter instanceof BaseAdapter){
                ((BaseAdapter)adapter).notifyDataSetChanged();
            }else {
                throw new RuntimeException("Adapter is not BaseAdapter or BaseAdapter Child,Please Change");
            }
        }
    }

    /**
     * 执行拖动
     * @param moveY
     */
    private void doDrag(int moveY) {
        dragImageView.setVisibility(View.VISIBLE);
        params.y = moveY;
        windowManager.updateViewLayout(dragImageView, params);
        int currentMoveInPosition = this.pointToPosition(0, moveY);
        //如果当手指移动时时.所在的位置是有效的条目(也就是说不是在分割线中)此时的目标条目是就是当前的条目
        if(currentMoveInPosition != INVALID_POSITION){
            descPosition = currentMoveInPosition;
        }
        int scrollHeight = 0;
        if(moveY < upBounce){
            scrollHeight = 25;
        }else if(moveY > downBounce){
            scrollHeight = -25;
        }
        //获取当前条目的getTop 再加上偏移理然后移动
        int top = getChildAt(descPosition - getFirstVisiblePosition()).getTop();
        setSelectionFromTop(descPosition,top + scrollHeight);
    }

    /**
     * 把当前列表数据传进来
     * @param list
     */
    public void setData(List<T> list){
        this.listData = list;
    }

}
