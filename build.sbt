import SonatypeKeys._

sonatypeSettings

name := "facebook4s"

version := "0.1.5"

scalaVersion := "2.12.1"

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

organization := "com.github.vooolll"

organizationHomepage := Some(url("https://github.com/vooolll"))

description := "Async Scala SDK for the Facebook Graph API."

autoAPIMappings := true
apiMappings ++= {
  val classpath = (fullClasspath in Compile).value
  def findJar(name: String): File = {
    val regex = ("/" + name + "[^/]*.jar$").r
    classpath.find { jar => regex.findFirstIn(jar.data.toString).nonEmpty }.get.data // fail hard if not found
  }


  Map(
    findJar("scala-library") -> url("http://scala-lang.org/api/" + "2.12.1" + "/"),
    findJar("config") -> url("https://typesafehub.github.io/config/latest/api/")
  )
}

pomExtra :=
  <url>https://github.com/vooolll/facebook4s</url>
  <scm>
    <url>https://github.com/vooolll/facebook4s</url>
    <connection>scm:git:git@github.com:vooolll/facebook4s.git</connection>
  </scm>
  <developers>
    <developer>
      <id>slow_harry</id>
      <name>Valeryi Baibossynov</name>
      <url>https://github.com/vooolll</url>
    </developer>
  </developers>

libraryDependencies ++= {
  val akkaV = "2.4.19"
  val playV = "2.6.3"
  val akkaHttpV = "10.0.9"
  val scalaTestV = "3.0.1"
  val typesafeV = "1.3.1"
  val mockitoV = "1.8.5"
  val catsV = "1.0.0-MF"
  val scalaLogging = "3.7.2"
  val akkaPlayJsonSupport = "1.18.0"
  val logbackClassic = "1.2.3"
  val uriBuilder = "0.9.0"

  Seq(
    "org.typelevel"                  %% "cats-core"           % catsV,
    "com.typesafe.akka"              %% "akka-actor"          % akkaV,
    "com.typesafe.akka"              %% "akka-http"           % akkaHttpV,
    "com.typesafe.play"              %% "play-json"           % playV,
    "com.typesafe.scala-logging"     %% "scala-logging"       % scalaLogging,
    "de.heikoseeberger"              %% "akka-http-play-json" % akkaPlayJsonSupport,
    "org.f100ded.scala-url-builder"  %% "scala-url-builder"   % uriBuilder,
    "org.scalatest"                  %% "scalatest"           % scalaTestV % "test",

    "ch.qos.logback"                 %  "logback-classic"      % logbackClassic,
    "org.mockito"                    %  "mockito-core"         % mockitoV % "test",
    "com.typesafe"                   %  "config"               % typesafeV
  )
}

licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-Xlint",
  "-deprecation",
  "-Xfatal-warnings",
  "-feature",
  "-language:postfixOps",
  "-unchecked"
)

fork := true

javaOptions in Test ++= Seq(sys.env.getOrElse("TRAVIS_OPTION", "-Dconfig.file=src/test/resources/.facebook-dev.conf"))

scalacOptions in ThisBuild ++= Seq("-language:postfixOps",
  "-language:implicitConversions",
  "-language:existentials",
  "-feature",
  "-deprecation")
