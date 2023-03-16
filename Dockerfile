FROM mriffle/build-spectr:latest AS builder
MAINTAINER Michael Riffle <mriffle@uw.edu>

ADD . /app
WORKDIR /app

COPY docker/config-files/file_object_storage_config_files_dir_config.properties Webapp_Main/src/main/resources/

RUN ant -f ant_build_all.xml


FROM tomcat:9-jdk11-corretto
MAINTAINER Michael Riffle <mriffle@uw.edu>

COPY --from=builder /app/deploy/file_object_storage.war /usr/local/tomcat/webapps

RUN mkdir /data/ && mkdir /data/config && chmod 777 /data/config

COPY --from=builder /app/docker/config-files/file_object_storage_config_allowed_remotes.properties /data/config
COPY --from=builder /app/docker/config-files/file_object_storage_server_config_dirs_process_cmd.properties /data/config
