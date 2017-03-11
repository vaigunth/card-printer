package com.vaigunth.cardprinter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.List;


public abstract class PrinterRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<Object> mDataList;
    private Animation mFlipAnimation;
    private PrinterRecyclerView mPrinterRecyclerView;
    private int mParentHeight;
    private int mChildHeight;
    private int mChildHeightScaleFactor;
    private int mParentWidth;
    private int mChildWidth;
    private int mChildWidthScaleFactor;
    private FrameLayout mParent;
    private View mCardFront;
    private View mCardBack;
    private Animator.AnimatorListener mAnimationListener;

    public PrinterRecyclerAdapter(List mDataList, PrinterRecyclerView recyclerView) {

        if (mDataList == null) {
            throw new IllegalArgumentException("Data cannot be null !");
        }

        this.mDataList = mDataList;
        this.mPrinterRecyclerView = recyclerView;
    }

    public List getDataSet() {
        return mDataList;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        /*View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;*/
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {
        initCardAnimation(viewHolder, i);
    }

    public void initCardAnimation(RecyclerView.ViewHolder viewHolder, int i) {
        if (i == 0 && mPrinterRecyclerView.shouldAnimateCard()) {
            //Animate on new card
            mPrinterRecyclerView.setShouldAnimateCard(false);
            mParent = (FrameLayout) viewHolder.itemView;
            mParent.clearAnimation();
            mCardFront = LayoutInflater.from(mParent.getContext()).inflate(R.layout.card_front, mParent, false);
            mCardBack = LayoutInflater.from(mParent.getContext()).inflate(R.layout.card_back, mParent, false);
            animateInsertion(viewHolder, i);
        } else {
            //Don't animate existing cards
            mParent = (FrameLayout) viewHolder.itemView;
            mParent.clearAnimation();
            mCardBack = LayoutInflater.from(mParent.getContext()).inflate(R.layout.card_back, mParent, false);
            mParent.addView(mCardBack);
        }
    }

    public void initCardAnimation(RecyclerView.ViewHolder viewHolder, int i, int cardFrontLayoutId, int cardBackLayoutId) {
        if (i == 0 && mPrinterRecyclerView.shouldAnimateCard()) {
            //Animate on new card
            mPrinterRecyclerView.setShouldAnimateCard(false);
            mParent = (FrameLayout) viewHolder.itemView;
            mParent.clearAnimation();
            mCardFront = LayoutInflater.from(mParent.getContext()).inflate(cardFrontLayoutId, mParent, false);
            mCardBack = LayoutInflater.from(mParent.getContext()).inflate(cardBackLayoutId, mParent, false);
            animateInsertion(viewHolder, i);
        } else {
            //Don't animate existing cards
            mParent = (FrameLayout) viewHolder.itemView;
            mParent.clearAnimation();
            mCardBack = LayoutInflater.from(mParent.getContext()).inflate(cardBackLayoutId, mParent, false);
            mParent.addView(mCardBack);
        }
    }

    /*
    * Flips the card out of the printer into the recycler view
    * */
    private void animateInsertion(final RecyclerView.ViewHolder viewHolder, final int i) {
        mParent.addView(mCardFront);
        mParent.addView(mCardBack);

        mCardFront.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mParentHeight = mParent.getMeasuredHeight();
                mChildHeight = mCardFront.getMeasuredHeight();
                mChildWidth = mCardFront.getMeasuredWidth();
                mChildHeightScaleFactor = mParentHeight / mChildHeight;
                mParentWidth = mParent.getMeasuredWidth();
                mChildWidthScaleFactor = mParentWidth / mChildWidth;
            }
        });

        mCardFront.setVisibility(View.INVISIBLE);
        mCardBack.setVisibility(View.INVISIBLE);

        mFlipAnimation = AnimationUtils.loadAnimation(viewHolder.itemView.getContext(), R.anim.scale_anim);

        mFlipAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCardBack.setVisibility(View.VISIBLE);
                        mCardFront.setVisibility(View.GONE);
                    }
                }, 150);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCardFront.clearAnimation();
                mCardBack.clearAnimation();
                mPrinterRecyclerView.setIsRefreshing(false);
                mPrinterRecyclerView.resetProgress();
                mCardFront.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mAnimationListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCardFront.clearAnimation();
                        mCardBack.clearAnimation();
                        mParent.clearAnimation();
                        viewHolder.itemView.clearAnimation();
                        viewHolder.itemView.setAnimation(mFlipAnimation);
                        viewHolder.itemView.startAnimation(mFlipAnimation);
                    }
                }, 5);

            }

            @Override
            public void onAnimationEnd(Animator animation) {


            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        };
        Runnable animate = new Runnable() {
            @Override
            public void run() {
                if (!mPrinterRecyclerView.shouldStopRefresh()) {
                    mCardBack.setVisibility(View.INVISIBLE);
                    mCardFront.setVisibility(View.VISIBLE);
                    ObjectAnimator frontScaleX = ObjectAnimator.ofFloat(mCardFront, "scaleX", mChildWidthScaleFactor);
                    ObjectAnimator frontScaleY = ObjectAnimator.ofFloat(mCardFront, "scaleY", mChildHeightScaleFactor);

                    ObjectAnimator backScaleX = ObjectAnimator.ofFloat(mCardBack, "scaleX", 0f, 1f);
                    ObjectAnimator backScaleY = ObjectAnimator.ofFloat(mCardBack, "scaleY", 0f, 1f);

                    AnimatorSet scaleAnim = new AnimatorSet();
                    scaleAnim.setDuration(300);
                    scaleAnim.addListener(mAnimationListener);
                    scaleAnim.playTogether(frontScaleY, frontScaleX, backScaleX, backScaleY);
                    scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                    scaleAnim.start();
                } else {
                    mPrinterRecyclerView.setStopRefresh(false);
                }

            }
        };
        Handler handler = new Handler();
        handler.postDelayed(animate, 1050);
    }


    public static abstract class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(final View itemView) {
            super(itemView);
        }
    }


}