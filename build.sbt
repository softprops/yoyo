organization := "me.lessis"

name := "yoyo"

version := "0.1.0-SNAPSHOT"

crossScalaVersions ++= Seq("2.10.4", "2.11.5")

scalaVersion := crossScalaVersions.value.last

homepage := Some(url(s"http://github.com/softprops/${name.value}#readme"))

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature").filter(
    Function.const(scalaVersion.value.startsWith("2.11")))

initialCommands := "import scala.concurrent.ExecutionContext.Implicits.global;"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-json4s-native" % "0.11.1"

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](version)

buildInfoPackage := "yoyo"

pomExtra  := (
  <scm>
    <url>git@github.com:softprops/{name.value}.git</url>
    <connection>scm:git:git@github.com:softprops/{name.value}.git</connection>
  </scm>
  <developers>
    <developer>
      <id>softprops</id>
      <name>Doug Tangren</name>
      <url>https://github.com/softprops</url>
    </developer>
  </developers>)

bintraySettings
 
bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("yo")
