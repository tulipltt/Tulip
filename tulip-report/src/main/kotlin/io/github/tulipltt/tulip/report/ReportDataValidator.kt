package io.github.tulipltt.tulip.report

import org.slf4j.LoggerFactory

/**
 * Validates the report data and identifies missing or suspicious values.
 */
object ReportDataValidator {
    private val logger = LoggerFactory.getLogger(ReportDataValidator::class.java)

    /**
     * Validates the [ReportData] and returns a list of warning messages.
     */
    fun validate(data: ReportData): List<String> {
        val warnings = mutableListOf<String>()

        if (data.results.isEmpty()) {
            val msg = "No benchmark results found in report data."
            logger.warn(msg)
            warnings.add(msg)
        }

        data.results.forEachIndexed { index, result ->
            if (result.bmName == null) {
                val msg = "Benchmark result at index $index is missing a name."
                logger.warn(msg)
                warnings.add(msg)
            }
            if (result.duration == null || result.duration <= 0.0) {
                val msg = "Benchmark '${result.bmName ?: "Unknown"}' at index $index has invalid duration: ${result.duration}"
                logger.warn(msg)
                warnings.add(msg)
            }
            if (result.numActions == null) {
                val msg = "Benchmark '${result.bmName ?: "Unknown"}' at index $index is missing action count."
                logger.warn(msg)
                warnings.add(msg)
            }
        }

        return warnings
    }
}
