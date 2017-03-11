package com.vaigunth.cardprinter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vaigunth on 30-Oct-16.
 */

public class PrinterRecyclerView extends RelativeLayout {

    static float PULL_DOWN_THRESHOLD;

    private RecyclerView mRecyclerView;
    private boolean mIsRefreshing;
    private boolean mShouldAnimateCard;
    private boolean mStopRefresh;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;
    private float mPreviousY;
    private float mDeltaY;
    private List mCardsList;
    private ProgressBar mProgressBarLeft;
    private ProgressBar mProgressBarRight;
    private int mProgressCount;
    private Runnable mProgressUpdater;
    private boolean mProgressIncreasing;
    private ImageView mPrinter;
    private ImageView mPrinterCard;
    private LinearLayout mPrinterKnob;
    private LinearLayout mPrinterLight;
    private boolean mYellowLight;
    private Runnable mTogglePrinterLight;
    private float mChangeInY;


    public PrinterRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public PrinterRecyclerView(Context context) {
        super(context);
        initialize(context);
    }


    private void initialize(Context context) {
        //inflate(context, R.layout.printer_recycler_view, this);
        View rootView = inflate(context, R.layout.printer_recycler_view, null);
        mPrinter = (ImageView) rootView.findViewById(R.id.printer_image_view);
        mPrinterCard = (ImageView) rootView.findViewById(R.id.printer_card_image_view);
        mPrinterKnob = (LinearLayout) rootView.findViewById(R.id.printer_knob);
        mPrinterLight = (LinearLayout) rootView.findViewById(R.id.printer_light);
        mYellowLight = true;
        mShouldAnimateCard = false;
        mProgressCount = 0;
        mStopRefresh = false;

        mProgressBarLeft = (ProgressBar) rootView.findViewById(R.id.progress_left);
        mProgressBarRight = (ProgressBar) rootView.findViewById(R.id.progress_right);
        mProgressBarLeft.setVisibility(GONE);
        mProgressBarRight.setVisibility(GONE);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mCardsList = new ArrayList<>();

        //Animate when a new item appears on the list
        RecyclerView.ItemAnimator itemAnimator = new SimpleItemAnimator() {
            @Override
            public boolean animateRemove(RecyclerView.ViewHolder holder) {
                return false;
            }

            @Override
            public boolean animateAdd(RecyclerView.ViewHolder holder) {
                return false;
            }

            @Override
            public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
                final View view = holder.itemView;
                fromX += ViewCompat.getTranslationX(holder.itemView);
                fromY += ViewCompat.getTranslationY(holder.itemView);
                endAnimation(holder);
                int deltaX = toX - fromX;
                final int deltaY = toY - fromY;
                if (deltaX == 0 && deltaY == 0) {
                    dispatchMoveFinished(holder);
                    return false;
                }
                if (deltaY != 0) {
                    if (!mStopRefresh && mIsRefreshing) {
                        ViewCompat.setTranslationY(view, -deltaY);
                        ObjectAnimator moveDownCardsAnim = ObjectAnimator.ofFloat(view, "translationY", -deltaY, dpToPx(0));
                        moveDownCardsAnim.setDuration(400);
                        moveDownCardsAnim.setStartDelay(780);
                        moveDownCardsAnim.setInterpolator(new AccelerateInterpolator());
                        moveDownCardsAnim.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                if (mStopRefresh) {
                                    view.clearAnimation();
                                }
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
                        });
                        moveDownCardsAnim.start();
                    }
                }
                return true;
            }

            @Override
            public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder,
                                         int fromX, int fromY, int toX, int toY) {
                return false;
            }

            @Override
            public void runPendingAnimations() {

            }

            @Override
            public void endAnimation(final RecyclerView.ViewHolder item) {
            }

            @Override
            public void endAnimations() {
            }

            @Override
            public boolean isRunning() {

                return false;
            }
        };
        mRecyclerView.setItemAnimator(itemAnimator);

        /*mRecyclerAdapter = new PrinterRecyclerAdapter(mCardsList, this);
        mRecyclerView.setAdapter(mRecyclerAdapter);*/

       /* mLinearLayoutManager = new LinearLayoutManager(getContext()) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return true;
            }
        };
        mRecyclerView.setLayoutManager(mLinearLayoutManager);*/

        mProgressCount = 0;

        //Reset progress bar by gradually decreasing the value
        final Handler handler = new Handler();
        mProgressUpdater = null;
        mProgressUpdater = new Runnable() {
            @Override
            public void run() {
                if (!mProgressIncreasing && !mIsRefreshing) {
                    if (mProgressCount >= 0) {
                        mProgressCount -= 4;
                        mProgressBarLeft.setProgress(mProgressCount);
                        mProgressBarRight.setProgress(mProgressCount);
                        if (mRecyclerView.getTranslationY() != 0) {
                            mRecyclerView.setTranslationY(dpToPx((mProgressCount / 2)));
                        }
                        handler.postDelayed(mProgressUpdater, 5);
                    } else {
                        mProgressCount = 0;
                        if (!mIsRefreshing) {
                            mProgressBarLeft.setVisibility(GONE);
                            mProgressBarRight.setVisibility(GONE);
                        }
                    }
                }
            }
        };

        mPreviousY = 0;
        mDeltaY = 0;
        mIsRefreshing = false;
        PULL_DOWN_THRESHOLD = dpToPx(200);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPreviousY = event.getRawY();
                        mDeltaY = 0;
                        //Initialize progress bar
                        if (mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() <= 0) {
                            mProgressBarLeft.setVisibility(View.VISIBLE);
                            mProgressBarRight.setVisibility(View.VISIBLE);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mChangeInY = event.getRawY() - mPreviousY;
                        //Cancel refresh when scrolled
                        if (mIsRefreshing && canScrollUp()) {
                            mStopRefresh = true;
                            mIsRefreshing = false;
                            if (mCardsList.size() > 0) {
                                mCardsList.remove(0);
                                mRecyclerAdapter.notifyDataSetChanged();
                            }
                            mPrinter.clearAnimation();
                            mPrinterKnob.clearAnimation();
                            mPrinterCard.clearAnimation();
                            mRecyclerView.clearAnimation();
                            mPrinter.animate()
                                    .setDuration(100)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterKnob.animate()
                                    .setDuration(100)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterLight.animate()
                                    .setDuration(100)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterCard.setVisibility(GONE);
                            mPrinterCard.setTranslationY(-dpToPx(40));

                            mProgressIncreasing = false;
                        }
                        //Animate printer and progress bar when pulled down
                        if (!mIsRefreshing && !canScrollUp() && mChangeInY > 0 && mLinearLayoutManager.findFirstVisibleItemPosition() <= 0) {
                            mDeltaY += (mChangeInY);
                            mProgressCount = (int) ((mDeltaY * 100) / PULL_DOWN_THRESHOLD);
                            mProgressCount = mProgressCount > 100 ? 100 : mProgressCount;
                            //Show progress increase
                            if (mProgressCount >= 0) {
                                mProgressBarLeft.setVisibility(View.VISIBLE);
                                mProgressBarRight.setVisibility(View.VISIBLE);
                                mProgressBarLeft.setProgress(mProgressCount);
                                mProgressBarRight.setProgress(mProgressCount);
                                mPrinter.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                                mPrinterKnob.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                                mPrinterLight.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                                mRecyclerView.setTranslationY(dpToPx((mProgressCount / 2) + 10));
                            }
                        } else if (mChangeInY <= 0 && !mIsRefreshing) {
                            //Show progress decrease
                            mDeltaY += (mChangeInY);
                            mProgressCount = (int) ((mDeltaY * 100) / PULL_DOWN_THRESHOLD);
                            mProgressCount = mProgressCount < 0 ? 0 : mProgressCount;
                            mProgressBarLeft.setVisibility(View.VISIBLE);
                            mProgressBarRight.setVisibility(View.VISIBLE);
                            mProgressBarLeft.setProgress(mProgressCount);
                            mProgressBarRight.setProgress(mProgressCount);
                            mPrinter.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                            mPrinterKnob.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                            mPrinterLight.setTranslationY(-dpToPx(100) + dpToPx(mProgressCount));
                            mRecyclerView.setTranslationY(dpToPx((mProgressCount / 2) + 10));
                        }

                        mPreviousY = event.getRawY();

                        //Check if condition for refresh satisfied
                        if (mLinearLayoutManager.findFirstCompletelyVisibleItemPosition() <= 0
                                && mDeltaY >= PULL_DOWN_THRESHOLD && !mIsRefreshing && mProgressCount >= 100
                                && !canScrollUp() && mRecyclerView.getTranslationY() != 0) {
                            mShouldAnimateCard = true;
                            mPreviousY = mDeltaY = 0;
                            mIsRefreshing = true;
                            mRecyclerView.scrollToPosition(0);
                            mCardsList.add(0, true);
                            mRecyclerAdapter.notifyItemInserted(0);
                            mRecyclerView.scrollToPosition(0);
                            mYellowLight = true;
                            animatePrinter();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Reset animation on partial pull down
                        mPreviousY = mDeltaY = 0;
                        if (!mIsRefreshing) {
                            resetProgress();
                            mPrinter.animate()
                                    .setDuration(200)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterKnob.animate()
                                    .setDuration(200)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterLight.animate()
                                    .setDuration(200)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinter.clearAnimation();
                            mPrinterKnob.clearAnimation();
                            mPrinterCard.clearAnimation();
                            mRecyclerView.clearAnimation();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        this.addView(rootView);
    }

    /*
    * Returns true if the recycler view can scroll up further from the current point
    * */
    private boolean canScrollUp() {
        return ViewCompat.canScrollVertically(mRecyclerView, -1);
    }


    /*
    * Printer animates when the refresh condition is satisfied
    * */
    private void animatePrinter() {

        mProgressIncreasing = true;

        if (mIsRefreshing) {
            mPrinter.animate()
                    .setDuration(100)
                    .setInterpolator(new LinearInterpolator())
                    .translationY(0)
                    .start();
            mPrinterKnob.animate()
                    .setDuration(100)
                    .setInterpolator(new LinearInterpolator())
                    .translationY(0)
                    .start();

            mPrinterLight.animate()
                    .setDuration(100)
                    .setInterpolator(new LinearInterpolator())
                    .translationY(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mTogglePrinterLight.run();
                        }
                    })
                    .start();

            //Animate the printer knob movement while printing
            TranslateAnimation knobAnimation = new TranslateAnimation(0.0f, dpToPx(30),
                    0.0f, 0.0f);
            knobAnimation.setDuration(150);
            knobAnimation.setRepeatCount(5);
            knobAnimation.setRepeatMode(2);
            knobAnimation.setInterpolator(new LinearInterpolator());
            mPrinterKnob.startAnimation(knobAnimation);
            mPrinterCard.setVisibility(View.VISIBLE);
            mPrinterCard.animate()
                    .setDuration(1200)
                    .setInterpolator(new LinearInterpolator())
                    .translationY(dpToPx(10))
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mPrinter.animate()
                                    .setDuration(300)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterKnob.animate()
                                    .setDuration(300)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterLight.animate()
                                    .setDuration(300)
                                    .setInterpolator(new LinearInterpolator())
                                    .translationY(-dpToPx(100))
                                    .start();
                            mPrinterCard.setVisibility(GONE);
                            mPrinterCard.setTranslationY(-dpToPx(40));
                            mPrinterCard.setVisibility(View.VISIBLE);
                            mPrinter.clearAnimation();
                            mPrinterKnob.clearAnimation();
                            mPrinterCard.clearAnimation();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();

            mTogglePrinterLight = null;
            final int[] count = {3};
            final Handler handler = new Handler();
            //Continuously toggle printer light when printing
            mTogglePrinterLight = new Runnable() {
                @Override
                public void run() {
                    if (mYellowLight) {
                        mPrinterLight.setBackgroundResource(R.drawable.printer_light_2);
                        mYellowLight = false;
                    } else {
                        mPrinterLight.setBackgroundResource(R.drawable.printer_light_1);
                        mYellowLight = true;
                    }
                    if (count[0] != 0) {
                        handler.postDelayed(mTogglePrinterLight, 200);
                        count[0]--;
                    }
                }
            };

            //Reset recycler view position
            mRecyclerView.animate()
                    .translationY(0)
                    .setDuration(330)
                    .setStartDelay(820)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            if (mStopRefresh) {
                                animation.pause();
                                mRecyclerView.clearAnimation();
                            }
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
                    })
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    /*
    * Resets progress bar
    * */
    public void resetProgress() {
        mProgressIncreasing = false;
        mProgressUpdater.run();
    }

    /*
    * Converts a dp value into corresponding value in pixels
    * */
    private float dpToPx(int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return px;
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerAdapter;
    }

    public void setAdapter(PrinterRecyclerAdapter printerRecyclerAdapter) {
        mRecyclerAdapter = printerRecyclerAdapter;
        mCardsList = printerRecyclerAdapter.getDataSet();
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    public void setPrinterCardImage(int imageId) {
        mPrinterCard.setImageResource(imageId);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mLinearLayoutManager;
    }

    public void setLayoutManager(LinearLayoutManager layoutManager) {
        mLinearLayoutManager = layoutManager;
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    public boolean shouldAnimateCard() {
        return mShouldAnimateCard;
    }

    public void setShouldAnimateCard(boolean shouldAnimateCard) {
        this.mShouldAnimateCard = shouldAnimateCard;
    }

    public boolean shouldStopRefresh() {
        return mStopRefresh;
    }

    public void setStopRefresh(boolean stopRefresh) {
        this.mStopRefresh = stopRefresh;
    }

    public void setIsRefreshing(boolean isRefreshing) {
        this.mIsRefreshing = isRefreshing;
    }
}
