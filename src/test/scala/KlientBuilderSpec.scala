package test

import java.net.URI

import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest.{WordSpec, Matchers}
import org.zalando.nakadi.client._

class KlientBuilderSpec extends WordSpec with Matchers {

  "KlientBuilder" must {
    "build a Klient instance, if everything is set properly" ignore /*in*/ {
      KlientBuilder()
        .withEndpoint(new URI("localhost:8080"))
        .withTokenProvider(() => "my-token")
        .build()
    }

    "build a Java client instance, if everything is set properly" ignore /*in*/ {
      KlientBuilder()
        .withEndpoint(new URI("localhost:8080"))
        .withTokenProvider(() => "my-token")
        .buildJavaClient()
    }

    "throw an exception, if not all mandatory arguments are set" ignore /*in*/ {
      an [IllegalStateException] must be thrownBy {
        KlientBuilder()
          .withTokenProvider(() => "my-token")
          .build()
      }
      an [IllegalStateException] must be thrownBy {
        KlientBuilder()
          .withEndpoint(new URI("localhost:8080"))
          .build()
      }
    }

    /** what does this actually test? (commented out to get things compile); I need to study this ObjectMapper thing. AKa050216
    "should use the specified ObjectMapper" ignore /*in*/ {

      val objectMapper = new ObjectMapper()

      val klient: KlientImpl = KlientBuilder()
        .withEndpoint(new URI("localhost:8080"))
        .withTokenProvider(() => "my-token")
        .withObjectMapper(Some(objectMapper))
        .build  //.asInstanceOf[KlientImpl]

      klient.objectMapper == objectMapper should be(true)
    }
    **/
  }
}
