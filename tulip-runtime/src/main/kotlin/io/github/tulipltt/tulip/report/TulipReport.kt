package io.github.tulipltt.tulip.report

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.GZIPInputStream
import org.python.util.PythonInterpreter

fun createHtmlReport(outputFilename: String) {
    val decodedBytes = Base64.getDecoder().decode(report_py.mainScriptTextBase64)
    val jythonCode = decompress(decodedBytes)
    PythonInterpreter().use { pyInterp ->
        pyInterp.exec("__name__=\"\"")
        pyInterp.exec(jythonCode)
        pyInterp.eval("createReport(\"${outputFilename}\")")
    }
}

@Throws(IOException::class)
fun decompress(compressedData: ByteArray): String? {
    val bis = ByteArrayInputStream(compressedData)
    val gzip = GZIPInputStream(bis)
    val bos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while ((gzip.read(buffer).also { len = it }) != -1) {
        bos.write(buffer, 0, len)
    }
    gzip.close()
    bis.close()
    bos.close()
    return bos.toString(StandardCharsets.UTF_8.name())
}
