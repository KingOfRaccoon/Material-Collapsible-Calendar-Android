package com.shrikanthravi.collapsiblecalendarview.data

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.shrikanthravi.collapsiblecalendarview.R
import com.shrikanthravi.collapsiblecalendarview.widget.UICalendar
import java.util.*
import kotlin.math.ceil

/**
 * Created by shrikanthravi on 06/03/18.
 */
class CalendarAdapter(context: Context, val calendar: Calendar) {
    private var mFirstDayOfWeek = 1
    private val mInflater: LayoutInflater
    private var mEventDotSize = UICalendar.EVENT_DOT_BIG
    private var mItemList: MutableList<Day> = ArrayList()
    private var mViewList: MutableList<View> = ArrayList()
    private var mEventList: MutableList<Event> = ArrayList()

    // public methods
    val count: Int
        get() = mItemList.size

    fun getItem(position: Int): Day {
        return mItemList[position]
    }

    fun getView(position: Int): View {
        return mViewList[position]
    }

    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        mFirstDayOfWeek = firstDayOfWeek
    }

    fun setEventDotSize(eventDotSize: Int) {
        mEventDotSize = eventDotSize
    }

    fun addEvent(event: Event) {
        mEventList.add(event)
    }

    fun refresh() {
        // clear data
        mItemList.clear()
        mViewList.clear()

        // set calendar
        val year = calendar[Calendar.YEAR]
        val month = calendar[Calendar.MONTH]
        calendar[year, month] = 1
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = calendar[Calendar.DAY_OF_WEEK] - 1

        // generate day list
        val offset = 0 - (firstDayOfWeek - mFirstDayOfWeek) + 1
        val length = ceil(((lastDayOfMonth - offset + 1).toFloat() / 7).toDouble()).toInt() * 7
        for (i in offset until length + offset) {
            var numYear: Int
            var numMonth: Int
            var numDay: Int
            val tempCal = Calendar.getInstance()
            if (i <= 0) { // prev month
                if (month == 0) {
                    numYear = year - 1
                    numMonth = 11
                } else {
                    numYear = year
                    numMonth = month - 1
                }
                tempCal[numYear, numMonth] = 1
                numDay = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH) + i
            } else if (i > lastDayOfMonth) { // next month
                if (month == 11) {
                    numYear = year + 1
                    numMonth = 0
                } else {
                    numYear = year
                    numMonth = month + 1
                }
                tempCal[numYear, numMonth] = 1
                numDay = i - lastDayOfMonth
            } else {
                numYear = year
                numMonth = month
                numDay = i
            }
            val day = Day(numYear, numMonth, numDay)
            val view: View = if (mEventDotSize == UICalendar.EVENT_DOT_SMALL)
                mInflater.inflate(R.layout.day_layout_small, null)
            else
                mInflater.inflate(R.layout.day_layout, null)
            val txtDay = view.findViewById<View>(R.id.txt_day) as TextView
            val imgEventTag = view.findViewById<View>(R.id.img_event_tag) as ImageView
            txtDay.text = day.day.toString()
            if (day.month != calendar[Calendar.MONTH]) {
                txtDay.alpha = 0.3f
            }
            for (j in mEventList.indices) {
                val event = mEventList[j]
                if (day.year == event.year && day.month == event.month && day.day == event.day) {
                    imgEventTag.visibility = View.VISIBLE
                    imgEventTag.setColorFilter(event.color, PorterDuff.Mode.SRC_ATOP)
                }
            }
            mItemList.add(day)
            mViewList.add(view)
        }
    }

    init {
        calendar[Calendar.DAY_OF_MONTH] = 1
        mInflater = LayoutInflater.from(context)
        refresh()
    }
}