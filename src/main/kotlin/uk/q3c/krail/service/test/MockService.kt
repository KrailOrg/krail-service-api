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

package uk.q3c.krail.service.test

import com.google.inject.Inject
import uk.q3c.krail.eventbus.MessageBus
import uk.q3c.krail.i18n.Translate
import uk.q3c.krail.service.AbstractService
import uk.q3c.krail.service.Cause
import uk.q3c.krail.service.State
import uk.q3c.util.guice.SerializationSupport

/**
 * Created by David Sowerby on 01/11/15.
 */
open class MockService @Inject constructor(translate: Translate, messageBus: MessageBus, serializationSupport: SerializationSupport) : AbstractService(translate, messageBus, serializationSupport) {

    var callsToStart = 0
    var callsToStop = 0
    var failToStart: Boolean = false
    var failToStop: Boolean = false
    var startDelay: Int = 0
    var stopDelay: Int = 0
    val statusHistory: MutableList<State> = mutableListOf()
    var failToReset = false

    init {
        statusHistory.add(State.INITIAL)
    }


    override fun doStart() {
        println("starting $nameKey")
        callsToStart++

        if (failToStart) {
            throw RuntimeException("Mocked exception during a call to doStart()")
        }

        try {
            Thread.sleep(startDelay.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }


    }


    override fun doStop() {
        println("stopping $nameKey")
        callsToStop++
        if (failToStop) {
            throw RuntimeException("Mocked exception during a call to doStop()")
        }
        try {
            Thread.sleep(stopDelay.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    override fun publishStatusChange(previousState: State, cause: Cause) {
        super.publishStatusChange(previousState, cause)
        statusHistory.add(state)
    }

    override fun doReset() {
        if (failToReset) {
            throw RuntimeException("Mocked exception during a call to doReset()")
        }
    }
}
