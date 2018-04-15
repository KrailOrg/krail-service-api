package uk.q3c.krail.service

import java.time.LocalDateTime
import javax.annotation.concurrent.Immutable

/**
 * Created by David Sowerby on 10 Apr 2018
 */
enum class State {
    INITIAL, STARTING, RUNNING, STOPPING, STOPPED, RESETTING, FAILED, not_specified
}

enum class Cause {
    FAILED, STOPPED, FAILED_TO_START, FAILED_TO_STOP, STARTED, FAILED_TO_RESET, RESET, not_specified
}

class ServiceStatusException(message: String) : RuntimeException(message)
class ServiceCauseException(message: String) : RuntimeException(message)

@Immutable
data class ServiceStatus(val service: Service, val state: State, val cause: Cause)

@Immutable
data class ServiceStatusRecord(
        val currentState: State,
        val lastStartTime: LocalDateTime,
        val lastStopTime: LocalDateTime,
        val previousState: State,
        val service: Service,
        val statusChangeTime: LocalDateTime,
        val empty: Boolean = false
) {
    constructor(service: Service) : this(
            service = service,
            empty = true,
            currentState = State.not_specified,
            lastStartTime = LocalDateTime.MIN,
            lastStopTime = LocalDateTime.MIN,
            previousState = State.not_specified,
            statusChangeTime = LocalDateTime.MIN)
}