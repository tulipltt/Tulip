package io.github.tulipltt.tulip.report

import org.asciidoctor.Asciidoctor
import org.asciidoctor.Attributes
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import java.io.File

/**
 * Handles AsciiDoc generation and conversion for Tulip reports.
 */
object ReportAdoc {
    /**
     * Generates an AsciiDoc summary report.
     */
    fun generateAsciiDoc(reportData: ReportData): String {
        val adoc = StringBuilder()
        adoc.append("= Benchmark Configuration Report\n")
        adoc.append(":toc: left\n\n")

        adoc.append("== Metadata\n\n")
        adoc.append("* *Version*: ${reportData.version}\n")
        adoc.append("* *Timestamp*: ${reportData.timestamp}\n\n")

        adoc.append("== Actions\n\n")
        val actions = reportData.config.actions
        adoc.append("[cols=\"1,2\"]\n|===\n")
        adoc.append("| Description | ${actions.description}\n")
        adoc.append("| User Class | ${actions.userClass}\n")
        adoc.append("| Output File | ${actions.jsonFilename}\n")
        adoc.append("| Report File | ${actions.htmlFilename}\n")
        adoc.append("|===\n\n")

        adoc.append("=== User Parameters\n\n")
        adoc.append("[cols=\"1,2\"]\n|===\n")
        actions.userParams.forEach { (k, v) ->
            adoc.append("| $k | $v\n")
        }
        adoc.append("|===\n\n")

        adoc.append("== Benchmarks\n\n")
        reportData.config.benchmarks.forEach { (name, config) ->
            adoc.append("=== $name\n\n")
            adoc.append("* *Enabled*: ${config.enabled}\n")
            adoc.append("* *APS Rate*: ${config.throughputRate}\n")
            adoc.append("* *Workflow*: ${config.workflow}\n")
            adoc.append("\n")
        }

        return adoc.toString()
    }

    fun convertAdocToHtml(adocPath: String) {
        val asciidoctor = Asciidoctor.Factory.create()
        val cssUrl =
            "https://raw.githubusercontent.com/tulipltt/Tulip/" +
                "refs/heads/main/docs/css/adoc-foundation.css"
        val attributes =
            Attributes.builder()
                .attribute("linkcss", false)
                .attribute("data-uri", true)
                .attribute("allow-uri-read", true)
                .attribute("stylesheet", cssUrl)
                .build()

        try {
            asciidoctor.requireLibrary("asciidoctor-diagram")
        } catch (ignore: Exception) {
            // Diagram support is optional
        }

        asciidoctor.convertFile(
            File(adocPath),
            Options.builder()
                .toFile(true)
                .attributes(attributes)
                .safe(SafeMode.UNSAFE)
                .build(),
        )
        asciidoctor.shutdown()
    }
}
