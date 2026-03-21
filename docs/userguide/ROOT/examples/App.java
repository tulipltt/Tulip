///usr/bin/env jbang "$0" "$@" ; exit $\?
//DEPS io.github.tulipltt:tulip-runtime:2.3.0
//JAVA 21+

import io.github.tulipltt.tulip.api.TulipApi;

public class App {
    public static void main(String[] args) {
        TulipApi.runTulip("benchmark_config.json");
    }
}
