# Pull base image.
# FROM mongo:4.4.15
FROM mongo:latest

RUN apt-get update \
    && apt-get install -y curl unzip vim wget \
        && apt-get install -y --no-install-recommends \
                ca-certificates \
                jq \
                numactl \
        && rm -rf /var/lib/apt/lists/*

COPY ./import.sh /docker-entrypoint-initdb.d/import.sh
RUN ["mkdir", "/tmp/dump/"]






