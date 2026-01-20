# INSTALL.md — Hướng dẫn cài đặt & chạy dự án Quản Lý Quán Cà Phê (Spring Boot + MySQL)

Tài liệu này hướng dẫn từ A → Z để chạy được web:
- cài môi trường
- tạo database
- cấu hình
- chạy ứng dụng
- đăng nhập với các role

Mục tiêu: người chưa biết Java vẫn làm theo và chạy được ứng dụng.

---

## 0) Tổng quan nhanh

- Backend: Spring Boot (Maven)
- Template: Thymeleaf
- Database: MySQL 8.x
- Java: JDK 17 (khuyến nghị)
- Port mặc định: `8080`
- Ứng dụng tự tạo bảng trong MySQL từ Entity (KHÔNG cần import schema SQL)

---

## 1) Chuẩn bị phần mềm cần cài

Lưu ý: Mình không thể upload file cài đặt lên nơi khác, nên dùng link chính thức để tải an toàn.

### 1.1 Cài JDK 17
Khuyến nghị: Temurin (Adoptium)
- Tải tại: https://adoptium.net/
- Chọn:
  - Version: **17 (LTS)**
  - OS đúng máy bạn (Windows/Mac/Linux)

Kiểm tra sau khi cài:
```bash
java -version
```

Kết quả đúng kiểu:
```
openjdk version "17.x.x"
```

### 1.2 Cài IDE (khuyến nghị)
Bạn có thể chọn 1 trong 3:
- IntelliJ IDEA Community: https://www.jetbrains.com/idea/download/
- Eclipse IDE: https://www.eclipse.org/downloads/
- VS Code + Java Pack: https://code.visualstudio.com/  
  Java Extension Pack: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

Khuyến nghị IntelliJ vì chạy Spring Boot dễ hơn.

### 1.3 Cài MySQL 8.x
- Tải MySQL: https://dev.mysql.com/downloads/mysql/
- (Tuỳ chọn) MySQL Workbench: https://dev.mysql.com/downloads/workbench/

Sau khi cài xong, hãy nhớ:
- username (thường là `root`)
- password (mật khẩu bạn đặt khi cài)

Kiểm tra MySQL đang chạy:
- Windows: mở Services xem MySQL80 có “Running”
- macOS (brew): `brew services list`

### 1.4 Git (nếu clone source)
- https://git-scm.com/downloads

---

## 2) Lấy source code về máy

### Cách A: Clone bằng Git
```bash
git clone https://github.com/Benlaptrinh/QuanCaPhe
cd demo
```

### Cách B: Download ZIP
- Vào GitHub repo → Code → Download ZIP
- Giải nén ra → mở thư mục project

---

## 3) Tạo database rỗng trong MySQL

Quan trọng: chỉ cần tạo database rỗng.  
Hibernate sẽ tự tạo bảng từ Entity khi chạy lần đầu.

Mở MySQL Workbench hoặc command line và chạy:

```sql
CREATE DATABASE quancaphe;
```

Lưu ý: Tên database đang được cấu hình trong `application.properties`.  
Nếu bạn muốn đổi sang `quancaphe`, hãy đổi ở file cấu hình và tạo DB theo đúng tên mới.

---

## 4) Cấu hình kết nối database (application.properties)

Mở file:
```
src/main/resources/application.properties
```

Cập nhật đúng theo máy bạn:
```properties
# DATABASE
spring.datasource.url=jdbc:mysql://localhost:3306/tiemchung?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / HIBERNATE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# THYMELEAF
spring.thymeleaf.cache=false
```

Ghi chú:
- `ddl-auto=update`: tự tạo bảng nếu chưa có, và cập nhật nhẹ schema.
- Nếu muốn reset DB mỗi lần chạy: dùng `create-drop` (không khuyến nghị khi nộp).

---

## 5) Chạy ứng dụng

### 5.1 Chạy bằng IDE (IntelliJ)
1. Mở IntelliJ → Open → chọn thư mục project.
2. Chọn JDK 17 cho Project SDK.
3. Đợi IntelliJ tải dependencies.
4. Mở file `src/main/java/com/example/demo/DemoApplication.java`
5. Click Run 

Nếu chạy thành công, console sẽ có dòng kiểu:
```
Started DemoApplication in ... seconds
```

### 5.2 Chạy bằng terminal (không cần IDE)
macOS/Linux:
```bash
./mvnw spring-boot:run
```

Windows:
```bat
mvnw.cmd spring-boot:run
```

---

## 6) Truy cập web

Mở trình duyệt:
- Trang chủ: `http://localhost:8080/`
- Trang login: `http://localhost:8080/login`

---

## 7) Đăng nhập — Tài khoản mặc định (Seed Admin)

Trong project có seed admin tại:
`DemoApplication.java` (CommandLineRunner)

Tài khoản mặc định:
- Username: `admin`
- Password: `admin123`
- Role: `ADMIN`

Seed chỉ chạy khi:
- DB chưa có user role ADMIN
- Ứng dụng chạy lên thành công

Bạn có thể thấy log:
```
Seeded admin/admin123
```

Sau khi login admin, bạn có thể tạo tài khoản staff (role `NHANVIEN`).

Trang staff:
- `http://localhost:8080/staff/home`

---

## 8) Dữ liệu ban đầu

- Không cần import schema SQL.
- Nếu muốn data demo (bàn, thực đơn, nhân viên...):
  - Cách A: tạo thủ công trong giao diện Admin
  - Cách B: tạo file `data.sql` để auto insert (chỉ làm khi thầy yêu cầu)

---

## 9) Lỗi thường gặp & cách xử lý nhanh

1) **Access denied for user**  
→ Sai username/password trong `application.properties`.

2) **Unknown database**  
→ Chưa tạo DB hoặc sai tên DB.

3) **Port 8080 bị chiếm**  
→ Thêm vào `application.properties`:
```properties
server.port=8081
```
Sau đó vào `http://localhost:8081/`.

4) **Login không được**  
→ Kiểm tra console có dòng `Seeded admin/admin123` không.  
Nếu DB đã có user cũ sai password, xoá record đó và chạy lại.

---

## 10) Build nhanh để kiểm tra compile OK (tuỳ chọn)

Không chạy test (nhanh hơn):
```bash
./mvnw -DskipTests package
```

Sau khi build thành công sẽ có file `.jar` trong `target/`.

---

## 11) Link tải phần mềm (nếu thầy yêu cầu đính kèm)

Không nên upload trực tiếp file cài đặt (nặng + không cần).  
Dùng link chính thức là chuẩn:

- JDK 17: https://adoptium.net/
- MySQL: https://dev.mysql.com/downloads/mysql/
- IntelliJ: https://www.jetbrains.com/idea/download/
- Eclipse: https://www.eclipse.org/downloads/
- VS Code: https://code.visualstudio.com/

---

## 12) Kết luận

Chỉ cần:
1) Tạo database rỗng  
2) Cấu hình `application.properties`  
3) Chạy app  
4) Login admin/admin123  

Là chạy được hệ thống.

---

## 13) Hướng dẫn sử dụng nhanh

1. Đăng nhập bằng tài khoản ADMIN (admin/admin123).
2. Vào menu Quản lý → Nhân viên để tạo nhân viên và tài khoản.
3. Vào Bán hàng để tạo hóa đơn, thêm món, thanh toán.
4. Vào Thống kê – Báo cáo để xem doanh thu, chi tiêu, bán theo ngày.
5. Nhân viên đăng nhập chỉ thao tác các chức năng được phân quyền.
