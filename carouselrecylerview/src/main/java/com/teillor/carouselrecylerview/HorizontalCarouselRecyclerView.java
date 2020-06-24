package com.teillor.carouselrecylerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalCarouselRecyclerView extends RecyclerView {

    View currentChildView = null;
    public int currentItemPosition = 0;
    private float scaleFactor = 0.3f;
    private double spreadFactor = 150d;
    private float minScaleOffset = 1f;
    private int childMarginFactor = 30;
    private boolean scrollFixedItemPosition = false;
    private boolean alignBottom = false;
    private boolean changeColor = false;

    public interface OnItemScrolledListener {
        void onScrolled(int index);
    }

    private OnItemScrolledListener onItemScrolledListener;

    public HorizontalCarouselRecyclerView(Context context) {
        super(context);
    }

    public HorizontalCarouselRecyclerView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public HorizontalCarouselRecyclerView(Context context, AttributeSet attributes, OnItemScrolledListener onItemScrolledListener) {
        super(context, attributes);
        this.onItemScrolledListener = onItemScrolledListener;
    }

    public void setOnItemScrolledListener(OnItemScrolledListener onItemScrolledListener){
        this.onItemScrolledListener = onItemScrolledListener;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setSpreadFactor(double spreadFactor) {
        this.spreadFactor = spreadFactor;
    }

    public void setChildMarginFactor(int childMarginFactor) {
        this.childMarginFactor = childMarginFactor;
    }

    public void setMinScaleOffset(float minScaleOffset) {
        this.minScaleOffset = minScaleOffset;
    }

    public void setScrollFixedItemPosition(boolean scrollFixedItemPosition) {
        this.scrollFixedItemPosition = scrollFixedItemPosition;
    }

    public void leftScroll() {
        this.smoothScrollToPosition(currentItemPosition - 1);
    }

    public void leftScroll(int position) {
        this.smoothScrollToPosition(position);
    }

    public void rightScroll() {
        this.smoothScrollToPosition(currentItemPosition + 1);
    }

    public void rightScroll(int position) {
        this.smoothScrollToPosition(position);
    }

    public void initialize(RecyclerView.Adapter adapter) {
        setLayoutManager(new LinearLayoutManager(getContext(), HORIZONTAL, false));
        adapter.registerAdapterDataObserver(new AdapterDataObserver() {

            @Override
            public void onChanged() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        int sidePadding = (getWidth() / 2) - (getChildAt(0).getWidth() / 2);
                        setPadding(sidePadding, 0, sidePadding, 0);
                        scrollToPosition(0);
                        addOnScrollListener(new OnScrollListener() {
                            @Override
                            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                onScrollChanged();
                            }

                            @Override
                            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);
                                if(newState == SCROLL_STATE_IDLE) {
                                    int position = getChildAdapterPosition(currentChildView);
                                    currentItemPosition = position;
                                    if(onItemScrolledListener != null)  {
                                        onItemScrolledListener.onScrolled(position);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
        setAdapter(adapter);
    }

    private void colorChange(View child, float scaleValue) {
        float alpha = scaleValue;
        if(alpha <= 1.1) alpha = 0;
        else alpha = 1;
        child.setAlpha(alpha);
    }

    private void onScrollChanged() {
        final float[] dumpScale = {0f};
        post(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < getChildCount(); i++) {
                    View child = getChildAt(i);
                    int childCenterX = (child.getLeft() + child.getRight()) / 2;
                    float scaleValue = getGaussianScale(childCenterX, minScaleOffset, scaleFactor, spreadFactor);
                    if(dumpScale[0] < scaleValue) {
                        dumpScale[0] = scaleValue;
                        currentChildView = child;
                    }
                    child.setScaleX(scaleValue);
                    child.setScaleY(scaleValue);

                    RecyclerView.LayoutParams params = (LayoutParams) child.getLayoutParams();
                    int margin = (int)(childMarginFactor * (scaleValue / 2));
                    params.leftMargin = margin;
                    params.rightMargin = margin;
                    child.setLayoutParams(params);

                    if(alignBottom) {
                        child.setTranslationY((int)(margin / 3));
                    }

                    if(changeColor) {
                        colorChange(child, scaleValue);
                    }
                }
            }
        });
    }

    private float getGaussianScale(int x, float minScaleOffset, float scaleFactor, Double spreadFactor) {
        int recyclerCenterX = (getLeft() + getRight()) / 2;
        return (float) (Math.pow(Math.E, -Math.pow(x - recyclerCenterX, 2d) / (2 * Math.pow(spreadFactor, 2d))) * scaleFactor + minScaleOffset);
    }
}
