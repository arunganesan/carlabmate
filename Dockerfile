FROM ubuntu:19.10
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
    python3.7 python3.7-dev \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080
