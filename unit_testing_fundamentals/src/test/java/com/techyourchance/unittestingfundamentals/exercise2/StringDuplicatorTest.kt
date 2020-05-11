package com.techyourchance.unittestingfundamentals.exercise2

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

class StringDuplicatorTest {

    private val SUT = StringDuplicator()

    @Test
    fun `duplicating an empty string returns an empty string`(){
        val result = SUT.duplicate("")
        assertThat(result, `is`(""))
    }

    @Test
    fun `duplicating whitespace returns duplicated whitespace`(){
        val result = SUT.duplicate(" ")
        assertThat(result, `is`("  "))
    }

    @Test
    fun `duplicating a string returns duplicated string`(){
        val result = SUT.duplicate("ala")
        assertThat(result, `is`("alaala"))
    }
}