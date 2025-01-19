package ru.glebik.updater.library.init

import ru.glebik.updater.library.checker.CheckerParameters
import java.util.concurrent.TimeUnit

/**
 * Configuration class for managing auto-update parameters.
 * Includes options for periodic or one-time update checks.
 */
class UpdateConfig private constructor(
    val checkerParameters: CheckerParameters,
    val isPeriodic: Boolean,
    val repeatInterval: Long,
    val timeUnit: TimeUnit
) {
    /**
     * Interface for the base builder state.
     * Allows setting input data and choosing between one-time or periodic execution modes.
     */
    interface BaseBuilder {
        fun setCheckerParameters(params: CheckerParameters): BaseBuilder
        fun setOneTime(): FinalBuilder
        fun setPeriodic(): PeriodicBuilder
    }

    /**
     * Interface for the periodic builder state.
     * Allows configuring the interval for periodic execution.
     */
    interface PeriodicBuilder {
        fun setInterval(interval: Long, unit: TimeUnit): FinalBuilder
    }

    /**
     * Interface for the final builder state.
     * Allows building the final UpdateConfig instance.
     */
    interface FinalBuilder {
        fun build(): UpdateConfig
    }

    /**
     * Builder implementation for constructing UpdateConfig instances.
     */
    class Builder private constructor() : BaseBuilder, PeriodicBuilder, FinalBuilder {
        private var checkerParameters: CheckerParameters? = null
        private var isPeriodic: Boolean = false
        private var repeatInterval: Long = 24 // Default interval in hours
        private var timeUnit: TimeUnit = TimeUnit.HOURS

        companion object {
            /**
             * Creates a new instance of the builder.
             *
             * @return The initial state of the builder.
             */
            fun builder(): BaseBuilder = Builder()
        }

        override fun setCheckerParameters(params: CheckerParameters): BaseBuilder {
            this.checkerParameters = params
            return this
        }

        override fun setOneTime(): FinalBuilder {
            this.isPeriodic = false
            return this
        }

        override fun setPeriodic(): PeriodicBuilder {
            this.isPeriodic = true
            return this
        }

        override fun setInterval(interval: Long, unit: TimeUnit): FinalBuilder {
            this.repeatInterval = interval
            this.timeUnit = unit
            return this
        }

        override fun build(): UpdateConfig {
            requireNotNull(checkerParameters) { "CheckerParameters must be set using setInputData()" }
            return UpdateConfig(
                checkerParameters = checkerParameters!!,
                isPeriodic = isPeriodic,
                repeatInterval = repeatInterval,
                timeUnit = timeUnit
            )
        }
    }
}