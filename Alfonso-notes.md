# Alfonso docker-compose configurations

Briefly description of changes made on docker-compose and circleci files, containers configurations, etc...

## docker-compose.yml

docker-compose.yml now pulls harness image from https://hub.docker.com/r/actionml/harness/tags
elasticsearch and mongo service was added to docker-compose.yml
directory with test scripts and harness examples were mounted to release memory on the images (helps in reduce deployments execution time and optimizing images size)

Volumes were configured to make data persistent and make logs accessible

 - **Mongo** data path it/shared/data
 - **Mongo** logs path it/shared/logs
 - **Elasticsearch** data path it/shared/esdata (I needed to create the directory)

## circleci/config.yml

circleci deployment tags harness image depending on branch (develop, ci, latest/production)

## rest-server

Dockerfile for rest-server is in docker/Dockerfile
comments were removed from file because will cause issues in future versions
Updated env PATH

## Installing VW to run CBEngine test

a new docker-compose was created called: docker-compose-cb.yml

It's basically the same docker-compose but this one build image instead pull it from hub

***Command To run a different compose file***
`docker-compose -f docker-compose-cb.yml up --build`

where `-f` flag is used to specify the new compose file

A Lot of new libraries & dependencies we're added to Dockerfile


## Running docker-compose for small hosts

To enable docker-compose at startup it's necessary to add setting ***restart:always***
on docker-compose file for every service (ES, Mongo and Harness in our case)

 - it/shared/harness-logs is the new directory for harness-logs
 - it/shared/mongodata/ is the new directory for mongodata
 - it/shared/esdata/ is the new directory for ES-data

For production run:
 
`docker-compose up -d --build` for first time setup
`git pull && docker-compose down && docker-compose up -d --build --force-recreate` to update harness and takedown old containers and create new containers with new harness version
