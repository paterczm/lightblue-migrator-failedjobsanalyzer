package com.redhat.lightblue.migrator.analyzer

import com.redhat.lightblue.migrator.analyzer.model.MigrationJob

object MigratorJobsJsonGenerator {
    def apply(migrationJobs: List[MigrationJob]): String = html.migrationJobs.render(migrationJobs).toString()
}