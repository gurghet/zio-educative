FROM openjdk:11 as java-base

ENV SCALA_VERSION 2.13.3
ENV LANG C.UTF-8

WORKDIR /root

# Install scala
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  ln -s /root/scala-$SCALA_VERSION/bin/* /usr/local/bin

# Spread java a little bit
RUN \
  ln -s /usr/local/openjdk-11/bin/* /usr/local/bin

# Install ammonite repl for... science
RUN \
  curl -L -o /usr/local/bin/amm https://git.io/JUVOR && \
  chmod +x /usr/local/bin/amm

# Install dependencies
COPY lib/ /lib
