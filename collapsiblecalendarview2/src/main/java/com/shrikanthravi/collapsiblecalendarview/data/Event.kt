package com.shrikanthravi.collapsiblecalendarview.data

import com.shrikanthravi.collapsiblecalendarview.data.Day

/**
 * Created by shrikanthravi on 06/03/18.
 */
class Event {
    var year: Int
        private set
    var month: Int
        private set
    var day: Int
        private set
    var color = 0
        private set

    constructor(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    constructor(year: Int, month: Int, day: Int, color: Int) {
        this.year = year
        this.month = month
        this.day = day
        this.color = color
    }
}