///usr/bin/env jbang "$0" "$@" ; exit $?

//DEPS org.python:jython-standalone:2.7.5b1
//DEPS io.leego:banana:2.1.0

import org.python.util.jython;

public class Jython {

    public static void main(String... args) {
        jython.main(args);
    }
}
