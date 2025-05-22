package com.ziv.therahome;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ziv.therahome.R;

public class LogoPopUpManager {
    public static void show(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.logo_pop_up);

        ImageView logoImage = dialog.findViewById(R.id.logoImageView);
        View waterMask = dialog.findViewById(R.id.waterMask);
        logoImage.setAlpha(0.1f);
        waterMask.setBackgroundColor(Color.parseColor("#AA2196F3"));

        ValueAnimator animator = ValueAnimator.ofInt(0, 300);
        animator.setDuration(3000);
        animator.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            ViewGroup.LayoutParams params = waterMask.getLayoutParams();
            params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, activity.getResources().getDisplayMetrics());
            waterMask.setLayoutParams(params);
        });

        animator.addListener(new Animator.AnimatorListener() {
            public void onAnimationStart(Animator a) {}
            public void onAnimationEnd(Animator a) { dialog.dismiss(); }
            public void onAnimationCancel(Animator a) { dialog.dismiss(); }
            public void onAnimationRepeat(Animator a) {}
        });

        animator.start();
        dialog.show();
    }
}
