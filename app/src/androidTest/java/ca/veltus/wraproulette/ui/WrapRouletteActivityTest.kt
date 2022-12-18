package ca.veltus.wraproulette.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import ca.veltus.wraproulette.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@LargeTest
class WrapRouletteActivityTest {

    private lateinit var activityScenario: ActivityScenario<WrapRouletteActivity>

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setupActivityScenario() {
        activityScenario = ActivityScenario.launch(WrapRouletteActivity::class.java)
    }

    @After
    fun closeActivityScenario() {
        activityScenario.close()
    }

    @Test
    fun openAccountScreen_changeFieldsAndSave() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText("Account")).perform(click())
        onView(withId(R.id.nameInputEditText)).perform(click(), clearText(), closeSoftKeyboard())
        onView(withId(R.id.departmentInputEditText)).perform(
            click(), clearText(), closeSoftKeyboard()
        )
        onView(withId(R.id.nameInputEditText)).perform(
            click(), typeText("John Doe"), closeSoftKeyboard()
        )
        onView(withId(R.id.departmentInputEditText)).perform(
            click(), typeText("Director of Photography"), closeSoftKeyboard()
        )
        onView(withId(R.id.saveButton)).perform(click())
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        Thread.sleep(5000)
    }

    @Test
    fun openPoolsScreen_createAndEditNewPool() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText("Account")).perform(click())
        onView(withId(R.id.nameInputEditText)).perform(click(), clearText(), closeSoftKeyboard())
        onView(withId(R.id.departmentInputEditText)).perform(
            click(), clearText(), closeSoftKeyboard()
        )
        onView(withId(R.id.nameInputEditText)).perform(
            click(), typeText("John Doe"), closeSoftKeyboard()
        )
        onView(withId(R.id.departmentInputEditText)).perform(
            click(), typeText("Director of Photography"), closeSoftKeyboard()
        )
        onView(withId(R.id.saveButton)).perform(click())
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        Thread.sleep(5000)
    }
}