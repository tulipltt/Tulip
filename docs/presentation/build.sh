#!/bin/bash
jbang run asciidoctorj@asciidoctor \
    -r asciidoctor-revealjs \
    -r asciidoctor-diagram \
    presentation.adoc \
    -b revealjs \
    -a revealjsdir=https://cdn.jsdelivr.net/npm/reveal.js@6.0.1
