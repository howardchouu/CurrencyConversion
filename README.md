# Currency Conversion Demo

Use Java Controller to implement RESTful APIs on Currency Conversion

Demo link : http://ec2-18-117-182-46.us-east-2.compute.amazonaws.com:8080

## Features
* Tomcat v9.0.46
* JavaSE 11
* deploy on Amazon AWS EC2
* Used Docker Container

## Build & Installation

### Pre-installation
* apt-get install docker.io

### Step 1

```bash
$ docker run -it --name javaweb -p 8080:8080 tomcat:9.0.46-jdk11
$ docker run -it --name postgres -e POSTGRES_PASSWORD=demo  postgres:13
```

### Step 2

* SSH into a running container
```bash
$ docker exec -ti javaweb bash
```
* Use bash command in javaweb container
```bash=
apt update; \
apt install -y git; \
mkdir /usr/local/tmp_git; \
cd /usr/local/tmp_git; \
git clone https://github.com/howardchouu/CurrencyConversion.git; \
cp CurrencyConversion/Demo.war /usr/local/tomcat/webapps/; \
cp -f CurrencyConversion/conf/server.xml /usr/local/tomcat/conf/; \
rm -rf /usr/local/tmp_git;
```
* Restart javaweb container
```bash
docker restart javaweb
```

### Step 3
* SSH into a running container
```bash
$ docker exec -ti postgres bash
```
* Use bash command in postgres container
```bash=
apt update; \
apt install -y git; \
mkdir /var/tmp/git_tmp; \
cd /var/tmp/git_tmp; \
git clone https://github.com/howardchouu/CurrencyConversion.git; \
psql -h localhost -d postgres -U postgres -f /var/tmp/git_tmp/CurrencyConversion/conf/demo.sql; \
rm -rf /var/tmp/git_tmp
```

### Step 4

* Check the bridge network
```bash
$ docker inspect bridge
```
* The information of below is demo example
    * Check IPv4Address of postgres is 172.17.0.3/16
```
"Containers": {
            "3628c0e860192d596e6c11d50147e96bc5e5f0897f3169d05d55c5a133c27ccf": {
                "Name": "postgres",
                "EndpointID": "3cf5d2d93a8c9d43d8cfd81a0ab6550db8e093ea076eafabcca28427a1290275",
                "MacAddress": "02:42:ac:11:00:03",
                "IPv4Address": "172.17.0.3/16",
                "IPv6Address": ""
            },
            "e00e675612cfe9bd802345b4cc315f1b120f17cd88c1dc5bd7056856299b48fd": {
                "Name": "javaweb",
                "EndpointID": "e958ac8f89a63e3adb5d58f14488233b3ce9811bbcfd14a24ef43affd3e0addf",
                "MacAddress": "02:42:ac:11:00:02",
                "IPv4Address": "172.17.0.2/16",
                "IPv6Address": ""
            }
        },
```