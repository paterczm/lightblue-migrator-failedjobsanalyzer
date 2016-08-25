package com.redhat.lightblue.migrator.analyzer

object FailedJobsAnalyzerApp {

    def main(args: Array[String]): Unit = {
      new FailedJobsAnalyzer(args)
    }

}