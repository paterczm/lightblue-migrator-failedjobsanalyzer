package com.redhat.lightblue.migrator.analyzer

import com.redhat.lightblue.client.Projection
import com.redhat.lightblue.client.Query
import com.redhat.lightblue.client.http.LightblueHttpClient
import com.redhat.lightblue.client.request.data.DataFindRequest
import com.fasterxml.jackson.databind.node.ArrayNode

import scala.collection.JavaConversions._
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.charset.StandardCharsets
import scala.util.control.Breaks._

import com.redhat.lightblue.migrator.analyzer.model._

/**
 * FailedJobAnalyzer
 *
 */
class FailedJobsAnalyzer(args: Array[String], classifiersList: Option[List[FailedJob => Boolean]])  {

    def this(args: Array[String]) = this(args, None)

    val clientFilePath = args(0)

     // configure json mapper for scala
    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    println(s"""Loading Lightblue client configuration from $clientFilePath""")
    val lbClient = new LightblueHttpClient(clientFilePath)

    val failedJobs = fetchFailedJobs()

    classifiersList match {
        case None => ;
        case Some(classifiers) => failedJobs.foreach({ job => {
            breakable {
                // apply classifiers until one matches
                classifiers.foreach { classifier =>
                    if (classifier(job)) {
                        break;
                    }
                }
            }
        }
        })
    }

    generateHtmlReport(failedJobs)

    def fetchFailedJobs(): List[FailedJob] = {
        val findRequest = new DataFindRequest("migrationJob", null)

        findRequest.where(Query.and(Query.withValue("status = failed")/*, Query.withValue("generated = false")*/))
        findRequest.select(Projection.includeFieldRecursively("*"));

        val response = lbClient.data(findRequest)

        val json = response.getJson

        val failedJobsArray = json.get("processed").asInstanceOf[ArrayNode].iterator().toList

        failedJobsArray.map(x => mapper.treeToValue(x, classOf[FailedJob]))
    }

    def generateHtmlReport(failedJobs: List[FailedJob]) {
        println(s"""Found ${failedJobs.length} failed jobs. Processing...""")

        val errorClassCountsMap = scala.collection.mutable.HashMap.empty[String, Int]

        failedJobs foreach (failedJob => {

            val error = failedJob.lightblueResponse match {
                case Some(x) => x.pretty() // lightblue resonse
                case None => failedJob.errMsg // no lightblue response, write stack trace
            }

            Files.write(Paths.get(s"""./report/${failedJob._id}.html"""), s"""<pre>$error</pre>""".getBytes(StandardCharsets.UTF_8))

            val classId = failedJob.custom match {
                case Some(x) => x.id
                case None => "None"
            }

            errorClassCountsMap(classId) = errorClassCountsMap.get(classId) match {
                case Some(x) => x+1
                case None => 1
            }
        })

        val jobsWithLBResponse = failedJobs.filter { j => j.lightblueResponse.isDefined }
        val jobsOther = failedJobs.filter { j => !j.lightblueResponse.isDefined }

        val htmlReportPage = html.index.render(jobsWithLBResponse, jobsOther, scala.collection.immutable.ListMap(errorClassCountsMap.toSeq.sortWith(_._2 > _._2):_*), new java.util.Date());

        Files.write(Paths.get(s"""./report/index.html"""), htmlReportPage.body.getBytes(StandardCharsets.UTF_8))

        println("Done!")
    }

}