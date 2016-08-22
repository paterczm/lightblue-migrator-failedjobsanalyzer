package com.redhat.lightblue.migrator.analyzer.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class FailedJob(configurationName: String, _id: String, generated: Boolean, query: String, jobExecutions: List[JobExecution]) {
    def errMsg = jobExecutions(0).errorMsg

    def causedBy = errMsg match {
        case FailedJob.causedByRegex(msg) => msg
        case FailedJob.socketExceptionRegex() => "SocketException"
        case _ => "<cause not found>"
    }

    def lightblueResponse: Option[LightblueResponse] = {
        errMsg match {
            case LightblueResponse(x) => Some(x)
            case _ => None
        }
    }
}

object FailedJob {
    val causedByRegex = "(?s).*Caused by: (.*?)\n.*".r
    val socketExceptionRegex = "(?s).*java.net.SocketException.*".r
}

@JsonIgnoreProperties(ignoreUnknown = true)
case class JobExecution(errorMsg: String)