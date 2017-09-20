#!/bin/sh

URL="https://www.dropbox.com/s/mx738t2hyov4214/nips-papers.zip?dl=1"

mkdir -p data         && \
cd data               && \
curl -L -O -J $URL    && \
unzip nips-papers.zip && \
rm nips-papers.zip
