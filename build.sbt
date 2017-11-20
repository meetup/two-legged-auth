enablePlugins(CommonSettingsPlugin)
enablePlugins(CoverallsWrapper)

scalaVersion := "2.11.8"
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= {

  Seq(
    "io.reactivex" % "rxnetty" % "0.4.20",
    "com.netflix.hystrix" % "hystrix-rx-netty-metrics-stream" % "1.4.23"
      exclude("io.reactivex", "rxnetty")
      exclude("com.netflix.archaius", "archaius-core"), // TODO make sure this is safe
    "com.meetup" %% "scala-logger" % "0.2.22",
    "org.asynchttpclient" % "async-http-client" % "2.0.37",
    "me.lessis" %% "base64" % "0.2.0",
    "me.lessis" %% "prints" % "0.1.0",

    // Looks like there is an issue causing deadlocks which is introduced in 2.6.7+ but not fixed yet.
    // See https://github.com/mockito/mockito/issues/1067
    "org.mockito" % "mockito-core" % "2.6.6" % "test"
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

bintrayOrganization in ThisBuild := Some("meetup")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
