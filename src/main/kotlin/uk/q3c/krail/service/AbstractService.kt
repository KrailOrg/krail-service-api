/*
 *
 *  * Copyright (c) 2016. David Sowerby
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 *  * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 *  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations under the License.
 *
 */
package uk.q3c.krail.service

import com.google.inject.Inject
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.slf4j.LoggerFactory
import uk.q3c.krail.eventbus.GlobalMessageBus
import uk.q3c.krail.eventbus.MessageBus
import uk.q3c.krail.i18n.DefaultLabelKey
import uk.q3c.krail.i18n.I18NKey
import uk.q3c.krail.i18n.Translate
import uk.q3c.krail.service.State.*
import uk.q3c.util.guice.SerializationSupport
import java.io.IOException
import java.io.ObjectInputStream
import javax.annotation.concurrent.ThreadSafe


/**
 * The easiest way to provide a [Service] is to sub-class either this class or [AbstractService].
 *
 *
 * A [ServiceBusMessage] is broadcast at every state change via [messageBus]. The [ServiceMonitor] subscribes to those messages.
 *
 *
 *
 *
 * For the MBassador event bus implementation, it is not necessary to annotate a sub-class of AbstractService with a @Listener, unless you want to subscribe to another event bus as well as the default [GlobalMessageBus]
 *
 *
 *
 * @author David Sowerby
 */

