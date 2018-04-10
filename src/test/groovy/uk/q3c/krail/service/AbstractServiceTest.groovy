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
import spock.lang.Specification
import uk.q3c.krail.eventbus.MessageBus
import uk.q3c.krail.i18n.I18NKey
import uk.q3c.krail.i18n.Translate
import uk.q3c.util.guice.SerializationSupport
/**
 * Created by David Sowerby on 08/11/15.
 *
 */
//@UseModules([])
class AbstractServiceTest extends Specification {

    def translate = Mock(Translate)
    SerializationSupport serializationSupport = Mock(SerializationSupport)

    TestService service

    MessageBus messageBus = Mock(MessageBus)

    def setup() {

        service = new TestService(translate, messageBus, servicesExecutor, serializationSupport)
        service.setThrowStartException(false)
        service.setThrowStopException(false)

    }

//    def "name translated"() {
//        given:
//        translate.from(LabelKey.Authorisation) >> "Authorisation"
//        expect:
//        service.getNameKey().equals(LabelKey.Authorisation)
//        service.getName().equals("Authorisation")
//    }
//
//
//    def "description key translated"() {
//        given:
//        translate.from(LabelKey.Authorisation) >> "Authorisation"
//        service.setDescriptionKey(LabelKey.Authorisation)
//
//        expect:
//        service.getDescriptionKey().equals(LabelKey.Authorisation)
//        service.getDescription().equals("Authorisation")
//    }
//
//
//    def "setDescriptionKey null is accepted"() {
//        when:
//        service.setDescriptionKey(null)
//
//        then:
//
//        service.getDescriptionKey().equals(null)
//    }
//
//    def "missing description key returns empty String"() {
//
//        expect:
//        service.getDescription().equals("")
//    }
//
//    def "start"(State initialState,  boolean serviceFail, boolean allDepsOk, Cause callWithCause, State transientState, State finalState, Cause finalCause) {
//
//        given:
//
//        service.state = initialState
//        service.throwStartException serviceFail
//
//        when:
//
//        ServiceStatus status = service.start(callWithCause)
//
//        then:
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == transientState && m.cause == callWithCause })
//        1 * servicesExecutor.execute(action, callWithCause) >> allDepsOk
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == finalState && m.cause == finalCause })
//        service.getState() == finalState
//        service.getCause() == finalCause
//        status.state == finalState
//        status.cause == finalCause
//        status.service == service
//
//
//        where:
//        initialState  |  serviceFail | allDepsOk | callWithCause | transientState | finalState   | finalCause
////        State.INITIAL  | false       | true      | Cause.STARTED | State.STARTING | State.RUNNING | Cause.STARTED
////        State.STOPPED  | false       | true      | Cause.STARTED | State.STARTING | State.RUNNING | Cause.STARTED
//        State.STOPPED |  true        | true      | Cause.STARTED | State.STARTING | State.FAILED | Cause.FAILED_TO_START
////        State.INITIAL  false       | false     | Cause.STARTED | State.STARTING | State.INITIAL | Cause.DEPENDENCY_FAILED
////        State.STOPPED  false       | false     | Cause.STARTED | State.STARTING | State.STOPPED | Cause.DEPENDENCY_FAILED
//
//    }
//
//    def "stop"(State initialState,  boolean serviceFail, boolean allDepsOk, Cause callWithCause, State transientState, State finalState, Cause finalCause) {
//
//        given:
//
//        service.state = initialState
//        service.throwStopException serviceFail
//
//        when:
//
//        ServiceStatus status = service.stop(callWithCause)
//
//        then:
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == transientState && m.cause == callWithCause })
//        1 * servicesExecutor.execute(action, callWithCause) >> true
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == finalState && m.cause == finalCause })
//        service.getState() == finalState
//        service.getCause() == finalCause
//        status.state == finalState
//        status.cause == finalCause
//        status.service == service
//
//
//        where:
//        initialState  |  serviceFail | allDepsOk | callWithCause            | transientState | finalState    | finalCause
//        State.RUNNING |  false       | true      | Cause.STOPPED            | State.STOPPING | State.STOPPED | Cause.STOPPED
//        State.RUNNING |  false       | true      | Cause.FAILED             | State.STOPPING | State.FAILED  | Cause.FAILED
//        State.RUNNING |  true        | true      | Cause.STOPPED            | State.STOPPING | State.FAILED  | Cause.FAILED_TO_STOP
//        State.RUNNING |  true        | true      | Cause.FAILED             | State.STOPPING | State.FAILED  | Cause.FAILED
//        State.RUNNING |  true        | true      | Cause.DEPENDENCY_STOPPED | State.STOPPING | State.FAILED  | Cause.FAILED_TO_STOP
//        State.RUNNING |  true        | true      | Cause.DEPENDENCY_FAILED  | State.STOPPING | State.FAILED  | Cause.FAILED_TO_STOP
//    }
//
//
//    def "ignored start calls"(State initialState) {
//
//        given:
//
//        service.state = initialState
//        Cause initialCause = service.getCause()
//
//        when:
//
//        ServiceStatus status = service.start()
//
//        then:
//        service.getState() == initialState
//        service.getCause() == initialCause
//        status.state == initialState
//        status.cause == initialCause
//        status.service == service
//
//
//        where:
//        initialState   | action
//        State.STARTING | -
//        State.RUNNING  | -
//    }
//
//    def "disallowed start calls throw exception"(State initialState) {
//
//        given:
//
//        service.state = initialState
//        Cause initialCause = service.getCause()
//
//        when:
//
//        ServiceStatus status = service.start()
//
//        then:
//        thrown(ServiceStatusException)
//
//
//        where:
//        initialState   | action
//        State.STOPPING | -
//        State.FAILED   | -
//    }
//
//
//    def "ignored stop calls"(State initialState,  Cause callWithCause) {
//
//        given:
//
//        service.state = initialState
//        Cause initialCause = service.getCause()
//
//        when:
//
//        ServiceStatus status = service.stop(callWithCause)
//
//        then:
//        service.getState() == initialState
//        service.getCause() == initialCause
//        status.state == initialState
//        status.cause == initialCause
//        status.service == service
//
//
//        where:
//        initialState    | action      | callWithCause
//        State.STOPPED   | Action.STOP | Cause.STOPPED
//        State.STOPPED   | Action.STOP | Cause.FAILED
//        State.FAILED    | Action.STOP | Cause.STOPPED
//        State.FAILED    | Action.STOP | Cause.FAILED
//        State.STOPPING  | Action.STOP | Cause.STOPPED
//        State.STOPPING  | Action.STOP | Cause.FAILED
//        State.RESETTING | Action.STOP | Cause.STOPPED
//        State.RESETTING | Action.STOP | Cause.FAILED
//    }
//
//    def "disallowed stop calls throw exception"() {
//        //there are none
//        expect: true
//    }
//
//
//    def "reset"(State initialState, boolean serviceFail, State transientState, State finalState, Cause finalCause) {
//
//        given:
//
//        service.state = initialState
//        service.throwResetException serviceFail
//
//        when:
//
//        ServiceStatus status = service.reset()
//
//        then:
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == transientState && m.cause == Cause.RESET })
//        1 * messageBus.publishSync({ ServiceBusMessage m -> m.toState == finalState && m.cause == finalCause })
//        service.getState() == finalState
//        service.getCause() == finalCause
//        status.state == finalState
//        status.cause == finalCause
//        status.service == service
//
//
//        where:
//        initialState  | action      | serviceFail | transientState  | finalState    | finalCause
//        State.STOPPED | Action.STOP | false       | State.RESETTING | State.INITIAL | Cause.RESET
//        State.FAILED  | Action.STOP | false       | State.RESETTING | State.INITIAL | Cause.RESET
//        State.STOPPED | Action.STOP | true        | State.RESETTING | State.FAILED  | Cause.FAILED_TO_RESET
//        State.FAILED  | Action.STOP | true        | State.RESETTING | State.FAILED  | Cause.FAILED_TO_RESET
//
//    }
//
//    def "ignored reset calls"(State initialState) {
//
//        given:
//
//        service.state = initialState
//        Cause initialCause = service.getCause()
//
//        when:
//
//        ServiceStatus status = service.reset()
//
//        then:
//        service.getState() == initialState
//        service.getCause() == initialCause
//        status.state == initialState
//        status.cause == initialCause
//        status.service == service
//
//
//        where:
//        initialState    | _
//        State.INITIAL   | _
//        State.RESETTING | _
//    }
//
//    def "disallowed reset calls throw exception"(State initialState) {
//
//        given:
//
//        service.state = initialState
//        Cause initialCause = service.getCause()
//
//        when:
//
//        ServiceStatus status = service.reset()
//
//        then:
//        thrown(ServiceStatusException)
//
//
//        where:
//        initialState   | _
//        State.STOPPING | _
//        State.RUNNING  | _
//        State.STARTING | _
//    }
//
//    def "fail short call"() {
//        given:
//
//        service.state = State.RUNNING
//
//        when:
//
//        service.fail()
//
//        then:
//
//        1 * servicesExecutor.execute(Action.STOP, Cause.FAILED) >> true
//    }
//
//    def "dependencyFail"() {
//
//        given:
//
//        service.state = State.RUNNING
//
//        when:
//
//        service.dependencyFail()
//
//        then:
//
//        1 * servicesExecutor.execute(Action.STOP, Cause.DEPENDENCY_FAILED) >> true
//    }
//
//    def "dependencyStop"() {
//
//        given:
//
//        service.state = State.RUNNING
//
//        when:
//
//        service.dependencyStop()
//
//        then:
//
//        1 * servicesExecutor.execute(Action.STOP, Cause.DEPENDENCY_STOPPED) >> true
//
//    }
//
//    def "start short call"() {
//
//
//        when:
//
//        service.start()
//
//        then:
//
//        1 * servicesExecutor.execute(Action.START, Cause.STARTED) >> true
//
//    }
//
//    def "stop"() {
//
//        given:
//
//        service.state = State.RUNNING
//
//        when:
//
//        service.stop()
//
//        then:
//
//        1 * servicesExecutor.execute(Action.STOP, Cause.STOPPED) >> true
//
//    }
//
//
    static class TestService extends AbstractService implements Service {

        boolean throwStartException = false
        boolean throwStopException = false
        boolean throwResetException = false

        @Inject
        protected TestService(Translate translate, MessageBus messageBus, SerializationSupport serializationSupport) {
            super(translate, messageBus, serializationSupport)
        }

        @Override
        void doStart() {
            if (throwStartException) {
                throw new RuntimeException("Test Exception thrown")
            }
        }

        @Override
        void doStop() {
            if (throwStopException) {
                throw new RuntimeException("Test Exception thrown")
            }
        }

        @Override
        void doReset() {
            if (throwResetException) {
                throw new RuntimeException("Test Exception thrown")
            }
        }

        @Override
        I18NKey getNameKey() {
            return LabelKey.Authorisation
        }

        void throwStartException(boolean throwStartException) {
            this.throwStartException = throwStartException
        }

        void throwStopException(boolean throwStopException) {
            this.throwStopException = throwStopException
        }

        void throwResetException(boolean throwResetException) {
            this.throwResetException = throwResetException
        }
    }


}