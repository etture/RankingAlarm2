package com.ydly.rankingalarm2.util

import android.content.Context
import android.view.animation.AnimationUtils
import android.view.animation.Animation
import android.support.v4.content.res.ResourcesCompat
import android.content.res.Resources.NotFoundException
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.support.annotation.*
import android.support.v4.content.res.TypedArrayUtils.getTextArray
import javax.inject.Inject


class ResourceProvider(private val context: Context) {

    @NonNull
    fun getText(@StringRes resId: Int): CharSequence {
        return context.getText(resId)
    }

    @NonNull
    fun getTextArray(@ArrayRes resId: Int): Array<CharSequence> {
        return context.getResources().getTextArray(resId)
    }

    @NonNull
    fun getQuantityText(@PluralsRes resId: Int, quantity: Int): CharSequence {
        return context.getResources().getQuantityText(resId, quantity)
    }

    @NonNull
    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    @NonNull
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    @NonNull
    fun getStringArray(@ArrayRes resId: Int): Array<String> {
        return context.getResources().getStringArray(resId)
    }

    @NonNull
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String {
        return context.getResources().getQuantityString(resId, quantity)
    }

    @NonNull
    fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return context.getResources().getQuantityString(resId, quantity, *formatArgs)
    }

    fun getInteger(@IntegerRes resId: Int): Int {
        return context.getResources().getInteger(resId)
    }

    @NonNull
    fun getIntArray(@ArrayRes resId: Int): IntArray {
        return context.getResources().getIntArray(resId)
    }

    fun getBoolean(@BoolRes resId: Int): Boolean {
        return context.getResources().getBoolean(resId)
    }

    fun getDimension(@DimenRes resId: Int): Float {
        return context.getResources().getDimension(resId)
    }

    fun getDimensionPixelSize(@DimenRes resId: Int): Int {
        return context.getResources().getDimensionPixelSize(resId)
    }

    fun getDimensionPixelOffset(@DimenRes resId: Int): Int {
        return context.getResources().getDimensionPixelOffset(resId)
    }

    fun getDrawable(@DrawableRes resId: Int): Drawable? {
        return ContextCompat.getDrawable(context, resId)
    }

    @ColorInt
    fun getColor(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun getColorStateList(@ColorRes resId: Int): ColorStateList? {
        return ContextCompat.getColorStateList(context, resId)
    }

    @Nullable
    @Throws(Resources.NotFoundException::class)
    fun getFont(@FontRes id: Int): Typeface? {
        return ResourcesCompat.getFont(context, id)
    }

    fun loadAnimation(@AnimRes id: Int): Animation {
        return AnimationUtils.loadAnimation(context, id)
    }
}
