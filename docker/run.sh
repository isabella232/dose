#!/bin/sh

docker stop $(docker ps -a -q)
docker rm $(docker ps -a -q)

docker run -d -p 8002:80 -P --name="consul1" --hostname="consul1" "untoldwind/dose:consul-boot-v1"
docker run -d -P --name="consul2" --link consul1:consul1 --hostname="consul2" "untoldwind/dose:consul-v1"
docker run -d -P --name="consul3" --link consul1:consul1 --hostname="consul3" "untoldwind/dose:consul-v1"
docker run -d -p 8000:80 -P --name="log" --hostname="log" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3  "untoldwind/dose:log-v4"
docker run -d -p 8001:80 -p 9410:9410 -P --name="zipkin" --hostname="zipkin" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:zipkin-v3"
docker run -d -p 3306:3306 -P --name="mysql" --hostname="mysql" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:mysql-v4" 
docker run -d -p 27017:27017 -P --name="mongo" --hostname="mongo" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:mongo-v1" 

# Uggly but necessary atm
sleep 5

docker run -d -P --name="customer" --hostname="customer" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:customer-v4" 
docker run -d -p 8090:80 -P --name="product" --hostname="product" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:product-v4" 
docker run -d -P --name="cart" --hostname="cart" --link mysql:mysql-db --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:cart-v4"
docker run -d -P --name="billing" --link cart:cart-service --link product:product-service --link customer:customer-service --link mysql:mysql-db --link zipkin:zipkin-collector --hostname="billing" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:billing-v4"
docker run -d -p 8080:80 -P --name="web" --hostname="web" --link consul1:consul1 --link consul2:consul2 --link consul3:consul3 "untoldwind/dose:web-v4"
