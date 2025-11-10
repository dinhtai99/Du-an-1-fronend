# Hướng dẫn khắc phục lỗi Gradle Timeout

## Vấn đề
Lỗi: `Could not install Gradle distribution from 'https://services.gradle.org/distributions/gradle-8.13-bin.zip'. Reason: java.net.SocketTimeoutException: Read timed out`

## Giải pháp đã áp dụng

### 1. Đã tăng timeout trong gradle.properties
Đã thêm các cấu hình sau vào `gradle.properties`:
```
systemProp.http.connectionTimeout=60000
systemProp.http.socketTimeout=60000
org.gradle.daemon.idletimeout=10800000
```

### 2. Các giải pháp khác bạn có thể thử:

#### Giải pháp A: Tải thủ công Gradle
1. Tải Gradle 8.13 từ: https://gradle.org/releases/
2. Giải nén file zip
3. Đặt vào thư mục: `~/.gradle/wrapper/dists/gradle-8.13-bin/[hash]/`
   - Hash có thể tìm trong thư mục đã tạo sẵn
4. Thử build lại

#### Giải pháp B: Sử dụng mirror hoặc proxy
Nếu đang ở Việt Nam, có thể thử:
1. Sử dụng VPN
2. Hoặc cấu hình proxy trong `gradle.properties`:
```
systemProp.http.proxyHost=your.proxy.host
systemProp.http.proxyPort=8080
systemProp.https.proxyHost=your.proxy.host
systemProp.https.proxyPort=8080
```

#### Giải pháp C: Xóa cache và thử lại
```bash
rm -rf ~/.gradle/wrapper/dists/gradle-8.13-bin
./gradlew --stop
./gradlew build
```

#### Giải pháp D: Sử dụng Gradle version khác
Nếu vẫn gặp vấn đề, có thể thử downgrade Gradle version trong `gradle/wrapper/gradle-wrapper.properties`:
```
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
```

## Kiểm tra kết nối
Chạy lệnh sau để kiểm tra kết nối:
```bash
curl -I https://services.gradle.org/distributions/gradle-8.13-bin.zip
```

Nếu lệnh này cũng timeout, vấn đề là do mạng hoặc firewall.

