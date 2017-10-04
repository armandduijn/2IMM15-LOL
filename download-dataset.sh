#!/bin/sh

URL="https://www.dropbox.com/s/mx738t2hyov4214/nips-papers.zip?dl=1"
URL_DERIVED="https://www.dropbox.com/s/o58tj0vzapso6ug/nips-papers-derived.zip?dl=1"

mkdir -p data/derived         && \
cd data                       && \
curl -L -O -J $URL            && \
unzip nips-papers.zip         && \
rm nips-papers.zip            && \
cd derived                    && \
curl -L -O -J $URL_DERIVED    && \
unzip nips-papers-derived.zip && \
rm nips-papers-derived.zip
