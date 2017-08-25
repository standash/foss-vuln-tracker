FROM tomcat:8.0-jre8
MAINTAINER standash <stdashevsky@gmail.com>

# install mongodb
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 0C49F3730359A14518585931BC711F9BA15703C6
RUN touch /etc/apt/sources.list.d/mongodb-org-3.4.list
RUN echo "deb http://repo.mongodb.org/apt/debian jessie/mongodb-org/3.4 main" | tee /etc/apt/sources.list.d/mongodb-org-3.4.list
RUN apt-get update
RUN apt-get -y install mongodb
RUN mkdir -p /data/db
RUN touch /var/log/mongodb/mongo-error.log

# cleanup 
RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# start the mongodb and catalina
CMD service mongodb start && catalina.sh run
