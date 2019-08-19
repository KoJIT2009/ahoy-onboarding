package com.codemybrainsout.onboarder;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.codemybrainsout.onboarder.utils.ShadowTransformer;
import com.codemybrainsout.onboarder.views.CircleIndicatorView;
import com.codemybrainsout.onboarder.views.FlowingGradientClass;

import java.util.List;

public abstract class AhoyOnboarderFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private CircleIndicatorView circleIndicatorView;
    private ViewPager vpOnboarderPager;
    private AhoyOnboarderAdapter ahoyOnboarderAdapter;
    private TextView btnSkip;
    private ImageView ivNext, ivPrev;
    private FrameLayout navigationControls;
    private FrameLayout buttonsLayout;
    private RelativeLayout parentLayout;
    private ImageView backgroundImage;
    private View backgroundImageOverlay;

    private ShadowTransformer mCardShadowTransformer;
    private Typeface typeface;
    private List<Integer> colorList;
    private boolean solidBackground = false;
    private List<AhoyOnboarderCard> pages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_ahoy, container, false);
        
        setStatusBackgroundColor();
        //hideActionBar();
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parentLayout = (RelativeLayout) view.findViewById(R.id.parent_layout);
        circleIndicatorView = (CircleIndicatorView) view.findViewById(R.id.circle_indicator_view);
        btnSkip = (TextView) view.findViewById(R.id.btn_skip);
        buttonsLayout = (FrameLayout) view.findViewById(R.id.buttons_layout);
        navigationControls = (FrameLayout) view.findViewById(R.id.navigation_layout);
        ivNext = (ImageView) view.findViewById(R.id.ivNext);
        ivPrev = (ImageView) view.findViewById(R.id.ivPrev);
        backgroundImage = (ImageView) view.findViewById(R.id.background_image);
        backgroundImageOverlay = (View) view.findViewById(R.id.background_image_overlay);
        vpOnboarderPager = (ViewPager) view.findViewById(R.id.vp_pager);
        vpOnboarderPager.addOnPageChangeListener(this);
        btnSkip.setOnClickListener(this);
        ivPrev.setOnClickListener(this);
        ivNext.setOnClickListener(this);

        hideFinish(false);
        fadeOut(ivPrev, false);
    }

    public void setOnboardPages(List<AhoyOnboarderCard> pages) {

        this.pages = pages;
        ahoyOnboarderAdapter = new AhoyOnboarderAdapter(pages, this.getChildFragmentManager(), dpToPixels(0, this.getContext()), typeface);
        mCardShadowTransformer = new ShadowTransformer(vpOnboarderPager, ahoyOnboarderAdapter);
        mCardShadowTransformer.enableScaling(true);
        vpOnboarderPager.setAdapter(ahoyOnboarderAdapter);
        vpOnboarderPager.setPageTransformer(false, mCardShadowTransformer);
        circleIndicatorView.setPageIndicators(pages.size());

    }

    public float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    private void setStatusBackgroundColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getActivity().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            this.getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(this.getContext(), R.color.black_transparent));
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        boolean isInFirstPage = vpOnboarderPager.getCurrentItem() == 0;
        boolean isInLastPage = vpOnboarderPager.getCurrentItem() == ahoyOnboarderAdapter.getCount() - 1;

        if (i == R.id.btn_skip && isInLastPage) {
            onFinishButtonPressed();
        } else if (i == R.id.ivPrev && !isInFirstPage) {
            vpOnboarderPager.setCurrentItem(vpOnboarderPager.getCurrentItem() - 1);
        } else if (i == R.id.ivNext && !isInLastPage) {
            vpOnboarderPager.setCurrentItem(vpOnboarderPager.getCurrentItem() + 1);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        int firstPagePosition = 0;
        int lastPagePosition = ahoyOnboarderAdapter.getCount() - 1;
        circleIndicatorView.setCurrentPage(position);
        circleIndicatorView.setCurrentPage(position);

        if (position == lastPagePosition) {
            fadeOut(circleIndicatorView);
            showFinish();
            fadeOut(ivNext);
            fadeIn(ivPrev);
        } else if (position == firstPagePosition) {
            fadeOut(ivPrev);
            fadeIn(ivNext);
            hideFinish();
            fadeIn(circleIndicatorView);
        } else {
            fadeIn(circleIndicatorView);
            hideFinish();
            fadeIn(ivPrev);
            fadeIn(ivNext);
        }

        if (solidBackground && (pages.size() == colorList.size())) {
            backgroundImage.setBackgroundColor(ContextCompat.getColor(this.getContext(), colorList.get(position)));
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void fadeOut(View v) {
        fadeOut(v, true);
    }

    private void fadeOut(final View v, boolean delay) {

        long duration = 0;
        if (delay) {
            duration = 300;
        }

        if (v.getVisibility() != View.GONE) {
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
            fadeOut.setDuration(duration);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    v.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(fadeOut);
        }
    }

    private void fadeIn(final View v) {

        if (v.getVisibility() != View.VISIBLE) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
            fadeIn.setDuration(300);
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {

                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            v.startAnimation(fadeIn);
        }
    }

    private void showFinish() {
        btnSkip.setVisibility(View.VISIBLE);
        this.btnSkip.animate().translationY(0 - dpToPixels(5, this.getContext())).setInterpolator(new DecelerateInterpolator()).setDuration(500).start();
    }

    private void hideFinish(boolean delay) {

        long duration = 0;
        if (delay) {
            duration = 250;
        }

        this.btnSkip.animate().translationY(this.btnSkip.getBottom() + dpToPixels(100, this.getContext())).setInterpolator(new AccelerateInterpolator()).setDuration(duration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {

                btnSkip.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void hideFinish() {
        hideFinish(true);
    }

    /*
    private void hideActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }
    */

    abstract public void onFinishButtonPressed();

    public void showNavigationControls(boolean navigation) {
        if (navigation) {
            navigationControls.setVisibility(View.VISIBLE);
        } else {
            navigationControls.setVisibility(View.GONE);
        }
    }

    public void setImageBackground(int resId) {
        backgroundImageOverlay.setVisibility(View.VISIBLE);
        backgroundImage.setImageResource(resId);
    }

    public void setColorBackground(@ColorRes int color) {
        backgroundImage.setBackgroundColor(ContextCompat.getColor(this.getContext(), color));
    }

    public void setColorBackground(List<Integer> color) {
        this.colorList = color;
        solidBackground = true;
        backgroundImage.setBackgroundColor(ContextCompat.getColor(this.getContext(), color.get(0)));
    }

    public void setGradientBackground() {

        FlowingGradientClass grad = new FlowingGradientClass();
        grad.setBackgroundResource(R.drawable.translate)
                .onRelativeLayout(parentLayout)
                .setTransitionDuration(4000)
                .start();
    }

    public void setGradientBackground(int drawable) {
        FlowingGradientClass grad = new FlowingGradientClass();
        grad.setBackgroundResource(drawable)
                .onRelativeLayout(parentLayout)
                .setTransitionDuration(4000)
                .start();
    }

    public void setInactiveIndicatorColor(int color) {
        this.circleIndicatorView.setInactiveIndicatorColor(color);
    }

    public void setActiveIndicatorColor(int color) {
        this.circleIndicatorView.setActiveIndicatorColor(color);
    }

    /**
     * <br/><br/>
     * <b>N.B. Builds before JELLY_BEAN will use the default style</b>
     * <br/><br/>
     * Set the xml drawable style of the skip/done button, <br/>
     * using for example: ContextCompat.getDrawable(this, R.drawable.rectangle_button);
     *
     * @param res A drawable xml file representing your desired button style
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setFinishButtonDrawableStyle(Drawable res) {
        btnSkip.setBackground(res);
    }

    public void setFinishButtonTitle(CharSequence title) {
        btnSkip.setText(title);
    }

    public void setFinishButtonTitle(@StringRes int titleResId) {
        btnSkip.setText(titleResId);
    }

    public void setFont(Typeface typeface) {
        this.btnSkip.setTypeface(typeface);
        this.typeface = typeface;
    }
}
