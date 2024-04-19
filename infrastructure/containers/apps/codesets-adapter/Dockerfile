FROM openjdk:21

RUN mkdir -p /usr/local/codesets-adapter

COPY ./codesets-adapter.jar /usr/local/codesets-adapter
COPY run.sh /usr/local/bin/run-server.sh
RUN chmod a+rx /usr/local/bin/run-server.sh

CMD /usr/local/bin/run-server.sh





