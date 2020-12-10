package io.nais.yaml

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import java.io.StringWriter

val mustache: Mustache = DefaultMustacheFactory().compile("naisyaml/nais.yaml.mustache")

data class NaisApp(
   val name: String,
   val team: String,
   val image: String
)

fun createNaisYaml(app: NaisApp) = StringWriter().use {
      mustache.execute(it, app).flush()
      it.toString()
}
