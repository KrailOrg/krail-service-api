package uk.q3c.krail.service

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldEqualTo
import org.amshove.kluent.shouldNotBeTrue
import org.amshove.kluent.shouldThrow
import org.apache.commons.lang3.SerializationUtils
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.eventbus.MessageBus
import uk.q3c.krail.i18n.Translate
import uk.q3c.krail.i18n.test.MockTranslate
import uk.q3c.krail.service.State.*
import uk.q3c.krail.service.test.MockService
import uk.q3c.util.guice.DefaultSerializationSupport
import uk.q3c.util.guice.InjectorLocator
import uk.q3c.util.guice.SerializationSupport

/**
 * Created by David Sowerby on 11 Apr 2018
 */
object AbstractServiceTest : Spek({

    given("a service sub-classed from AbstractService") {
        val translate = MockTranslate()
        val messageBus: MessageBus = mock()
        val serializationSupport: SerializationSupport = DefaultSerializationSupport(TestInjectorLocator())


        lateinit var service: MockService


        beforeEachTest {
            service = MockService(translate, messageBus, serializationSupport)
        }

        on("moving through the standard cycle of start, stop, and then reset") {
            service.start()
            service.stop()
            service.reset()

            it("state changes occur in the correct order and are notified on the message bus") {
                service.state.shouldBe(INITIAL)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING, STOPPING, STOPPED, RESETTING, INITIAL)
            }
        }

        on("construction") {
            it("returns default name, description and instance number") {
                service.name.shouldBeEqualTo("not specified")
                service.description.shouldBeEqualTo("not specified")
                service.instanceNumber.shouldBe(0)
            }
        }

        on("changing properties") {
            service.nameKey = LabelKey.No
            service.descriptionKey = LabelKey.Yes
            service.instanceNumber = 99

            it("returns new values") {
                service.name.shouldBeEqualTo("No")
                service.description.shouldBeEqualTo("Yes")
                service.nameKey.shouldBe(LabelKey.No)
                service.descriptionKey.shouldBe(LabelKey.Yes)
                service.instanceNumber.shouldEqualTo(99)
            }
        }

        on("trying to start but failing") {
            service.failToStart = true
            service.start()

            it("state changes occur in the correct order and are notified on the message bus") {
                service.state.shouldBe(FAILED)
                service.cause.shouldBe(Cause.FAILED_TO_START)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, FAILED)
            }
        }

        on("trying to stop but failing") {
            service.failToStop = true
            service.start()
            service.stop()

            it("state changes occur in the correct order and are notified on the message bus") {
                service.state.shouldBe(FAILED)
                service.cause.shouldBe(Cause.FAILED_TO_STOP)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING, STOPPING, FAILED)
            }
        }


        on("trying to stop when already stopped") {
            service.start()
            service.stop()
            service.stop()

            it("state changes occur in the correct order, no attempt made to stop a second time") {
                service.state.shouldBe(STOPPED)
                service.cause.shouldBe(Cause.STOPPED)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING, STOPPING, STOPPED)
            }
        }

        on("trying to stop when in initial state") {

            it("Call ignored") {
                service.state.shouldBe(INITIAL)
                service.cause.shouldBe(Cause.not_specified)
                assertThat(service.statusHistory).containsExactly(INITIAL)
            }
        }

        on("trying to reset incorrectly") {
            service.start()
            val result = { service.reset() }

            it("exception thrown, because we can only reset when already stopped") {
                result shouldThrow (ServiceStatusException::class)
            }
        }

        on("trying to reset, but reset fails") {
            service.failToReset = true
            service.start()
            service.stop()
            service.reset()

            it("shows as failed, because we have been unable to reset") {
                service.state.shouldBe(FAILED)
                service.cause.shouldBe(Cause.FAILED_TO_RESET)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING, STOPPING, STOPPED, RESETTING, FAILED)
            }
        }


        on("resetting a service which is already in initial state") {
            service.reset()

            it("it ignored") {
                service.state.shouldBe(INITIAL)
                service.cause.shouldBe(Cause.not_specified)
                assertThat(service.statusHistory).containsExactly(INITIAL)
            }
        }

        on("starting a service which is already running") {
            service.start()
            service.start()

            it("ignores the call to start again") {
                service.state.shouldBe(RUNNING)
                service.cause.shouldBe(Cause.STARTED)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING)
            }
        }

        on("failing a running service") {
            service.start()
            service.fail()

            it("ignores the call to start again") {
                service.state.shouldBe(FAILED)
                service.cause.shouldBe(Cause.FAILED)
                assertThat(service.statusHistory).containsExactly(INITIAL, STARTING, RUNNING, STOPPING, FAILED)
            }
        }

        on("trying to start a service which stopping") {
            service.state = State.STOPPING
            val result = { service.start() }

            it("throws exception") {
                result.shouldThrow(ServiceStatusException::class)
            }
        }

        on("trying to start a service which has failed") {
            service.state = State.FAILED
            val result = { service.start() }

            it("throws exception") {
                result.shouldThrow(ServiceStatusException::class)
            }
        }

        on("calling isStarted when running") {
            service.state = RUNNING

            it("returns true") {
                service.isStarted.shouldBeTrue()
            }
        }

        on("calling isStarted when not running") {
            service.state = FAILED

            it("returns false") {
                service.isStarted.shouldNotBeTrue()
            }
        }

        on("calling isStopped when stopped") {
            service.state = STOPPED

            it("returns true") {
                service.isStopped.shouldBeTrue()
            }
        }

        on("calling isStopped when failed") {
            service.state = FAILED

            it("returns false") {
                service.isStopped.shouldNotBeTrue()
            }
        }
    }

})


object SerialisationTest : Spek({

    given("a Guice constructed service") {
        lateinit var service: MockService
        lateinit var injector: Injector

        beforeEachTest {
            injector = Guice.createInjector(testModules())
            InjectorHolder.injector = injector
            service = injector.getInstance(MockService::class.java)
        }


        on("serialising and deserialising") {
            service.start()
            service.instanceNumber = 99
            val output = SerializationUtils.serialize(service)
            val returnResult: MockService = SerializationUtils.deserialize(output)

            it("reconstructs with correct state, and all transient fields populated") {
                returnResult.state.shouldEqual(RUNNING)
                returnResult.instanceNumber.shouldEqualTo(99)
            }
        }

    }
})


fun testModules(): List<LocalTestModule> {
    return listOf(LocalTestModule())
}


class LocalTestModule : AbstractModule() {

    val messageBus: MessageBus = mock()

    override fun configure() {
        bind(SerializationSupport::class.java).to(DefaultSerializationSupport::class.java)
        bind(Translate::class.java).toInstance(MockTranslate())
        bind(InjectorLocator::class.java).toInstance(TestInjectorLocator())
        bind(MessageBus::class.java).toInstance(messageBus)
    }

}