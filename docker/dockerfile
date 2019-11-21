FROM ubuntu:18.04
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    python3.7 python3.7-dev \
    python3-pip \
    ruby rpm \
    nodejs npm \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
