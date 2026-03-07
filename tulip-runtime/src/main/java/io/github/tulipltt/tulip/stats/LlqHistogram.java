// language: java
package io.github.tulipltt.tulip.stats;

// spotless:off
//DEPS tools.jackson.core:jackson-databind:3.0.3
//DEPS org.hdrhistogram:HdrHistogram:2.2.2
// spotless:on

import java.io.IOException;
import java.util.*;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * LlqHistogram provides a lightweight quantized histogram optimized for recording time durations
 * (in nanoseconds) and computing aggregates such as percentiles, average and standard deviation on
 * quantized buckets.
 *
 * <p>The histogram uses a precomputed set of quantized values ({@code qValues}) and maintains
 * counts per bucket in {@code qCounts}. Values recorded are quantized via {@link #llq(long)} before
 * being counted. Typical use-cases: recording many latency measurements, serializing/deserializing
 * counts, combining histograms, and generating simple HTML reports.
 *
 * <p>Important characteristics:
 *
 * <ul>
 *   <li>Quantization is deterministic and designed for time units (ns, μs, ms, s).
 *   <li>Thread-safety is <b>not</b> provided by this class; external synchronization is required
 *       for concurrent updates.
 *   <li>Serialization methods provide simple JSON mapping keyed by quantized value.
 * </ul>
 */
public class LlqHistogram {

    /** Default constructor for LlqHistogram */
    public LlqHistogram() {}

    /**
     * Internal helper grouping a quantized bucket value and its count. Used for percentile
     * computation.
     */
    private record Bin(long value, long count) implements Comparable<Bin> {
        /**
         * Create a Bin for a quantized value and its observed count.
         *
         * @param value quantized bucket value
         * @param count number of observations in the bucket
         */
        private Bin {}

        /**
         * Compare bins by their quantized value (ascending).
         *
         * @param other other bin to compare with
         * @return negative if this.value < other.value, 0 if equal, positive otherwise
         */
        @Override
        public int compareTo(Bin other) {
            return Long.compare(this.value, other.value);
        }
    }

    /**
     * Smallest measurable duration (in nanoseconds) observed during static initialization and
     * quantized using {@link #llq(long)}. This value is used for display formatting of
     * sub-nanosecond buckets.
     */
    private static final long minNanos;

    /**
     * Precomputed quantized values used as bucket keys. Length is 210 to cover the range needed by
     * this histogram implementation.
     */
    private static final long[] qValues = new long[210];

    /** Counts for each bucket in {@link #qValues}. Instance-level; not shared. */
    private final long[] qCounts = new long[210];

    /**
     * Quantize a raw value {@code n} into the histogram's low-latency quantization bucket. The
     * method reduces resolution for large values while preserving readable scale transitions (ns →
     * μs → ms → s).
     *
     * @param n raw value (typically nanoseconds)
     * @return quantized value used as a bucket key
     */
    public static long llq(long n) {
        if (n < 10) {
            return n;
        }

        long p = 1;
        while (p * 10 <= n) {
            p *= 10;
        }
        long scale = p / 10;

        // v25 = 25 * scale, v50 = 50 * scale
        long v25 = 25 * scale;
        long v50 = 50 * scale;

        // rounding logic
        return (((n * 10 + v25) / v50) * v50) / 10;
    }

    /**
     * Merge another LlqHistogram into this histogram by summing corresponding bucket counts.
     *
     * @param llqh the other histogram to add into this one
     */
    public void add(LlqHistogram llqh) {
        for (int i = 0; i < qCounts.length; i++) {
            qCounts[i] += llqh.qCounts[i];
        }
    }

    /**
     * Record counts from an HdrHistogram instance into this histogram. Each value from {@code hdr}
     * will be scaled by {@code scaleFactor} before recording.
     *
     * @param hdr the HdrHistogram source of recorded values
     */
    public void add(org.HdrHistogram.Histogram hdr) {
        for (HistogramIterationValue v : hdr.recordedValues()) {
            recordValue(v.getValueIteratedTo(), v.getCountAddedInThisIterationStep());
        }
    }

    /** Reset this histogram by zeroing all bucket counts. */
    public void reset() {
        Arrays.fill(qCounts, 0L);
    }

    /**
     * Record a single observation of raw value {@code n} (count = 1).
     *
     * @param n raw value to record
     */
    public void recordValue(long n) {
        recordValue(n, 1);
    }

    /**
     * Record {@code count} observations of raw value {@code n}. The value will be quantized and the
     * appropriate bucket count incremented.
     *
     * @param n raw value to record
     * @param count number of observations to add
     * @throws ArrayIndexOutOfBoundsException if quantized value does not map to an index
     */
    public void recordValue(long n, long count) {
        long q = llq(n);
        int index = Arrays.binarySearch(qValues, q);
        qCounts[index] += count;
    }

    /**
     * Return the smallest quantized value that currently has a non-zero count.
     *
     * @return minimum recorded quantized value, or 0 if histogram is empty
     */
    public long minValue() {
        for (int i = 0; i < qCounts.length; i++) {
            if (qCounts[i] != 0) {
                return qValues[i];
            }
        }
        return 0;
    }

    /**
     * Return the largest quantized value that currently has a non-zero count.
     *
     * @return maximum recorded quantized value, or 0 if histogram is empty
     */
    public long maxValue() {
        for (int i = qCounts.length - 1; i >= 0; i--) {
            if (qCounts[i] != 0) {
                return qValues[i];
            }
        }
        return 0;
    }

    /**
     * Return the total number of recorded observations across all buckets.
     *
     * @return total count of observations
     */
    public long numValues() {
        long totalCount = 0;
        for (long count : qCounts) {
            totalCount += count;
        }
        return totalCount;
    }

    /**
     * Compute the average (mean) value across all recorded observations. The returned value is
     * based on the quantized bucket values and their counts.
     *
     * @return arithmetic mean of recorded values (0.0 if no observations)
     */
    public double averageValue() {
        double totalSum = 0.0;
        long totalCount = 0;
        for (int i = 0; i < qCounts.length; i++) {
            long qvalue = qValues[i];
            long qcount = qCounts[i];
            totalSum += (double) qvalue * (double) qcount;
            totalCount += qcount;
        }
        if (totalCount == 0) {
            return 0.0;
        }
        return totalSum / totalCount;
    }

    /**
     * Estimate a percentile value (by quantized bucket) using the inclusive definition: the
     * smallest value whose cumulative count is >= the target rank.
     *
     * @param percentile percentile in range [0.0, 100.0]
     * @return quantized bucket value corresponding to the requested percentile
     * @throws IllegalArgumentException if {@code percentile} is out of range
     */
    public long percentileValue(double percentile) {
        if (percentile < 0.0 || percentile > 100.0) {
            throw new IllegalArgumentException(
                    "Percentile must be between 0.0 and 100.0 (inclusive).");
        }

        // Step 1: Combine and Sort Bins
        List<Bin> bins = new ArrayList<>(qValues.length);
        long totalCount = 0L;

        for (int i = 0; i < qCounts.length; i++) {
            long count = qCounts[i];
            if (count > 0) {
                bins.add(new Bin(qValues[i], count));
                totalCount += count;
            }
        }

        // Handle empty dataset
        if (totalCount == 0) {
            return 0L;
        }

        // Handle 0th and 100th percentile edge cases for quick return
        if (percentile == 0.0) {
            return bins.get(0).value; // Minimum value
        }
        if (percentile == 100.0) {
            return bins.get(bins.size() - 1).value; // Maximum value
        }

        // Step 2: Determine Target Rank (R)
        long targetRank = (long) Math.ceil((percentile / 100.0) * totalCount);
        long cumulativeCount = 0L;

        // Step 3: Find the Quantized Value at the Target Rank
        for (Bin bin : bins) {
            cumulativeCount += bin.count;
            if (cumulativeCount >= targetRank) {
                return bin.value;
            }
        }

        // Fallback
        return bins.get(bins.size() - 1).value;
    }

    /**
     * Compute the population standard deviation of recorded values (based on quantized bucket
     * values and counts). Returns 0.0 when fewer than two observations exist.
     *
     * @return population standard deviation (0.0 if not enough observations)
     */
    public double standardDeviationValue() {
        double mean = averageValue();

        long totalCount = 0L;
        double sumOfSquaredDifferences = 0.0;

        for (int i = 0; i < qCounts.length; i++) {
            long value = qValues[i];
            long count = qCounts[i];

            if (count > 0) {
                totalCount += count;
                double difference = (double) value - mean;
                sumOfSquaredDifferences += Math.pow(difference, 2) * (double) count;
            }
        }

        if (totalCount <= 1) {
            return 0.0;
        }

        double variance = sumOfSquaredDifferences / totalCount;
        return Math.sqrt(variance);
    }

    /**
     * Format a quantized time value for HTML display, choosing appropriate units.
     *
     * @param qv quantized value in nanoseconds
     * @param prefix label prefix (e.g. "avg: ") inserted before the value
     * @return an HTML table cell fragment with formatted time
     */
    private String formatTimeValue(long qv, String prefix) {
        if (qv < 1000L) {
            if (qv == 0L) {
                return String.format("    <td>%s &lt; %d ns</td>\n", prefix, minNanos);
            } else {
                return String.format("    <td>%s %d ns</td>\n", prefix, qv);
            }
        } else if (qv < 1_000_000L) {
            return String.format(Locale.US, "    <td>%s %.1f μs</td>\n", prefix, qv / 1000.0);
        } else if (qv < 1_000_000_000L) {
            return String.format(Locale.US, "    <td>%s %.1f ms</td>\n", prefix, qv / 1000000.0);
        } else {
            return String.format(Locale.US, "    <td>%s %.1f s</td>\n", prefix, qv / 1000000000.0);
        }
    }

    /**
     * Produce a simple HTML table fragment summarizing the histogram buckets and a small set of
     * summary statistics (average, sd, p50, p90, p95, p99).
     *
     * @return HTML string fragment (table rows) summarizing the histogram
     */
    public String toHtmlString() {
        long nv = numValues();
        long cv = 0;
        long av;
        StringBuilder htmlString = new StringBuilder();
        htmlString.append("  <tr>\n");
        htmlString.append("    <th>Value</th>\n");
        htmlString.append("    <th>Percentile</th>\n");
        htmlString.append("    <th>Total Count</th>\n");
        htmlString.append("    <th>Bucket Size</th>\n");
        htmlString.append("    <th>Percentage</th>\n");
        htmlString.append("    <th>Above Count</th>\n");
        htmlString.append("  </tr>\n");
        for (int i = qCounts.length - 1; i >= 0; i--) {
            if (qCounts[i] != 0) {
                av = cv;
                cv += qCounts[i];
                htmlString.append("  <tr>\n");
                long qv = qValues[i];

                // Value
                htmlString.append(formatTimeValue(qv, ""));

                // Percentile
                htmlString.append(
                        String.format(Locale.US, "    <td>%.6f</td>\n", 1.0 - 1.0 * av / nv));

                // Total Count
                htmlString.append(String.format("    <td>%d</td>\n", nv - av));

                // Bucket Size
                htmlString.append(String.format("    <td>%d</td>\n", qCounts[i]));

                // Percentage
                double percentage = 100.0 * qCounts[i] / nv;
                if (percentage >= 1.0) {
                    htmlString.append(
                            String.format(
                                    Locale.US, "    <td><mark>%.3f</mark></td>\n", percentage));
                } else {
                    htmlString.append(String.format(Locale.US, "    <td>%.3f</td>\n", percentage));
                }

                // Above Count
                htmlString.append(String.format("    <td>%d</td>\n", av));

                htmlString.append("  </tr>\n");
            }
        }

        htmlString.append("  <tr>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("    <td></td>\n");
        htmlString.append("  </tr>\n");

        htmlString.append("  <tr>\n");
        htmlString.append(formatTimeValue((long) averageValue(), "avg: "));
        htmlString.append(formatTimeValue((long) standardDeviationValue(), "sd: "));
        htmlString.append(formatTimeValue(percentileValue(50.0), "p50: "));
        htmlString.append(formatTimeValue(percentileValue(90.0), "p90: "));
        htmlString.append(formatTimeValue(percentileValue(95.0), "p95: "));
        htmlString.append(formatTimeValue(percentileValue(99.0), "p99: "));
        htmlString.append("  </tr>\n");

        return htmlString.toString();
    }

    /**
     * Populate this histogram from a JSON string where keys are quantized values (as numbers)
     * mapped to counts. Existing counts are cleared before loading.
     *
     * @param jsonString JSON string mapping quantized value → count
     * @throws IOException on JSON parsing errors
     */
    public void fromJsonString(String jsonString) throws IOException {
        TypeReference<Map<Long, Long>> typeRef = new TypeReference<>() {};
        reset();
        JsonMapper jsonMapper = new JsonMapper();
        Map<Long, Long> longMap = jsonMapper.readValue(jsonString, typeRef);
        longMap.forEach(this::recordValue);
    }

    /**
     * Serialize the non-zero buckets to a compact JSON string mapping quantized value → count.
     * Note: this method constructs JSON manually; for robust serialization prefer {@link
     * #fromJsonString(String)} + ObjectMapper.
     *
     * @return JSON string representation of non-zero buckets
     */
    public String toJsonString() {
        StringBuilder jsonString = new StringBuilder("{");
        int count = 0;
        for (int i = 0; i < qCounts.length; i++) {
            if (qCounts[i] != 0) {
                if (count > 0) {
                    jsonString.append(", ");
                }
                jsonString.append("\"");
                jsonString.append(qValues[i]);
                jsonString.append("\"");
                jsonString.append(": ");
                jsonString.append(qCounts[i]);
                count += 1;
            }
        }
        jsonString.append("}");
        return jsonString.toString();
    }

    /**
     * Print a human-readable summary of key statistics and the JSON representation to standard out.
     * Intended for quick debugging; use programmatic accessors for production code.
     */
    public void display() {
        System.out.println();
        System.out.println("  AVG: " + averageValue());
        System.out.println("   SD: " + standardDeviationValue());
        System.out.println("  P50: " + percentileValue(50.0));
        System.out.println("  P90: " + percentileValue(90.0));
        System.out.println("  P95: " + percentileValue(95.0));
        System.out.println("  P99: " + percentileValue(99.0));
        System.out.println("  MIN: " + minValue());
        System.out.println("  MAX: " + maxValue());
        System.out.println("  NUM: " + numValues());
        String json = toJsonString();
        System.out.println("  JSN: " + json);
    }

    static {
        // initialize qValues
        int idx = 0;
        while (idx < 11) {
            qValues[idx] = idx;
            idx += 1;
        }

        long num = 10L;
        long step = 5L;
        while (num < 1_000_000_000_001L) {
            num += step;
            qValues[idx] = num;
            idx += 1;
            if (Math.log10(num) == (double) ((int) Math.log10(num))) {
                step *= 10L;
            }
        }

        // determine minNanos - smallest duration nanoTime can measure
        long[] duration = new long[1_000_000];
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        long t0 = System.nanoTime();
        long t1 = t0;
        for (int i = 0; i < duration.length; i++) {
            while (t1 == t0) {
                t1 = System.nanoTime();
            }
            duration[i] = t1 - t0;
            t0 = t1;
        }
        for (long value : duration) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        minNanos = llq(min);
    }

    /**
     * Simple command-line exercise of the LlqHistogram class. Not intended for production use;
     * included for convenience when running the class directly.
     *
     * @param args command-line arguments (ignored)
     * @throws IOException on JSON parsing errors
     */
    public static void main(String[] args) throws IOException {
        LlqHistogram hist = new LlqHistogram();
        Histogram hdr = new Histogram(4);

        for (long i = 0; i < 1_000_000_001L; i++) {
            hist.recordValue(i);
            hdr.recordValue(i);
        }
        hist.display();
        System.out.println();
        LlqHistogram hist2 = new LlqHistogram();
        hist2.add(hdr);
        hist2.fromJsonString(hist2.toJsonString());
        hist2.display();
    }
}
