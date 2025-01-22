# readme
- 开发环境：JDK: 1.8;&nbsp;&nbsp;&nbsp;kafak: 2.8.0,&nbsp;&nbsp;kafka 需要建topic: fraud-detection-topic
- AWSCloudWatchLogger.java 是awsCloudWatch 的实现，为了方便本地调试，已经屏蔽掉；
- 目前告警是通过邮件发送，所以配置的我的邮箱，注意修改application.properties邮箱配置，否则收不到邮件
- 在开发的电脑上docker能正常跑起来，需要注意修改application.properties中bootstrap-servers值
- 由于电脑装的k8s有点问题，所以只写了yaml文件文件，未验证；
- 本次代码的设计及测试用例见：HSBC homework.pdf；
- 有三个操作视频，每次操作内容见标题


- 打包命令

mvn clean install package -DskipTests

- 镜像包制作

在项目根目录执行如下命令

```sh
docker build -f Dockerfile -t fraud-detection:latest .
```

- 运行

docker run -d -p 8080:8080 fraud-detection:latest

- 创建 namespace

````bash
kubectl create ns fraud-detection
```
- 部署

```bash
kubectl apply -f deployment.yaml
````

