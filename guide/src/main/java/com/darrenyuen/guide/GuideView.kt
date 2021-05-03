package com.darrenyuen.guide

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import kotlin.math.sqrt

/**
 * Create by yuan on 2021/3/24
 */
class GuideView(val mContext: Context) : RelativeLayout(mContext), ViewTreeObserver.OnGlobalLayoutListener{

    private var first = true

    //外部调用可以传的参数
    private lateinit var mTargetView: View
    private var mColor: Int = 0
    private lateinit var mDirection: Direction
    private lateinit var mShape: HighLightShape
    private var mOffsetX = 0
    private var mOffsetY = 0
    private var mRadius = 0
    private var mCustomGuideView: View? = null


    //绘制展示引导view相关
    private var mCirclePaint: Paint? = null
    private var mBGPaint: Paint? = null
    private var isMeasured = false //targetView是否已经测量
    private var center = IntArray(2) //targetView中心
    private var location = IntArray(2) //targetView左上角坐标
    private var porterDuffXfermode: PorterDuffXfermode?  = null //绘图层叠模式
    private var bitmap: Bitmap? = null  //绘制前景bitmap
    private var mCanvas: Canvas? = null
    private var bgColor: Int = 0 //背景颜色 argb

    //回调
    private var mOnClickListener: OnClickListener? = null

    fun setTargetView(target: View) {
        mTargetView = target
    }

    fun setBgColor(color: Int) {
        mColor = color
    }

    fun setDirection(direction: Direction) {
        mDirection = direction
    }

    fun setShape(shape: HighLightShape) {
        mShape = shape
    }

    fun setOffset(x: Int, y: Int) {
        mOffsetX = x
        mOffsetY = y
    }

    fun setRadius(radius: Int) {
        mRadius = radius
    }

    fun setCustomGuideView(view: View) {
        mCustomGuideView = view
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        mOnClickListener = onClickListener
    }

    internal fun setClickInfo() {
        setOnClickListener {
            mOnClickListener?.onClick(this)
        }
    }

    @SuppressLint("ResourceAsColor")
    fun show() {
        mTargetView.viewTreeObserver.addOnGlobalLayoutListener(this)
        this.setBackgroundColor(R.color.transparent)
        ((mContext as Activity).window.decorView as FrameLayout).addView(this)
        first = false
    }

    fun hide() {

        mTargetView.viewTreeObserver.removeOnGlobalLayoutListener(this)
        this.removeAllViews()
        ((mContext as Activity).window.decorView as FrameLayout).removeView(this)
//        mCustomGuideView?.let {
//            mTargetView.viewTreeObserver.removeOnGlobalLayoutListener(this)
//            this.removeAllViews()
//            ((mContext as Activity).window.decorView as FrameLayout).removeView(this)
//        }
    }

    //留给业务层去判断是否使用新手引导
//    private fun hasShown(): Boolean {
//        return false
//    }

    override fun onGlobalLayout() {
        if (isMeasured) return
        if (mTargetView.height > 0 && mTargetView.width > 0) isMeasured = true
        mTargetView.getLocationInWindow(location)
        center[0] = location[0] + mTargetView.width / 2
        center[1] = location[1] + mTargetView.height / 2
        if (mRadius == 0) mRadius = getTargetViewRadius()
        createGuideView()
    }

    private fun getTargetViewRadius(): Int {
        if (isMeasured) {
            val size = getTargetViewSize()
            val x = size[0]
            val y = size[1]
            return (sqrt(x.toDouble() * x + y * y) / 2).toInt()
        }
        return  -1
    }

    private fun getTargetViewSize(): IntArray {
        val location = IntArray(2)
        if (isMeasured) {
            location[0] = mTargetView.width
            location[1] = mTargetView.height
        }
        return location
    }

