///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.python:jython-standalone:2.7.5b1
//DEPS com.google.code.gson:gson:2.11.0
//DEPS org.hdrhistogram:HdrHistogram:2.2.2
//DEPS io.github.wfouche.tulip:tulip-runtime:2.2.0
//DEPS org.asciidoctor:asciidoctorj:3.0.1
//DEPS org.asciidoctor:asciidoctorj-diagram:3.1.0
//DEPS org.asciidoctor:asciidoctorj-diagram-plantuml:1.2025.3

import org.python.util.jython;

public class Jython {

    public static void main(String... args) {
        jython.main(args);
    }

}
