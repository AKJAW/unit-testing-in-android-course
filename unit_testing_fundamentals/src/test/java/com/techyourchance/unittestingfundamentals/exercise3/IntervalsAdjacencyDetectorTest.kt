package com.techyourchance.unittestingfundamentals.exercise3

import com.techyourchance.unittestingfundamentals.example3.Interval
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class IntervalsAdjacencyDetectorTest {

    private val SUT = IntervalsAdjacencyDetector()

    //TODO conained within should've also be done

    @Test
    fun `detecting distant intervals from left returns false`(){
        val interval1 = Interval(-5, 0)
        val interval2 = Interval(5, 10)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(false))
    }

    @Test
    fun `detecting distant intervals from right returns false`(){
        val interval1 = Interval(5, 10)
        val interval2 = Interval(-5, 0)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(false))
    }

    @Test
    fun `detecting adjecent intervals from left returns true`(){
        val interval1 = Interval(0, 5)
        val interval2 = Interval(5, 10)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(true))
    }

    @Test
    fun `detecting adjecent intervals from right returns true`(){
        val interval1 = Interval(5, 10)
        val interval2 = Interval(0, 5)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(true))
    }

    @Test
    fun `detecting overlapping intervals from left returns false`(){
        val interval1 = Interval(0, 7)
        val interval2 = Interval(3, 10)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(false))
    }

    @Test
    fun `detecting overlapping intervals from right returns false`(){
        val interval1 = Interval(3, 10)
        val interval2 = Interval(0, 7)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(false))
    }

    @Test
    fun `detecting the intervals returns false`(){
        val interval1 = Interval(3, 10)
        val interval2 = Interval(3, 10)
        val result = SUT.isAdjacent(interval1, interval2)
        assertThat(result, `is`(false))
    }
}