@ThreadSafe
abstract class AbstractService @Inject protected constructor(
        @field:Transient val translate: Translate,
        @field:Transient val messageBus: MessageBus,
        val serializationSupport: SerializationSupport)

    : Service {


    @Transient
    private var log = LoggerFactory.getLogger(AbstractService::class.java)
    private val lock = arrayOfNulls<Any>(0)

    override var state = INITIAL
        get() {
            synchronized(lock) {
                return field
            }
        }
    private var descriptionKey: I18NKey = DefaultLabelKey.not_specified
    private var nameKey: I18NKey = DefaultLabelKey.not_specified
    override var instanceNumber = 0
        get() {
            synchronized(lock) {
                return field
            }
        }
        set(value) {
            synchronized(lock) {
                field = value
            }
        }
    override var cause: Cause = Cause.not_specified
        get() {
            synchronized(lock) {
                return field
            }
        }

    override val isStarted: Boolean
        get() = synchronized(lock) {
            return state === RUNNING
        }

    override val isStopped: Boolean
        get() = synchronized(lock) {
            return state === STOPPED
        }

    init {
        messageBus.subscribe(this)
    }

    override fun getNameKey(): I18NKey {
        return nameKey
    }

    override fun setNameKey(nameKey: I18NKey) {
        this.nameKey = nameKey
    }


    override fun stop(): ServiceStatus {
        return stop(Cause.STOPPED)
    }


    private fun stop(cause: Cause): ServiceStatus {
        synchronized(lock) {
            if (state === STOPPED || state === STOPPING || state === FAILED || state === RESETTING) {
                log.debug("Attempting to stop service {}, but it is already stopped or resetting. No action taken", name)
                return ServiceStatus(this, this.state, this.cause)
            }
            if (state === INITIAL) {
                log.debug("Currently in INITIAL state, stop or fail ignored")
                return ServiceStatus(this, this.state, this.cause)
            }
            log.info("Stopping service: {}", name)
            setState(STOPPING, cause)
            //boolean dependantsRequiringThisAreStopped
            // always require this service
            try {
                doStop()
                setState(stopStateFromCause(cause), cause)
            } catch (e: Exception) {
                log.error("Exception occurred while trying to stop {}.", name)
                if (cause === Cause.FAILED) {
                    //service has already failed, not just failed to stop
                    setState(stopStateFromCause(Cause.FAILED), Cause.FAILED)
                } else {
                    setState(stopStateFromCause(Cause.FAILED_TO_STOP), Cause.FAILED_TO_STOP)
                }
            }

            return ServiceStatus(this, this.state, this.cause)
        }
    }

    private fun stopStateFromCause(cause: Cause): State {
        return if (cause === Cause.FAILED || cause === Cause.FAILED_TO_STOP) {
            FAILED
        } else STOPPED
    }

    @Throws(Exception::class)
    protected abstract fun doStop()

    override fun getName(): String {
        synchronized(lock) {
            return translate.from(getNameKey())
        }
    }

    override fun fail(): ServiceStatus {
        return stop(Cause.FAILED)
    }

    @Synchronized
    override fun reset(): ServiceStatus {
        synchronized(lock) {
            if (state === INITIAL || state === RESETTING) {
                return ServiceStatus(this, this.state, this.cause)
            }
            if (state !== STOPPED && state !== FAILED) {
                throw ServiceStatusException("Must be in a STOPPED state before reset()")
            }
            log.info("Resetting service: {}", name)
            setState(State.RESETTING, Cause.RESET)
            try {
                doReset()
                setState(INITIAL, Cause.RESET)
                return ServiceStatus(this, this.state, this.cause)
            } catch (e: Exception) {
                log.error("Exception while trying to reset {}", name, e)
                setState(State.FAILED, Cause.FAILED_TO_RESET)
                return ServiceStatus(this, this.state, this.cause)
            }

        }
    }

    /**
     * Often not needed to do anything - but override if it does
     */
    @SuppressFBWarnings("ACEM_ABSTRACT_CLASS_EMPTY_METHODS")
    protected open fun doReset() {

    }

    private fun start(cause: Cause): ServiceStatus {
        synchronized(lock) {
            if (state === RUNNING || state === STARTING) {
                log.debug("{} already started, no action taken", name)
                return ServiceStatus(this, this.state, this.cause)
            }
            if (state === STOPPING) {
                throw ServiceStatusException("Cannot start() when state is " + state.name)
            }
            if (state === FAILED) {
                throw ServiceStatusException("Cannot start() when state is " + state.name + ".  Call reset() first")
            }

            log.info("Starting service: {}", name)
            setState(STARTING, cause)
            try {
                doStart()
                setState(RUNNING, Cause.STARTED)
            } catch (e: Exception) {
                log.error("Exception occurred while trying to start service $name", e)
                setState(FAILED, Cause.FAILED_TO_START)
            }
            return ServiceStatus(this, this.state, this.cause)
        }
    }

    override fun start(): ServiceStatus {
        return start(cause = Cause.STARTED)
    }

    @Throws(Exception::class)
    protected abstract fun doStart()


    private fun setState(state: State, cause: Cause) {
        synchronized(lock) {
            if (state !== this.state) {
                val previousState = this.state
                this.state = state
                this.cause = cause
                log.debug("'$name' Service has changed status from $previousState to $state")
                publishStatusChange(previousState, cause)
            }
        }
    }


    override fun getDescriptionKey(): I18NKey {
        synchronized(lock) {
            return descriptionKey
        }
    }

    override fun setDescriptionKey(descriptionKey: I18NKey) {
        synchronized(lock) {
            this.descriptionKey = descriptionKey
        }
    }

    override fun getDescription(): String {
        synchronized(lock) {
            return translate.from(descriptionKey)
        }
    }


    protected open fun publishStatusChange(previousState: State, cause: Cause) {
        log.debug("publishing ServiceBusMessage, state changed from $previousState to $state")
        messageBus.publishSync(ServiceBusMessage(this, previousState, state, cause))
    }

    /**
     * A last resort, should never be needed - environment should ensure that all services are stopped on close down
     */
    protected fun finalize() {
        stop()
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        beforeDeserialization()
        inputStream.defaultReadObject()
        beforeTransientInjection()
        serializationSupport.injectTransientFields(this)
        afterTransientInjection()
        serializationSupport.checkForNullTransients()
        messageBus.subscribe(this)
    }


    /**
     * By default does nothing but can be overridden to execute code before any other action is taken for deserialization.
     * It cannot be used to set exclusions for [serializationSupport], as [serializationSupport] will be deserialized
     * after this call
     */
    protected open fun beforeDeserialization() {

    }

    /**
     * Override (but call super) to populate fields before [serializationSupport] injects Guice dependencies.
     * It could be used to set exclusions for [serializationSupport]
     */
    protected open fun beforeTransientInjection() {
        log = LoggerFactory.getLogger(AbstractService::class.java)
    }


    /**
     * By default does nothing but can be overridden to populate transient fields after [serializationSupport]
     * has injected Guice dependencies.
     */
    protected open fun afterTransientInjection() {

    }
}
