package uk.q3c.krail.service

import com.google.inject.Injector
import uk.q3c.util.guice.InjectorLocator

/**
 * Created by David Sowerby on 13 Apr 2018
 */
object InjectorHolder {

    var injector: Injector? = null

    fun hasInjector(): Boolean {
        return injector != null
    }
}


class TestInjectorLocator : InjectorLocator {

    override fun get(): Injector {
        return InjectorHolder.injector!!
    }

    override fun put(injector: Injector) {
        InjectorHolder.injector = injector
    }

}