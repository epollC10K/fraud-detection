FROM openjdk:8

WORKDIR /app
COPY target/fraud-detection-0.0.1-SNAPSHOT.jar /app/

# 暴露端口
EXPOSE 8080
## 运行命令组装一下啦
ENTRYPOINT ["java","-jar","/app/fraud-detection-0.0.1-SNAPSHOT.jar"]