package uk.q3c.krail.service

import org.amshove.kluent.shouldBeEqualTo
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import uk.q3c.krail.i18n.I18NKey


/**
 * Created by David Sowerby on 11 Apr 2018
 */

object ServiceKeyTest : Spek({
    given("a service key") {

        on("creating a key") {
            val key = ServiceKey(LabelKey.Authorisation)
            it(" returns key.name from toString()") {
                key.toString().shouldBeEqualTo("Authorisation")
            }
        }
    }

})


enum class LabelKey : I18NKey {

    Yes, No, Authorisation
}
