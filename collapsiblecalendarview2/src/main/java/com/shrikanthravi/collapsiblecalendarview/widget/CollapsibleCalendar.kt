package com.shrikanthravi.collapsiblecalendarview.widget

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.updatePadding
import com.shrikanthravi.collapsiblecalendarview.R
import com.shrikanthravi.collapsiblecalendarview.data.CalendarAdapter
import com.shrikanthravi.collapsiblecalendarview.data.Day
import com.shrikanthravi.collapsiblecalendarview.data.Event
import com.shrikanthravi.collapsiblecalendarview.listener.OnSwipeTouchListener
import com.shrikanthravi.collapsiblecalendarview.view.ExpandIconView
import com.sinaseyfi.advancedcardview.AdvancedCardView
import com.transitionseverywhere.ChangeText
import com.transitionseverywhere.TransitionManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by shrikanthravi on 07/03/18.
 */
class CollapsibleCalendar : UICalendar {
    private var mAdapter: CalendarAdapter? = null
    private var mListener: CalendarListener? = null
    private var expanded = false
    private var mInitHeight = 0
    private val mHandler = Handler()
    private var mIsWaitingForUpdate = false
    private var mCurrentWeekIndex = 0

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun init(context: Context) {
        super.init(context)
        val size = eventDotSize
        val cal = Calendar.getInstance()
        val adapter = CalendarAdapter(context, cal)
        adapter.setEventDotSize(eventDotSize)
        setAdapter(adapter)


        // bind events
        mLayoutRoot!!.setOnTouchListener(swipeTouchListener)
        mBtnPrevMonth!!.setOnClickListener { prevMonth() }
        mBtnNextMonth!!.setOnClickListener { nextMonth() }
        mBtnPrevWeek!!.setOnClickListener { prevWeek() }
        mBtnNextWeek!!.setOnClickListener { nextWeek() }
        expandIconView!!.setState(ExpandIconView.MORE, true)
        expandIconView!!.setOnClickListener {
            if (expanded) {
                collapse(400)
            } else {
                expand(400)
            }
        }
        post { collapseTo(mCurrentWeekIndex) }
    }

