FROM java:8-jdk
COPY ./target/gs-serving-web-content-0.1.0.jar /usr/app/
COPY ./ /usr/app/
WORKDIR /usr/app
RUN  apt-get install -y libfontconfig
RUN sh -c 'touch gs-serving-web-content-0.1.0.jar'
ENTRYPOINT ["java","-jar","gs-serving-web-content-0.1.0.jar"]
