# ekanek

Setting up:

run the following command in your terminal to clone the repository:
```
  git clone https://github.com/shubhamkumar0/ekanek.git
```
After opening the application in Intellij, Build the project.

Open application.properties and specify:
```
spring.datasource.url=jdbc:mysql://localhost:xxxx/ekanek
spring.datasource.username=root
spring.datasource.password=xxxxxxx

amazonProperties.endpointUrl=https://ap-south-1.amazonaws.com
amazonProperties.accessKey=*enter your access key*
amazonProperties.secretKey=*enter secret key here* 
```

Before running the project you need to set up a local database.
```
  CREATE SCHEMA `ekanek`;
```
Now to create tables inside the schema: 
```
    CREATE TABLE `filedetails` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `name` varchar(150) NOT NULL,
  `desc` varchar(150) NOT NULL DEFAULT '',
  `link` varchar(350) UNIQUE NOT NULL,
    PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;


  CREATE TABLE `userdetails` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `email` varchar(150) NOT NULL,
    `password` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `email` (`email`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```
After the application is running,
go to http://localhost/register
