package com.shrikanthravi.collapsiblecalendarview.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.*
import com.google.android.material.card.MaterialCardView
import com.shrikanthravi.collapsiblecalendarview.R
import com.shrikanthravi.collapsiblecalendarview.view.ExpandIconView
import com.shrikanthravi.collapsiblecalendarview.data.Day
import kotlin.jvm.JvmOverloads
import com.shrikanthravi.collapsiblecalendarview.view.LockScrollView

abstract class UICalendar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    protected var mContext: Context? = null
    protected lateinit var mInflater: LayoutInflater

    // UI
    protected var mLayoutRoot: LinearLayout? = null
    protected var mTxtTitle: TextView? = null
    protected var mTableHead: TableLayout? = null
    protected var mScrollViewBody: LockScrollView? = null
    protected var mTableBody: TableLayout? = null
    protected var mLayoutBtnGroupMonth: RelativeLayout? = null
    protected var mLayoutBtnGroupWeek: RelativeLayout? = null
    protected var mBtnPrevMonth: ImageView? = null
    protected var mBtnNextMonth: ImageView? = null
    protected var mBtnPrevWeek: ImageView? = null
    protected var mBtnNextWeek: ImageView? = null
    protected var expandIconView: ExpandIconView? = null
    protected var containerTableBody: MaterialCardView? = null

    // Attributes
    private var mShowWeek = true
    private var mFirstDayOfWeek = MONDAY
    private var mState = STATE_COLLAPSED
    private var mTextColor = Color.BLACK
    private var mPrimaryColor = Color.WHITE
    private var mTodayItemTextColor = Color.BLACK
    private var mTodayItemIconColor = Color.WHITE
    private var mItemIconColor = Color.parseColor("#333333")
    private var mTodayItemBackgroundDrawable =
        resources.getDrawable(R.drawable.circle_black_stroke_background)
    private var mSelectedItemTextColor = Color.WHITE
    private var mSelectedItemBackgroundDrawable =
        resources.getDrawable(R.drawable.circle_black_solid_background)
    private var mButtonLeftDrawable = resources.getDrawable(R.drawable.left_icon)
    private var mButtonRightDrawable = resources.getDrawable(R.drawable.right_icon)
    var selectedItem: Day? = null
    private var mButtonLeftDrawableTintColor = Color.BLACK
    private var mButtonRightDrawableTintColor = Color.BLACK
    private var mExpandIconColor = Color.BLACK
    private var mEventColor = Color.BLACK
    private var mEventDotSize = EVENT_DOT_BIG
    private var mColorContainerTableBody = Color.WHITE
    protected abstract fun redraw()
    protected abstract fun reload()
    protected open fun init(context: Context) {
        mContext = context
        mInflater = LayoutInflater.from(mContext)

        // load rootView from xml
        val rootView = mInflater.inflate(R.layout.widget_collapsible_calendarview, this, true)

        // init UI
        mLayoutRoot = rootView.findViewById(R.id.layout_root)
        mTxtTitle = rootView.findViewById(R.id.txt_title)
        mTableHead = rootView.findViewById(R.id.table_head)
        mScrollViewBody = rootView.findViewById(R.id.scroll_view_body)
        mTableBody = rootView.findViewById(R.id.table_body)
        mLayoutBtnGroupMonth = rootView.findViewById(R.id.layout_btn_group_month)
        mLayoutBtnGroupWeek = rootView.findViewById(R.id.layout_btn_group_week)
        mBtnPrevMonth = rootView.findViewById(R.id.btn_prev_month)
        mBtnNextMonth = rootView.findViewById(R.id.btn_next_month)
        mBtnPrevWeek = rootView.findViewById(R.id.btn_prev_week)
        mBtnNextWeek = rootView.findViewById(R.id.btn_next_week)
        expandIconView = rootView.findViewById(R.id.expandIcon)
        containerTableBody = rootView.findViewById(R.id.container_table_body)
    }

    protected fun setAttributes(attrs: TypedArray) {
        // set attributes by the values from XML
        isShowWeek = attrs.getBoolean(R.styleable.UICalendar_showWeek, mShowWeek)
        firstDayOfWeek = attrs.getInt(R.styleable.UICalendar_firstDayOfWeek, mFirstDayOfWeek)
        state = attrs.getInt(R.styleable.UICalendar_state, mState)
        textColor = attrs.getColor(R.styleable.UICalendar_textColor, mTextColor)
        primaryColor = attrs.getColor(R.styleable.UICalendar_primaryColor, mPrimaryColor)
        eventColor = attrs.getColor(R.styleable.UICalendar_eventColor, mEventColor)
        eventDotSize = attrs.getInt(R.styleable.UICalendar_eventDotSize, mEventDotSize)
        todayItemTextColor = attrs.getColor(
            R.styleable.UICalendar_todayItem_textColor, mTodayItemTextColor
        )
        todayItemIconColor = attrs.getColor(R.styleable.UICalendar_todayColorIconEvent, mTodayItemIconColor)
        itemIconColor = attrs.getColor(R.styleable.UICalendar_colorIconEvent, mItemIconColor)
        mTodayItemBackgroundDrawable =
            attrs.getDrawable(R.styleable.UICalendar_todayItem_background) ?: mTodayItemBackgroundDrawable
        selectedItemTextColor = attrs.getColor(
            R.styleable.UICalendar_selectedItem_textColor, mSelectedItemTextColor
        )
        mSelectedItemBackgroundDrawable =
            attrs.getDrawable(R.styleable.UICalendar_selectedItem_background) ?: mSelectedItemBackgroundDrawable
        mButtonLeftDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonLeft_drawable) ?: mButtonLeftDrawable
        mButtonRightDrawable = attrs.getDrawable(R.styleable.UICalendar_buttonRight_drawable) ?: mButtonRightDrawable
        setButtonLeftDrawableTintColor(
            attrs.getColor(
                R.styleable.UICalendar_buttonLeft_drawableTintColor,
                mButtonLeftDrawableTintColor
            )
        )
        setButtonRightDrawableTintColor(
            attrs.getColor(
                R.styleable.UICalendar_buttonRight_drawableTintColor,
                mButtonRightDrawableTintColor
            )
        )
        mColorContainerTableBody = attrs.getColor(R.styleable.UICalendar_containerBackgroundColor, Color.WHITE)
        setExpandIconColor(attrs.getColor(R.styleable.UICalendar_expandIconColor, mExpandIconColor))
        val selectedItem: Day? = null
    }

    fun setButtonLeftDrawableTintColor(color: Int) {
        mButtonLeftDrawableTintColor = color
        mBtnPrevMonth!!.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        mBtnPrevWeek!!.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        redraw()
    }

    fun setButtonRightDrawableTintColor(color: Int) {
        mButtonRightDrawableTintColor = color
        mBtnNextMonth!!.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        mBtnNextWeek!!.drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        redraw()
    }

    fun setExpandIconColor(color: Int) {
        mExpandIconColor = color
        expandIconView!!.setColor(color)
    }

    var colorContainerTableBody: Int
    get() = mColorContainerTableBody
    set(colorContainerTableBody) {
        mColorContainerTableBody = colorContainerTableBody
        redraw()
    }

    var isShowWeek: Boolean
        get() = mShowWeek
        set(showWeek) {
            mShowWeek = showWeek
            if (showWeek) {
                mTableHead!!.visibility = VISIBLE
            } else {
                mTableHead!!.visibility = GONE
            }
        }
    var firstDayOfWeek: Int
        get() = mFirstDayOfWeek
        set(firstDayOfWeek) {
            mFirstDayOfWeek = firstDayOfWeek
            reload()
        }
    open var state: Int
        get() = mState
        set(state) {
            mState = state
            if (mState == STATE_EXPANDED) {
                mLayoutBtnGroupMonth!!.visibility = VISIBLE
                mLayoutBtnGroupWeek!!.visibility = GONE
            }
            if (mState == STATE_COLLAPSED) {
                mLayoutBtnGroupMonth!!.visibility = GONE
                mLayoutBtnGroupWeek!!.visibility = VISIBLE
            }
        }
    var textColor: Int
        get() = mTextColor
        set(textColor) {
            mTextColor = textColor
            redraw()
            mTxtTitle!!.setTextColor(mTextColor)
        }
    var primaryColor: Int
        get() = mPrimaryColor
        set(primaryColor) {
            mPrimaryColor = primaryColor
            redraw()
            mLayoutRoot!!.setBackgroundColor(mPrimaryColor)
        }
    var eventDotSize: Int
        get() = mEventDotSize
        private set(eventDotSize) {
            mEventDotSize = eventDotSize
            redraw()
        }
    var eventColor: Int
        get() = mEventColor
        private set(eventColor) {
            mEventColor = eventColor
            redraw()
        }
    var todayItemTextColor: Int
        get() = mTodayItemTextColor
        set(todayItemTextColor) {
            mTodayItemTextColor = todayItemTextColor
            redraw()
        }

    var todayItemIconColor: Int
        get() = mTodayItemIconColor
        set(value){
            mTodayItemIconColor = value
            redraw()
        }

    var itemIconColor: Int
        get() = mItemIconColor
        set(value){
            mItemIconColor = value
            redraw()
        }
    var todayItemBackgroundDrawable: Drawable
        get() = mTodayItemBackgroundDrawable
        set(todayItemBackgroundDrawable) {
            mTodayItemBackgroundDrawable = todayItemBackgroundDrawable
            redraw()
        }
    var selectedItemTextColor: Int
        get() = mSelectedItemTextColor
        set(selectedItemTextColor) {
            mSelectedItemTextColor = selectedItemTextColor
            redraw()
        }
    var selectedItemBackgroundDrawable: Drawable
        get() = mSelectedItemBackgroundDrawable
        set(selectedItemBackground) {
            mSelectedItemBackgroundDrawable = selectedItemBackground
            redraw()
        }
    var buttonLeftDrawable: Drawable
        get() = mButtonLeftDrawable
        set(buttonLeftDrawable) {
            mButtonLeftDrawable = buttonLeftDrawable
            mBtnPrevMonth!!.setImageDrawable(buttonLeftDrawable)
            mBtnPrevWeek!!.setImageDrawable(buttonLeftDrawable)
        }
    var buttonRightDrawable: Drawable
        get() = mButtonRightDrawable
        set(buttonRightDrawable) {
            mButtonRightDrawable = buttonRightDrawable
            mBtnNextMonth!!.setImageDrawable(buttonRightDrawable)
            mBtnNextWeek!!.setImageDrawable(buttonRightDrawable)
        }

    companion object {
        // Day of Week
        const val SUNDAY = 0
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 3
        const val THURSDAY = 4
        const val FRIDAY = 5
        const val SATURDAY = 6

        // State
        const val STATE_EXPANDED = 0
        const val STATE_COLLAPSED = 1
        const val STATE_PROCESSING = 2
        const val EVENT_DOT_BIG = 0
        const val EVENT_DOT_SMALL = 1
    }

    init {
        init(context)
        val attributes = context.theme.obtainStyledAttributes(
            attrs, R.styleable.UICalendar, defStyleAttr, 0
        )
        setAttributes(attributes)
        attributes.recycle()
    }
}