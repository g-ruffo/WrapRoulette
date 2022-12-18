package ca.veltus.wraproulette.authentication

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import ca.veltus.wraproulette.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@LargeTest
class LoginSignupActivityTest {

    private lateinit var activityScenario: ActivityScenario<LoginSignupActivity>

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setupActivityScenario() {
        activityScenario = ActivityScenario.launch(LoginSignupActivity::class.java)
    }

    @After
    fun closeActivityScenario() {
        activityScenario.close()
    }

    @Test
    fun registerAndLogin_createAndEditPoolAndAccount() {
        val textInputEditText = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText.perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText("grayson@veltus.ca"),
            ViewActions.closeSoftKeyboard()
        )

        val textInputEditText2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText),
                ViewMatchers.withText("grayson@veltus.ca"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText2.perform(ViewActions.scrollTo(), ViewActions.click())

        val textInputEditText3 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText),
                ViewMatchers.withText("grayson@veltus.ca"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText3.perform(ViewActions.scrollTo(), ViewActions.click())

        val textInputEditText4 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText),
                ViewMatchers.withText("grayson@veltus.ca"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText4.perform(ViewActions.scrollTo(), ViewActions.replaceText(""))

        val textInputEditText5 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                ), ViewMatchers.isDisplayed()
            )
        )
        textInputEditText5.perform(ViewActions.closeSoftKeyboard())

        val materialTextView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.signUpTextView),
                ViewMatchers.withText("Not registered? Click to signup"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 5
                )
            )
        )
        materialTextView.perform(ViewActions.scrollTo(), ViewActions.click())

        val textInputEditText6 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nameInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.nameEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText6.perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText("John Doe"),
            ViewActions.closeSoftKeyboard()
        )

        val textInputEditText7 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.emailInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.emailEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText7.perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText("grayson@veltus.ca"),
            ViewActions.closeSoftKeyboard()
        )

        val textInputEditText8 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.departmentInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.departmentEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText8.perform(
            ViewActions.scrollTo(), ViewActions.replaceText("Grip"), ViewActions.closeSoftKeyboard()
        )

        val textInputEditText9 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.passwordInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.passwordEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText9.perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText("passwo"),
            ViewActions.closeSoftKeyboard()
        )

        val textInputEditText10 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.passwordInputEditText),
                ViewMatchers.withText("passwo"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.passwordEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText10.perform(ViewActions.scrollTo(), ViewActions.replaceText("password"))

        val textInputEditText11 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.passwordInputEditText),
                ViewMatchers.withText("password"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.passwordEditTextLayout), 0
                    ), 0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textInputEditText11.perform(ViewActions.closeSoftKeyboard())

        val materialButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.signUpButton),
                ViewMatchers.withText("Sign Up"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 7
                )
            )
        )
        materialButton.perform(ViewActions.scrollTo(), ViewActions.click())

        val appCompatImageView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.backButton),
                ViewMatchers.withContentDescription("Back image"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 0
                )
            )
        )
        appCompatImageView.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.signInButton),
                ViewMatchers.withText("Sign In"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 4
                )
            )
        )
        materialButton2.perform(ViewActions.scrollTo(), ViewActions.click())

        val appCompatImageButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withContentDescription("Open navigation drawer"), childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar), childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ), 1
                ), ViewMatchers.isDisplayed()
            )
        )
        appCompatImageButton.perform(ViewActions.click())

        val navigationMenuItemView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nav_pools), childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(com.google.android.material.R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.navHostFragmentMain), 0
                        )
                    ), 2
                ), ViewMatchers.isDisplayed()
            )
        )
        navigationMenuItemView.perform(ViewActions.click())

        val extendedFloatingActionButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.expandFab),
                ViewMatchers.withText("Actions"),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.adminFabLayout), childAtPosition(
                            ViewMatchers.withId(R.id.headerLayout), 6
                        )
                    ), 0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        extendedFloatingActionButton.perform(ViewActions.click())

        val extendedFloatingActionButton2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.newPoolFab),
                ViewMatchers.withText("Create"),
                ViewMatchers.withContentDescription("Create pool fab"),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.adminFabLayout), childAtPosition(
                            ViewMatchers.withId(R.id.headerLayout), 6
                        )
                    ), 1
                ),
                ViewMatchers.isDisplayed()
            )
        )
        extendedFloatingActionButton2.perform(ViewActions.click())

        val textInputEditText12 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.productionInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.productionEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText12.perform(
            ViewActions.scrollTo(), ViewActions.replaceText("Supe"), ViewActions.closeSoftKeyboard()
        )

        val textInputEditText13 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.passwordInputEditText), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.passwordEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText13.perform(
            ViewActions.scrollTo(),
            ViewActions.replaceText("password"),
            ViewActions.closeSoftKeyboard()
        )

        val materialAutoCompleteTextView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.selectDateAutoComplete), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.selectDateTextInputLayout), 0
                    ), 0
                )
            )
        )
        materialAutoCompleteTextView.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton3 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText("Set"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.buttonPanel),
                        0
                    ), 3
                )
            )
        )
        materialButton3.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialAutoCompleteTextView2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.selectStartTimeAutoComplete), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.selectStartTimeTextInputLayout), 0
                    ), 0
                )
            )
        )
        materialAutoCompleteTextView2.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton4 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText("Set"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.buttonPanel),
                        0
                    ), 3
                )
            )
        )
        materialButton4.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialAutoCompleteTextView3 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.selectBettingCloseTimeAutoComplete), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.selectBettingCloseTimeTextInputLayout), 0
                    ), 0
                )
            )
        )
        materialAutoCompleteTextView3.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton5 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText("Set"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.buttonPanel),
                        0
                    ), 3
                )
            )
        )
        materialButton5.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialAutoCompleteTextView4 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.selectBetAmountAutoComplete), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.selectBetAmountTextInputLayout), 0
                    ), 0
                )
            )
        )
        materialAutoCompleteTextView4.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton6 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText("Set"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.buttonPanel),
                        0
                    ), 3
                )
            )
        )
        materialButton6.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialAutoCompleteTextView5 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.selectMarginAutoComplete), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.selectMarginTextInputLayout), 0
                    ), 0
                )
            )
        )
        materialAutoCompleteTextView5.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton7 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(android.R.id.button1),
                ViewMatchers.withText("Set"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.buttonPanel),
                        0
                    ), 3
                )
            )
        )
        materialButton7.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialCheckBox = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.pIRCheckbox),
                ViewMatchers.withText("Price Is Right Rules"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 9
                )
            )
        )
        materialCheckBox.perform(ViewActions.scrollTo(), ViewActions.click())

        val materialButton8 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.createButton),
                ViewMatchers.withText("Create"),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.buttonLinearLayout), childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            11
                        )
                    ), 1
                )
            )
        )
        materialButton8.perform(ViewActions.scrollTo(), ViewActions.click())

        val overflowMenuButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withContentDescription("More options"), childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.toolbar), 2
                    ), 0
                ), ViewMatchers.isDisplayed()
            )
        )
        overflowMenuButton.perform(ViewActions.click())

        val materialTextView2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.title),
                ViewMatchers.withText("Edit Pool"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(androidx.databinding.library.baseAdapters.R.id.content),
                        0
                    ), 0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        materialTextView2.perform(ViewActions.click())

        val textInputEditText14 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.productionInputEditText),
                ViewMatchers.withText("Supe"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.productionEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText14.perform(ViewActions.scrollTo(), ViewActions.replaceText("Superman "))

        val textInputEditText15 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.productionInputEditText),
                ViewMatchers.withText("Superman "),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.productionEditTextLayout), 0
                    ), 0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textInputEditText15.perform(ViewActions.closeSoftKeyboard())

        val materialButton9 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.createButton),
                ViewMatchers.withText("Update"),
                childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.buttonLinearLayout), childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                            11
                        )
                    ), 1
                )
            )
        )
        materialButton9.perform(ViewActions.scrollTo(), ViewActions.click())

        val textView = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText("Superman"), ViewMatchers.withParent(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar),
                        ViewMatchers.withParent(IsInstanceOf.instanceOf(LinearLayout::class.java))
                    )
                ), ViewMatchers.isDisplayed()
            )
        )
        textView.check(ViewAssertions.matches(ViewMatchers.withText("Superman")))

        val appCompatImageButton2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withContentDescription("Open navigation drawer"), childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar), childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ), 1
                ), ViewMatchers.isDisplayed()
            )
        )
        appCompatImageButton2.perform(ViewActions.click())

        val navigationMenuItemView2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nav_account), childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(com.google.android.material.R.id.design_navigation_view),
                        childAtPosition(
                            ViewMatchers.withId(R.id.navHostFragmentMain), 0
                        )
                    ), 3
                ), ViewMatchers.isDisplayed()
            )
        )
        navigationMenuItemView2.perform(ViewActions.click())

        val textInputEditText16 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nameInputEditText),
                ViewMatchers.withText("John Doe"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.nameEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText16.perform(ViewActions.scrollTo(), ViewActions.click())

        val textInputEditText17 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nameInputEditText),
                ViewMatchers.withText("John Doe"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.nameEditTextLayout), 0
                    ), 0
                )
            )
        )
        textInputEditText17.perform(ViewActions.scrollTo(), ViewActions.replaceText("Jane Doe"))

        val textInputEditText18 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.nameInputEditText),
                ViewMatchers.withText("Jane Doe"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withId(R.id.nameEditTextLayout), 0
                    ), 0
                ),
                ViewMatchers.isDisplayed()
            )
        )
        textInputEditText18.perform(ViewActions.closeSoftKeyboard())

        val materialButton10 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.saveButton),
                ViewMatchers.withText("Save"),
                childAtPosition(
                    childAtPosition(
                        ViewMatchers.withClassName(Matchers.`is`("android.widget.ScrollView")), 0
                    ), 4
                )
            )
        )
        materialButton10.perform(ViewActions.scrollTo(), ViewActions.click())

        val appCompatImageButton3 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withContentDescription("Open navigation drawer"), childAtPosition(
                    Matchers.allOf(
                        ViewMatchers.withId(R.id.toolbar), childAtPosition(
                            ViewMatchers.withClassName(Matchers.`is`("com.google.android.material.appbar.AppBarLayout")),
                            0
                        )
                    ), 1
                ), ViewMatchers.isDisplayed()
            )
        )
        appCompatImageButton3.perform(ViewActions.click())

        val textView2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.userNameHeaderTextView),
                ViewMatchers.withText("Jane Doe"),
                ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(com.google.android.material.R.id.navigation_header_container))),
                ViewMatchers.isDisplayed()
            )
        )
        textView2.check(ViewAssertions.matches(ViewMatchers.withText("Jane Doe")))
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent) && view == parent.getChildAt(
                    position
                )
            }
        }
    }
}