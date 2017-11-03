scalaVersion := "2.11.8"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
resolvers += "Nexus" at "https://nexus.blt.meetup.com/content/repositories/releases"

libraryDependencies ++= {

  Seq(
    "io.reactivex" % "rxnetty" % "0.4.20",
    "org.json4s" %% "json4s-native" % "3.4.0",
    "com.netflix.hystrix" % "hystrix-rx-netty-metrics-stream" % "1.4.23"
      exclude("io.reactivex", "rxnetty")
      exclude("com.netflix.archaius", "archaius-core"), // TODO make sure this is safe
    "com.meetup" %% "scala-logger" % "10.0.0",
    "org.asynchttpclient" % "async-http-client" % "2.0.26",
    "me.lessis" %% "base64" % "0.2.0",

    // test
    "org.scalatest" %% "scalatest" % "3.0.0" % "test",
    "org.mockito" % "mockito-core" % "2.7.9" % "test"
  )
}

dependencyOverrides += "io.reactivex" % "rxnetty" % "0.4.20"

scalacOptions ++= Seq(
  "-language:reflectiveCalls"   // necessary until we implement "vampire methods" to eliminate this warning
)

parallelExecution in Test := false

name := "two-legged-auth"

organization := "com.meetup"

resolvers += Resolver.bintrayRepo("meetup", "maven")

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

        