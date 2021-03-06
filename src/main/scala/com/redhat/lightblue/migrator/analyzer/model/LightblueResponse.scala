package com.redhat.lightblue.migrator.analyzer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode

import scala.collection.JavaConversions._
import com.fasterxml.jackson.annotation.JsonRawValue

case class LightblueResponse(response: JsonNode) {
    def dataErrors: List[DataError] = {
        val jsonArray = response.get("dataErrors").asInstanceOf[ArrayNode].iterator().toList
        jsonArray.map(x => LightblueResponse.mapper.treeToValue(x, classOf[DataError]))
    }

    def error: String = dataErrors(0).errors(0).error

    def errors: List[String] = {
        val buf = scala.collection.mutable.MutableList[String]()
        dataErrors.foreach { dataErr =>
            dataErr.errors foreach { err => buf+=(err.error) }
        }

        buf.toList
    }

    def pretty(): String = LightblueResponse.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response)
}

object LightblueResponse {

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)

    val regex = """.*LightblueResponseException: Error returned from lightblue\.(.*?)$""".r

    def unapply(errMsg: String): Option[LightblueResponse] = {

        // match only first line of the exception stack trace
        errMsg.split("\n")(0) match {
            case regex(jsonPart) => {
                Some(LightblueResponse(mapper.readTree(jsonPart)))
            }
            case _ => None
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class DataError(errors: List[Error], @JsonRawValue data: JsonNode)
@JsonIgnoreProperties(ignoreUnknown = true)
case class Error(context: String, errorCode: String, msg: String) {
    def error = s"""$errorCode $msg"""
}