    private fun createGuideView() {
        val guideViewParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        guideViewParams.setMargins(0, center[1] + mRadius + 10, 0, 0)

        val width = this.width
        val height = this.height

        val left: Int = center[0] - mRadius
        val right: Int = center[0] + mRadius
        val top: Int = center[1] - mRadius
        val bottom: Int = center[1] + mRadius
        mCustomGuideView?.let {
            when (mDirection) {
                Direction.TOP -> {
                    this.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    guideViewParams.setMargins(mOffsetX, mOffsetY - height + top, -mOffsetX, height - top - mOffsetY)
                }
                Direction.LEFT -> {
                    this.gravity = Gravity.RIGHT
                    guideViewParams.setMargins(mOffsetX - width + left, top + mOffsetY, width - left - mOffsetX, -top - mOffsetY)
                }
                Direction.BOTTOM -> {
                    this.gravity = Gravity.CENTER_HORIZONTAL;
                    guideViewParams.setMargins(mOffsetX, bottom + mOffsetY, -mOffsetX, -bottom - mOffsetY)
                }
                Direction.RIGHT -> {
                    guideViewParams.setMargins(right + mOffsetX, top + mOffsetY, -right - mOffsetX, -top - mOffsetY)
                }
                Direction.LEFT_TOP -> {
                    this.gravity = Gravity.RIGHT
                    guideViewParams.setMargins(mOffsetX - width + left, mOffsetY - height + top, width - left - mOffsetX, height - top - mOffsetY)
                }
                Direction.LEFT_BOTTOM -> {
                    this.gravity = Gravity.RIGHT
                    guideViewParams.setMargins(mOffsetX - width + left, bottom + mOffsetY, width - left - mOffsetX, -bottom - mOffsetY)
                }
                Direction.RIGHT_TOP -> {
                    this.gravity = Gravity.BOTTOM
                    guideViewParams.setMargins(right + mOffsetX, mOffsetY - height + top, -right - mOffsetX, height - top - mOffsetY);
                }
                Direction.RIGHT_BOTTOM -> {
                    guideViewParams.setMargins(right + mOffsetX, bottom + mOffsetY, -right - mOffsetX, -top - mOffsetY)
                }
            }
            this.addView(mCustomGuideView, guideViewParams)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (!isMeasured) return
        drawBg(canvas)
    }

//    @RequiresApi(Build.VERSION_CODES.M)
    private fun drawBg(canvas: Canvas?) {
        bitmap = Bitmap.createBitmap(canvas?.width ?: 0, canvas?.height
                ?: 0, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(bitmap!!)
        val bgPaint = Paint()

        if (bgColor != 0) {
            bgPaint.color = bgColor
        } else bgPaint.color = resources.getColor(R.color.shadow)

        mCanvas?.drawRect(0f, 0f, mCanvas?.width?.toFloat()!!, mCanvas?.height?.toFloat()!!, bgPaint)

        if (mCirclePaint == null) mCirclePaint = Paint()
        porterDuffXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
        mCirclePaint?.xfermode = porterDuffXfermode
        mCirclePaint?.isAntiAlias = true
        mCirclePaint?.setColor(context.getColor(R.color.white))

        mShape?.let {
            val oval = RectF()
            when (mShape) {
                HighLightShape.CIRCLE -> mCanvas?.drawCircle(center[0].toFloat(), center[1].toFloat(), mRadius.toFloat(), mCirclePaint!!)
                HighLightShape.RECTANGLE -> mCanvas?.drawRect(RectF(center[0].toFloat() - mRadius / 2 - 10, center[1].toFloat() - mRadius / 2 - 10, center[0].toFloat() + mRadius / 2 + 10, center[1].toFloat() + mRadius / 2 + 10), mCirclePaint!!)
                else -> {}
            }
        }

        canvas?.drawBitmap(bitmap!!, 0f, 0f, bgPaint)
        bitmap?.recycle()
    }

    interface OnClickListener {
        fun onClick(guideView: GuideView)
    }

    class Builder private constructor(){

        companion object {

            private lateinit var guideView: GuideView

            private val mInstance = Builder()

            fun newInstance(context: Context): Builder {
                guideView = GuideView(context)
                return mInstance
            }
        }

        fun setTargetView(target: View): Builder {
            guideView.setTargetView(target)
            return mInstance
        }

        fun setBgColor(color: Int): Builder {
            guideView.setBgColor(color)
            return mInstance
        }

        fun setDirection(direction: Direction): Builder {
            guideView.setDirection(direction)
            return mInstance
        }

        fun setShape(shape: HighLightShape): Builder {
            guideView.setShape(shape)
            return mInstance
        }

        fun setOffset(x: Int, y: Int): Builder {
            guideView.setOffset(x, y)
            return mInstance
        }

        fun setRadius(radius: Int): Builder {
            guideView.setRadius(radius)
            return mInstance
        }

        fun setCustomGuideView(view: View): Builder {
            guideView.setCustomGuideView(view)
            return mInstance
        }

        fun setOnClickListener(onClickListener: OnClickListener): Builder {
            guideView.setOnClickListener(onClickListener)
            return mInstance
        }

        fun build(): GuideView {
            guideView.setClickInfo()
            return guideView
        }
    }

}