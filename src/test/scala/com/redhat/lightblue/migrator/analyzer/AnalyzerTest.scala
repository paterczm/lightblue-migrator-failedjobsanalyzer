package com.redhat.lightblue.migrator.analyzer

import org.junit.Assert._
import org.junit.Assert
import org.junit.Test

import com.redhat.lightblue.migrator.analyzer.model._

class AnalyzerTest {
  
    val stackTraceWithLBError = """java.lang.RuntimeException: com.redhat.lightblue.client.response.LightblueResponseException: Error returned from lightblue.{"status":"ERROR","modifiedCount":0,"matchCount":0,"hostname":"lightbluecrud6.prod.a4.vary.redhat.com","dataErrors":[{"data":{"creationDate":"20160818T10:29:13.659-0400","createdBy":"Migrator","userRedHatPrincipal":"LOGIN","userId":7908379,"lastUpdatedBy":"Migrator","hashMethod":"SHA-256","migrationDate":"20160818T10:29:13.659-0400","migratedFrom":"jdbc:oracle:thin:@db...","hashedPassword":"0123","salt":"0123","lastUpdateDate":"20160309T00:00:00.000-0500","objectType":"FooBar","passwordResets#":0,"_id":"0123"},"errors":[{"objectType":"error","context":"rest/SaveCommand/FooBar/save(FooBar:0.0.3)/save/update","errorCode":"mongo-crud:Duplicate","msg":"E11000 duplicate key error index: data.FooBar.$FooBar_userId dup key: { : 7908379 }"}]}]}
	at com.redhat.lightblue.migrator.DefaultMigrator.saveBatch(DefaultMigrator.java:200)
	at com.redhat.lightblue.migrator.DefaultMigrator.save(DefaultMigrator.java:158)
	at com.redhat.lightblue.migrator.Migrator.migrate(Migrator.java:180)
	at com.redhat.lightblue.migrator.Migrator.monitoredRun(Migrator.java:268)
	at com.redhat.lightblue.migrator.AbstractMonitoredThread.run(AbstractMonitoredThread.java:52)
Caused by: com.redhat.lightblue.client.response.LightblueResponseException: Error returned from lightblue.{"status":"ERROR", ... }
	at com.redhat.lightblue.client.response.AbstractLightblueResponse.assertNoErrors(AbstractLightblueResponse.java:60)
	at com.redhat.lightblue.client.response.AbstractLightblueResponse.<init>(AbstractLightblueResponse.java:43)
	at com.redhat.lightblue.client.response.DefaultLightblueDataResponse.<init>(DefaultLightblueDataResponse.java:30)
	at com.redhat.lightblue.client.http.LightblueHttpClient.data(LightblueHttpClient.java:232)
	at com.redhat.lightblue.client.http.LightblueHttpClient.data(LightblueHttpClient.java:38)
	at com.redhat.lightblue.migrator.DefaultMigrator.saveBatch(DefaultMigrator.java:197)
	... 4 more
"""
      
    val stackTraceWithCausedBy = """java.lang.RuntimeException: java.sql.SQLSyntaxErrorException: ORA-00933: SQL command not properly ended

	at com.redhat.lightblue.migrator.jdbc.SomeJdbcMigrator.getSourceDocuments(SomeJdbcMigrator.java:73)
	at com.redhat.lightblue.migrator.Migrator.migrate(Migrator.java:112)
	at com.redhat.lightblue.migrator.Migrator.monitoredRun(Migrator.java:268)
	at com.redhat.lightblue.migrator.AbstractMonitoredThread.run(AbstractMonitoredThread.java:52)
Caused by: java.sql.SQLSyntaxErrorException: ORA-00933: SQL command not properly ended

	at oracle.jdbc.driver.T4CTTIoer.processError(T4CTTIoer.java:447)
	at oracle.jdbc.driver.T4CTTIoer.processError(T4CTTIoer.java:396)
	at oracle.jdbc.driver.T4C8Oall.processError(T4C8Oall.java:951)
	at oracle.jdbc.driver.T4CTTIfun.receive(T4CTTIfun.java:513)
	at oracle.jdbc.driver.T4CTTIfun.doRPC(T4CTTIfun.java:227)
	at oracle.jdbc.driver.T4C8Oall.doOALL(T4C8Oall.java:531)
	at oracle.jdbc.driver.T4CPreparedStatement.doOall8(T4CPreparedStatement.java:208)
	at oracle.jdbc.driver.T4CPreparedStatement.executeForDescribe(T4CPreparedStatement.java:886)
	at oracle.jdbc.driver.OracleStatement.executeMaybeDescribe(OracleStatement.java:1175)
	at oracle.jdbc.driver.OracleStatement.doExecuteWithTimeout(OracleStatement.java:1296)
	at oracle.jdbc.driver.OraclePreparedStatement.executeInternal(OraclePreparedStatement.java:3613)
	at oracle.jdbc.driver.OraclePreparedStatement.executeQuery(OraclePreparedStatement.java:3657)
	at oracle.jdbc.driver.OraclePreparedStatementWrapper.executeQuery(OraclePreparedStatementWrapper.java:1495)
	at com.redhat.lightblue.migrator.jdbc.SomeJdbcMigrator.getSourceDocuments(SomeJdbcMigrator.java:63)
	... 3 more
"""

    @Test
    def analyzeStackTraceWithLBErrorTest() {
        
        stackTraceWithLBError match {
            case LightblueResponse(r) => {
                assertNotNull(r.dataErrors)
                assertEquals(1, r.dataErrors(0).errors.size)
                println(r.dataErrors(0).errors(0))
            }
            case _ => Assert.fail()
        }
        
    }
    
    @Test
    def analyzeStackTraceWithCausedByTest() {

        val j = FailedJob("name", "_id", false, "query", List(JobExecution(stackTraceWithCausedBy)))

        assertEquals("java.sql.SQLSyntaxErrorException: ORA-00933: SQL command not properly ended", j.causedBy)
    }

    @Test
    def testMatchUntilEOL() {
        
        val str = """foobar
          bar
          foo"""
        
        var regex = """(?s)^foo(.*?)
          .*""".r
        
        str match {
            case regex(x) => Assert.assertEquals("bar", x)
            case _ => Assert.fail()
        }                
    }           
}