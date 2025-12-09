package com.greencopper.toolkit.appinstance

import com.greencopper.testmocks.setupTest
import com.greencopper.toolkit.App
import com.greencopper.toolkit.Toolkit
import com.greencopper.toolkit.di.bindTestingAssembly
import com.greencopper.toolkit.di.resolver.resolve
import com.greencopper.toolkit.di.resolver.tryResolve
import com.greencopper.toolkit.logging.MockLoggingConfiguration
import com.greencopper.toolkit.logging.d
import com.greencopper.toolkit.testingdata.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ConcreteAppInstanceTest {

    private val loggingConfig = MockLoggingConfiguration()

    @BeforeEach
    fun setup() {
        Toolkit.setupTest(listOf(loggingConfig))
        bindTestingAssembly(App.resolve())
    }

    @Test
    fun getDate() {
        val date = App.date()
        Thread.sleep(1)
        val date2 = App.date()
        assertThat(date).isNotEqualTo(date2)
    }

    @Test
    fun setDate() {
        App.date = { ZonedDateTime.parse("2007-12-03T10:15:30+01:00[Europe/Paris]") }
        val date = App.date()
        Thread.sleep(1)
        val date2 = App.date()
        assertThat(date).isEqualTo(date2)
    }

    @Test
    fun getLocale() {
        assertThat(App.locale).isEqualTo(Locale.getDefault())
    }

    @Test
    fun getTimeZone() {
        assertThat(App.zoneId).isEqualTo(ZoneId.systemDefault())
    }

    @Test
    fun setTimeZone() {
        App.zoneId = ZoneId.of("Europe/Paris")
        val parisHour = App.date().hour
        App.zoneId = ZoneId.of("America/New_York")
        val newYorkHour = App.date().hour
        assertThat(parisHour).isNotEqualTo(newYorkHour)
    }

    @Test
    fun resolve() {
        val dog1 = App.resolve<Dog>()
        val dog2 = App.resolve<Dog>()
        assertThat(dog1).isNotNull
        assertThat(dog2).isNotNull
        assertThat(dog1).isEqualTo(dog2)
    }

    @Test
    fun getLog() {
        val message = "DebugTest"
        App.log.d(message)
        assertThat(loggingConfig.logContent).contains(message)
    }

    @Test
    fun whenZoo1IsRegistered_ShouldHaveAnimals() {
        val zoo = App.tryResolve<Zoo1>("zoo1")
        assertThat(zoo?.dog).isNotNull
    }

    @Test
    fun whenZoo2IsRegistered_ShouldHaveAnimals() {
        val zoo = App.tryResolve<Zoo2>("zoo2")
        assertThat(zoo?.dog).isNotNull
        assertThat(zoo?.dog2).isNotNull
    }

    @Test
    fun whenZoo3IsRegistered_ShouldHaveAnimals() {
        val zoo = App.tryResolve<Zoo3>("zoo3")
        assertThat(zoo?.dog).isNotNull
        assertThat(zoo?.dog2).isNotNull
        assertThat(zoo?.dog3).isNotNull
    }

    @Test
    fun whenZoo4IsRegistered_ShouldHaveAnimals() {
        val zoo = App.tryResolve<Zoo4>("zoo4")
        assertThat(zoo?.dog).isNotNull
        assertThat(zoo?.dog2).isNotNull
        assertThat(zoo?.dog3).isNotNull
        assertThat(zoo?.dog4).isNotNull
    }

    @Test
    fun whenZoo5IsRegistered_ShouldHaveAnimals() {
        val zoo = App.resolve<Zoo5>("zoo5")
        assertThat(zoo.dog).isNotNull
        assertThat(zoo.dog2).isNotNull
        assertThat(zoo.dog3).isNotNull
        assertThat(zoo.dog4).isNotNull
        assertThat(zoo.dog5).isNotNull
    }

    @Test
    fun whenZoo6IsRegistered_ShouldHaveAnimals() {
        val zoo = App.resolve<Zoo6>("zoo6")
        assertThat(zoo.dog).isNotNull
        assertThat(zoo.dog2).isNotNull
        assertThat(zoo.dog3).isNotNull
        assertThat(zoo.dog4).isNotNull
        assertThat(zoo.dog5).isNotNull
        assertThat(zoo.dog6).isNotNull
    }
}