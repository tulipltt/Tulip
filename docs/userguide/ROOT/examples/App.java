///usr/bin/env jbang "$0" "$@" ; exit $\?
//DEPS io.github.wfouche.tulip:tulip-runtime:2.2.0
//JAVA 25

import io.github.wfouche.tulip.api.TulipApi;

public class App {
    public static void main(String[] args) {
        TulipApi.runTulip("benchmark_config.json");
    }
}
