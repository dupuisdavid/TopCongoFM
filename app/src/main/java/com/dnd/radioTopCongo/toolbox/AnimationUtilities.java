package com.dnd.radioTopCongo.toolbox;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.view.View;
import android.view.ViewGroup;

public class AnimationUtilities {

	public AnimationUtilities() {
		
	}
	
	public static void fadeIn(final View view, final Runnable runnable) {
		AnimationUtilities.fadeIn(view, 350, runnable);
	}
	
	public static void fadeIn(final View view, int animationDuration, final Runnable runnable) {
		view.setVisibility(ViewGroup.VISIBLE);
		view.setAlpha(0f);
		view.animate().alpha(1f).setDuration(animationDuration).setListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
			@Override
			public void onAnimationEnd(Animator animation) {
				if (runnable != null) {
					runnable.run();
				}
			}
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
	}
	
	public static void fadeOut(final View view, final Runnable runnable) {
		AnimationUtilities.fadeOut(view, 350, runnable);
	}
	
	public static void fadeOut(final View view, int animationDuration, final Runnable runnable) {
		view.setAlpha(1f);
		view.animate().alpha(0f).setDuration(animationDuration).setListener(new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
			@Override
			public void onAnimationEnd(Animator animation) {
				view.setVisibility(ViewGroup.GONE);
				if (runnable != null) {
					runnable.run();
				}
			}
			@Override
			public void onAnimationCancel(Animator animation) {}
		});
	}

}
