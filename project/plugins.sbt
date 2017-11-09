resolvers +=  Resolver.url(
  "meetup-sbt-plugins",
  new java.net.URL("https://dl.bintray.com/meetup/sbt-plugins/")
)(Resolver.ivyStylePatterns)

addSbtPlugin("com.meetup" % "sbt-plugins" % "0.3.33")

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.1.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
