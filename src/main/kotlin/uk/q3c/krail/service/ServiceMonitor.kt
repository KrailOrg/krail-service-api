/*
 * Copyright (c) 2015. David Sowerby
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package uk.q3c.krail.service

import com.google.common.collect.ImmutableList

/**
 *
 * Monitors instances of [Service] implementations, and keeps a history of the most recent status changes (only
 * the current status and the most recent change, see [ServiceStatusRecord]).
 *
 *
 * Services are registered automatically the first time a state change message is received
 *
 *
 * Acknowledgement: developed from code contributed by https://github.com/lelmarir
 */

interface ServiceMonitor {
    val monitoredServices: ImmutableList<Service>

    /**
     * Returns most recent status record for [service], or an empty [ServiceStatusRecord] if no record has been made
     */
    fun getServiceStatus(service: Service): ServiceStatusRecord

    /**
     * The monitor is usually the only place where there is a record of all services, so it offers the facility to stop
     * all running services, usually as part of closedown
     */
    fun stopAllServices()
}

