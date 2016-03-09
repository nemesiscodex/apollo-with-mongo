FROM maven:latest

RUN mkdir /code

WORKDIR /code

EXPOSE 8080