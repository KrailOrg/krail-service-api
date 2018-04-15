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

import uk.q3c.krail.eventbus.BusMessage
import uk.q3c.krail.i18n.I18NKey
import uk.q3c.krail.i18n.NamedAndDescribed
import java.io.Serializable
import javax.annotation.concurrent.Immutable

/**
 * Implement this interface to provide a Service. A Service is typically something which is wired up using Guice
 * modules, but requires logic to get fully up and running, or consumes external resources - database connections, web
 * service etc, and is based on the recommendations of the Guice team. (see
 * https://code.google.com/p/google-guice/wiki/ModulesShouldBeFastAndSideEffectFree).
 *
 *
 * A [Service] can however be used for anything you feel appropriate, which could benefit from having a two stage
 * creation cycle - the initial configuration through Guice modules, followed by a controlled start to activate /
 * consume resources.
 *
 *
 * The easiest way is to create an implementation is to sub-class  [AbstractService].<br></br>
 *
 *
 * Implementations (even sub-classes of [AbstractService] must define a key which when combined with [instanceNumber], provides a unique
 * identity for this Service.  It is an I18NKey because it is expected that this name will be presented to end users (even if only to application sys
 * admins)
 *
 *
 * An instance of [ServiceMonitor] may be present, listening on the [MessageBus] for changes to service status
 *
 * The AOP code in the [ServicesModule] intercepts the finalize() method, and calls the stop() method to
 * ensure a service is stopped before being finalized.
 *
 *
 * A service should have the following characteristics:
 *
 *  1. All Services must be instantiated through Guice
 *  1. The constructor must be lightweight and must not require that its dependencies are already started at the time
 * of injection.
 *  1. If the dependency's constructor is lightweight as it should be, it should also be unnecessary to inject a Provider<Service></Service> *
 * Details of the lifecycle can be found at https://davidsowerby.gitbooks.io/krail-user-guide/content/devguide/devguide-services.html
 *
 * @author David Sowerby
 */
interface Service : NamedAndDescribed, Serializable {

    /**
     * returns the State value for this service instance
     *
     * @return the State value for this service instance
     */
    val state: State

    /**
     * Returns the cause of the last state change
     *
     * @return Returns the cause of the last state change
     */
    val cause: Cause

    /**
     * Returns true if and only if status == Service.Status.STARTED)
     *
     * @return true if and only if status == Service.Status.STARTED)
     */
    val isStarted: Boolean

    /**
     * Returns true if the service is in a stopped state
     *
     * @return true if the service is in a stopped state
     */
    val isStopped: Boolean


    val serviceKey: ServiceKey
        get() = ServiceKey(nameKey)

    /**
     * Not used by default, but can be used to identify a specific instance of a [Service].  Returns 0 by default
     */
    var instanceNumber: Int


    /**
     * You will only need to implement this if you are not using a sub-class of
     * [AbstractService]. When you do sub-class [AbstractService], override [AbstractService.doStart]. Exceptions should be caught and
     * handled within the implementation of this method - generally this will set the cause to [Cause.FAILED_TO_START]
     *
     * @throws ServiceStatusException if called when service is currently in a state which does not allow a start
     */
    fun start(): ServiceStatus

    /**
     * Equivalent to calling [stop] with a value of [State.STOPPED].  If successful, end state is [State.STOPPED]
     * Failure during the stop process set the state to [Cause.FAILED_TO_STOP]
     */
    fun stop(): ServiceStatus

    /**
     * Used when a failure is identified external to the service itself.
     *
     * Attempts to stop the Service, and sets the state to [Cause.FAILED].
     * If a stop does not complete correctly, cause is set to to [Cause.FAILED_TO_STOP]
     *
     * @return state after the call
     */
    fun fail(): ServiceStatus

    /**
     * Resets a service from a stopped or failed state to INITIAL.  Does nothing if the service is STARTED, STARTING or STOPPING
     *
     * @return a ServiceStatus of INITIAL
     */
    fun reset(): ServiceStatus


}


class ServiceException(message: String) : RuntimeException(message)
class ServiceConfigurationException(message: String) : RuntimeException(message)
class ServiceBusMessage(val service: Service, val fromState: State, val toState: State, val cause: Cause) : BusMessage

/**
 * A key object to uniquely identify a [Service] class without needing to use the Class itself
 *
 *
 * Created by David Sowerby on 24/10/15.
 */
@Immutable
data class ServiceKey(private val key: I18NKey) : Serializable {

    override fun toString(): String {
        return (key as Enum<*>).name
    }
}

class ServiceKeyException(message: String) : RuntimeException(message)