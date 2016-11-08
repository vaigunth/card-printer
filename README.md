# PULL DOWN TO REFRESH - CARD PRINTER

**Android implementation of the following dribble shot:**

https://dribbble.com/shots/3031884-Pull-to-Refresh-Printer

Download .apk file at:

https://www.dropbox.com/s/bm7ja5cwt2dtsgx/refresh.apk?dl=0

![Card printer demo](demo.gif?raw=true "Card Printer Demo")

Checking the condition for refresh inside the ```onTouchListener```:

```java
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
```
Printer knob animation to print the card:

```java
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
                        	//Reset the animation
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
```

Scale animation:

```xml
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:fromXScale="0.8"
    android:fromYScale="-1.0"
    android:interpolator="@android:interpolator/accelerate_decelerate"
    android:pivotX="50%"
    android:pivotY="15%"
    android:toXScale="1"
    android:toYScale="1" />
```

Flipping the card during the scale animation:
```java
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
                ((MainActivity) mActivity).mIsRefreshing = false;
                ((MainActivity) mActivity).resetProgress();
                mCardFront.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
```

Thank you **Saptarshi Prakash** for the innovative animation!