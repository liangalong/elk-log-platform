FROM docker.elastic.co/logstash/logstash:8.12.0
RUN bin/logstash-plugin install logstash-filter-json
COPY pipeline/logstash.conf /usr/share/logstash/pipeline/logstash.conf