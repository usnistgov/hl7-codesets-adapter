# Pull base image.
FROM mongo:3.7.9

RUN apt-get update \
    && apt-get install -y curl unzip vim wget \
        && apt-get install -y --no-install-recommends \
                ca-certificates \
                jq \
                numactl \
        && rm -rf /var/lib/apt/lists/*

COPY ./import.sh /docker-entrypoint-initdb.d/import.sh
RUN ["mkdir", "/tmp/dump/"]