    private val swipeTouchListener: OnSwipeTouchListener
        get() = object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
                collapse(400)
            }

            override fun onSwipeLeft() {
                if (state == STATE_COLLAPSED) nextWeek()
                else if (state == STATE_EXPANDED) nextMonth()
            }

            override fun onSwipeRight() {
                if (state == STATE_COLLAPSED) {
                    prevWeek()
                } else if (state == STATE_EXPANDED) {
                    prevMonth()
                }
            }

            override fun onSwipeBottom() {
                expand(400)
            }
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mInitHeight = mTableBody!!.measuredHeight
        if (mIsWaitingForUpdate) {
            redraw()
            mHandler.post { collapseTo(mCurrentWeekIndex) }
            mIsWaitingForUpdate = false
            if (mListener != null) {
                mListener!!.onDataUpdate()
            }
        }
    }

    override fun redraw() {
        // redraw all views of week
        containerTableBody?.setCardBackgroundColor(colorContainerTableBody)
        val rowWeek = mTableHead!!.getChildAt(0) as TableRow
        for (i in 0 until rowWeek.childCount) {
            (rowWeek.getChildAt(i) as TextView).setTextColor(textColor)
        }
        // redraw all views of day
        if (mAdapter != null) {
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)
                val view = mAdapter!!.getView(i)
                val containerTxtDay = view.findViewById<AdvancedCardView>(R.id.container_txt_day)
                val txtDay = view.findViewById<TextView>(R.id.txt_day)
                val iconDay = view.findViewById<ImageView>(R.id.img_event_tag)
                txtDay.setBackgroundColor(Color.TRANSPARENT)
                txtDay.setTextColor(textColor)

                // set today's item
                if (isToady(day)) {
                    if (mTypeSelectionToday == 1) {
                        containerTxtDay.background_Type = AdvancedCardView.BackgroundType.Fill
                        containerTxtDay.background = todayItemBackgroundDrawable
                        txtDay.setTextColor(todayItemTextColor)
                        iconDay.setColorFilter(todayItemIconColor)
                    } else if (mTypeSelectionToday == 0) {
                        containerTxtDay.background_Type = AdvancedCardView.BackgroundType.Stroke
                        containerTxtDay.stroke_Gradient_Colors = strokeGradientColorsToday
//                        containerTxtDay.stroke_Width = dp(view.context, 2)
                    }
                } else {
//                    containerTxtDay.stroke_Width = dp(view.context, 0)
                    iconDay.setColorFilter(itemIconColor)
                }

                // set the selected item
                if (isSelectedDay(day)) {
                    txtDay.setTextColor(selectedItemTextColor)
                    if (mTypeSelection == 1) {
                        containerTxtDay.background_Type = AdvancedCardView.BackgroundType.Fill
                        containerTxtDay.background = selectedItemBackgroundDrawable
                    } else if (mTypeSelection == 0) {
                        containerTxtDay.background = null
                        containerTxtDay.background_Type = AdvancedCardView.BackgroundType.Stroke
                        containerTxtDay.stroke_Gradient_Colors = strokeGradientColorsSelect
//                        containerTxtDay.stroke_Width = dp(view.context, 2)
                        containerTxtDay.invalidate()
                    }
                } else if (!isToady(day)) {
                    containerTxtDay.background_Type = AdvancedCardView.BackgroundType.Fill
                    iconDay.setColorFilter(itemIconColor)
                    containerTxtDay.stroke_Alpha = 0f
//                    containerTxtDay.stroke_Width = 0f
                    containerTxtDay.invalidate()
                }
            }
        }
    }

    override fun reload() {
        if (mAdapter != null) {
            mAdapter!!.setEventDotSize(eventDotSize)
            mAdapter!!.refresh()

            // reset UI
            val dateFormat = SimpleDateFormat("LLLL yyyy")
            dateFormat.timeZone = mAdapter!!.calendar.timeZone
            val newText = dateFormat.format(mAdapter!!.calendar.time)
                .replace(Calendar.getInstance().get(Calendar.YEAR).toString(), "").trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            if (mLayoutRoot != null && newText != mTxtTitle?.text) {
                TransitionManager.beginDelayedTransition(
                    mLayoutRoot!!,
                    ChangeText().setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_OUT)
                        .setChangeBehavior(ChangeText.CHANGE_BEHAVIOR_IN)
                )
                mTxtTitle!!.text = newText
            }
            mTableHead!!.removeAllViews()
            mTableBody!!.removeAllViews()
            var rowCurrent: TableRow

            // set day of week
            val dayOfWeekIds = intArrayOf(
                R.string.sunday,
                R.string.monday,
                R.string.tuesday,
                R.string.wednesday,
                R.string.thursday,
                R.string.friday,
                R.string.saturday
            )
            rowCurrent = TableRow(mContext)
            rowCurrent.layoutParams = TableLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            for (i in 0..6) {
                val view = mInflater.inflate(R.layout.layout_day_of_week, null)
                val txtDayOfWeek = view.findViewById<View>(R.id.txt_day_of_week) as TextView
                txtDayOfWeek.setText(dayOfWeekIds[(i + firstDayOfWeek) % 7])
                view.layoutParams = TableRow.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                rowCurrent.addView(view)
            }
            mTableHead!!.addView(rowCurrent)

            // set day view
            for (i in 0 until mAdapter!!.count) {
                if (i % 7 == 0) {
                    rowCurrent = TableRow(mContext)
//                    println("update padding: $i")
//                    rowCurrent.updatePadding(0,0, 0, dp(context, 5).toInt())
                    rowCurrent.layoutParams = TableLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    mTableBody!!.addView(rowCurrent)
                }
                val view = mAdapter!!.getView(i)
                view.layoutParams = TableRow.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                view.setOnTouchListener(swipeTouchListener)
                view.setOnClickListener { v -> onItemClicked(v, mAdapter!!.getItem(i)) }
                rowCurrent.addView(view)
            }
            redraw()
            mIsWaitingForUpdate = true
        }
    }

    private val suitableRowIndex: Int
        private get() = if (selectedItemPosition != -1) {
            val view = mAdapter!!.getView(selectedItemPosition)
            val row = view.parent as TableRow
            mTableBody!!.indexOfChild(row)
        } else if (todayItemPosition != -1) {
            val view = mAdapter!!.getView(todayItemPosition)
            val row = view.parent as TableRow
            mTableBody!!.indexOfChild(row)
        } else {
            0
        }

    fun onItemClicked(view: View?, day: Day) {
        select(day)
        val cal = mAdapter!!.calendar
        val newYear = day.year
        val newMonth = day.month
        val oldYear = cal[Calendar.YEAR]
        val oldMonth = cal[Calendar.MONTH]
        if (newMonth != oldMonth) {
            cal[day.year, day.month] = 1
            if (newYear > oldYear || newMonth > oldMonth) {
                mCurrentWeekIndex = 0
            }
            if (newYear < oldYear || newMonth < oldMonth) {
                mCurrentWeekIndex = -1
            }
            if (mListener != null) {
                mListener!!.onMonthChange()
            }
            reload()
        }
        if (mListener != null) {
            mListener!!.onItemClick(view)
        }
    }

    // public methods
    fun setAdapter(adapter: CalendarAdapter) {
        mAdapter = adapter
        adapter.setFirstDayOfWeek(firstDayOfWeek)
        reload()

        // init week
        mCurrentWeekIndex = suitableRowIndex
    }

    fun addEventTag(numYear: Int, numMonth: Int, numDay: Int) {
        mAdapter!!.addEvent(Event(numYear, numMonth, numDay, eventColor))
        reload()
    }

    fun addEventTag(numYear: Int, numMonth: Int, numDay: Int, color: Int) {
        mAdapter!!.addEvent(Event(numYear, numMonth, numDay, color))
        reload()
    }

    fun prevMonth() {
        val cal = mAdapter!!.calendar
        if (cal[Calendar.MONTH] == cal.getActualMinimum(Calendar.MONTH)) {
            cal[cal[Calendar.YEAR] - 1, cal.getActualMaximum(Calendar.MONTH)] = 1
        } else {
            cal[Calendar.MONTH] = cal[Calendar.MONTH] - 1
        }
        reload()
        if (mListener != null) {
            mListener!!.onMonthChange()
        }
    }

    fun nextMonth() {
        val cal = mAdapter!!.calendar
        if (cal[Calendar.MONTH] == cal.getActualMaximum(Calendar.MONTH)) {
            cal[cal[Calendar.YEAR] + 1, cal.getActualMinimum(Calendar.MONTH)] = 1
        } else {
            cal[Calendar.MONTH] = cal[Calendar.MONTH] + 1
        }
        reload()
        if (mListener != null) {
            mListener!!.onMonthChange()
        }
    }

    fun prevWeek() {
        if (mCurrentWeekIndex - 1 < 0) {
            mCurrentWeekIndex = -1
            prevMonth()
        } else {
            mCurrentWeekIndex--
            collapseTo(mCurrentWeekIndex)
        }
    }

    fun nextWeek() {
        if (mCurrentWeekIndex + 1 >= mTableBody!!.childCount) {
            mCurrentWeekIndex = 0
            nextMonth()
        } else {
            mCurrentWeekIndex++
            collapseTo(mCurrentWeekIndex)
        }
    }

    val year: Int
        get() = mAdapter!!.calendar[Calendar.YEAR]
    val month: Int
        get() = mAdapter!!.calendar[Calendar.MONTH]
    val selectedDay: Day
        get() {
            if (selectedItem != null) {
                return Day(
                    selectedItem!!.year,
                    selectedItem!!.month,
                    selectedItem!!.day
                )
            } else {
                val cal = Calendar.getInstance()
                val day = cal[Calendar.DAY_OF_MONTH]
                val month = cal[Calendar.MONTH]
                val year = cal[Calendar.YEAR]
                return Day(
                    year,
                    month + 1,
                    day
                )
            }
        }

    fun isSelectedDay(day: Day?): Boolean {
        return day != null && selectedItem != null
                && day.year == selectedItem!!.year
                && day.month == selectedItem!!.month
                && day.day == selectedItem!!.day
    }

    fun isToady(day: Day?): Boolean {
        val todayCal = Calendar.getInstance()
        return day != null && day.year == todayCal[Calendar.YEAR] && day.month == todayCal[Calendar.MONTH] && day.day == todayCal[Calendar.DAY_OF_MONTH]
    }

    val selectedItemPosition: Int
        get() {
            var position = -1
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)
                if (isSelectedDay(day)) {
                    position = i
                    break
                }
            }
            return position
        }
    val todayItemPosition: Int
        get() {
            var position = -1
            for (i in 0 until mAdapter!!.count) {
                val day = mAdapter!!.getItem(i)
                if (isToady(day)) {
                    position = i
                    break
                }
            }
            return position
        }

    fun collapse(duration: Int) {
        if (state == STATE_EXPANDED) {
            state = STATE_PROCESSING
            mLayoutBtnGroupMonth!!.visibility = GONE
            mLayoutBtnGroupWeek!!.visibility = VISIBLE
            mBtnPrevWeek!!.isClickable = false
            mBtnNextWeek!!.isClickable = false
            val index = suitableRowIndex
            mCurrentWeekIndex = index
            val currentHeight = mInitHeight
            val targetHeight = mTableBody!!.getChildAt(index).measuredHeight
            var tempHeight = 0
            for (i in 0 until index) {
                tempHeight += mTableBody!!.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight
            val anim: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    mScrollViewBody!!.layoutParams.height =
                        if (interpolatedTime == 1f) targetHeight else currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody!!.requestLayout()
                    if (mScrollViewBody!!.measuredHeight < topHeight + targetHeight) {
                        val position = topHeight + targetHeight - mScrollViewBody!!.measuredHeight
                        mScrollViewBody!!.smoothScrollTo(0, position)
                    }
                    if (interpolatedTime == 1f) {
                        state = STATE_COLLAPSED
                        mBtnPrevWeek!!.isClickable = true
                        mBtnNextWeek!!.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }
        expandIconView!!.setState(ExpandIconView.MORE, true)
    }

    private fun collapseTo(index: Int) {
        var indexItem = index
        if (state == STATE_COLLAPSED) {
            if (indexItem == -1) {
                indexItem = mTableBody!!.childCount - 1
            }
            mCurrentWeekIndex = indexItem
            val targetHeight = mTableBody!!.getChildAt(indexItem).measuredHeight
            var tempHeight = 0
            for (i in 0 until indexItem) {
                tempHeight += mTableBody!!.getChildAt(i).measuredHeight
            }
            val topHeight = tempHeight
            mScrollViewBody!!.layoutParams.height = targetHeight
            mScrollViewBody!!.requestLayout()
            mHandler.post { mScrollViewBody!!.smoothScrollTo(0, topHeight) }
            if (mListener != null) {
                mListener!!.onWeekChange(mCurrentWeekIndex)
            }
        }
    }

    fun expand(duration: Int) {
        if (state == STATE_COLLAPSED) {
            state = STATE_PROCESSING
            mLayoutBtnGroupMonth!!.visibility = VISIBLE
            mLayoutBtnGroupWeek!!.visibility = GONE
            mBtnPrevMonth!!.isClickable = false
            mBtnNextMonth!!.isClickable = false
            val currentHeight = mScrollViewBody!!.measuredHeight
            val targetHeight = mInitHeight
            val anim: Animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                    mScrollViewBody!!.layoutParams.height =
                        if (interpolatedTime == 1f) LayoutParams.WRAP_CONTENT else currentHeight - ((currentHeight - targetHeight) * interpolatedTime).toInt()
                    mScrollViewBody!!.requestLayout()
                    if (interpolatedTime == 1f) {
                        state = STATE_EXPANDED
                        mBtnPrevMonth!!.isClickable = true
                        mBtnNextMonth!!.isClickable = true
                    }
                }
            }
            anim.duration = duration.toLong()
            startAnimation(anim)
        }
        expandIconView!!.setState(ExpandIconView.LESS, true)
    }

    override var state: Int
        get() = super.state
        set(state) {
            super.state = state
            if (state == STATE_COLLAPSED) {
                expanded = false
            }
            if (state == STATE_EXPANDED) {
                expanded = true
            }
        }

    fun select(day: Day) {
        selectedItem = Day(day.year, day.month, day.day)
        redraw()
        if (mListener != null) {
            mListener!!.onDaySelect()
        }
    }

    fun setStateWithUpdateUI(state: Int) {
        this@CollapsibleCalendar.state = state
        if (state != state) {
            mIsWaitingForUpdate = true
            requestLayout()
        }
    }

    // callback
    fun setCalendarListener(listener: CalendarListener?) {
        mListener = listener
    }

    interface CalendarListener {
        // triggered when a day is selected programmatically or clicked by user.
        fun onDaySelect()

        // triggered only when the views of day on calendar are clicked by user.
        fun onItemClick(v: View?)

        // triggered when the data of calendar are updated by changing month or adding events.
        fun onDataUpdate()

        // triggered when the month are changed.
        fun onMonthChange()

        // triggered when the week position are changed.
        fun onWeekChange(position: Int)
    }

    fun dp(context: Context, pixel: Int) =
        context.resources.displayMetrics.density * pixel
}