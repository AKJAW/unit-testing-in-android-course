package com.techyourchance.unittestingfundamentals.exercise1

import org.junit.Assert
import org.junit.Test

class NegativeNumberValidatorTest {
    private val SUT = NegativeNumberValidator()

    @Test
    fun `zero returns false`(){
        Assert.assertFalse(SUT.isNegative(0))
    }

    @Test
    fun `positive number returns false`(){
        Assert.assertFalse(SUT.isNegative(1))
    }

    @Test
    fun `negative number returns true`(){
        Assert.assertTrue(SUT.isNegative(-1))
    }
}