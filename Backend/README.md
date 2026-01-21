# QuanCaPhe Pro — Backend (REST API)

Backend Spring Boot chỉ cung cấp REST API. Giao diện web nằm ở `frontend/` (React + Vite). Dự án đã bỏ hoàn toàn Thymeleaf.

---

## 1. Tóm tắt dự án

- **Mục tiêu:** API phục vụ quản lý quán cà phê (bán hàng theo bàn, nhân viên, thực đơn, thiết bị, kho, marketing, ngân sách, báo cáo).
- **Kiến trúc:** REST API + JWT.
- **Frontend:** React + TypeScript trong thư mục `frontend/`.

---

## 2. Tech stack

- **Java:** 17 (LTS)
- **Spring Boot:** 3.5.x (xem `pom.xml`)
- **Spring Web** (REST)
- **Spring Data JPA** (Hibernate)
- **Spring Security** (JWT, stateless)
- **Validation:** `spring-boot-starter-validation`
- **Database:** MySQL
- **Build:** Maven (`./mvnw`)
- **OpenAPI:** springdoc (`/swagger-ui`)
- **Logging:** Logback (rolling file)

---

## 3. Cấu trúc package

```
src/main/java/com/example/demo
├─ controller/              # REST controllers (/api/**)
├─ report/                  # Module báo cáo (API)
│  ├─ controller/
│  ├─ dto/
│  ├─ repository/
│  └─ service/
├─ service/                 # Business logic
├─ repository/              # JPA repositories
├─ entity/                  # JPA entities
├─ dto/                     # DTO/Form
└─ security/                # JWT + security config
```

---

## 4. Chạy local — Backend

**Yêu cầu:**
- Java 17
- MySQL

**Cấu hình DB & JWT:** `Backend/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tiemchung
spring.datasource.username=root
spring.datasource.password=123456

jwt.secret=changeit_replace_this_with_secure_value
jwt.expiration.seconds=3600
```

**Chạy:**

```bash
./mvnw -DskipTests package
./mvnw spring-boot:run
```

**API base:** `http://localhost:8080/api`

**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## 5. Chạy local — Frontend

```bash
cd frontend
npm install
npm run dev
```

**ENV (tùy chọn):**

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

---

## 6. Auth (JWT)

**Login**

```
POST /api/auth/login
{
  "username": "...",
  "password": "..."
}
```

Response trả về `token`. Các request sau dùng header:

```
Authorization: Bearer <token>
```

---

## 7. Logging (Logback)

Backend dùng Logback với file rolling. File cấu hình: `Backend/src/main/resources/logback-spring.xml`.

Mặc định log ghi ra:

```
logs/app.log
```

Có thể chỉnh level qua `application.properties`:

```properties
logging.level.root=INFO
logging.level.com.example.demo=DEBUG
```

---

## 8. Notes

- Backend chỉ phục vụ API, không còn server-rendered HTML.
- `ddl-auto=update` chỉ dùng cho dev. Production nên dùng Flyway/Liquibase.

- App fail startup với `Query validation failed` ở repository:
  - Kiểm tra JPQL có `function('date', ...)` hoặc constructor expression dùng function DB-specific. Nên chuyển sang native SQL.

- Tổng bị `null` hoặc lỗi parse template:
  - Đảm bảo model luôn trả list (không null) và dùng `#aggregates.sum(list.![field])`.

---

## 10. Checklist khi PR

- Chạy `./mvnw -DskipTests package` và test UI cơ bản.  
- Change scope gọn (1 feature/PR).  
- Thêm unit test khi sửa logic nghiệp vụ.  
- Nếu đổi schema, thêm migration ở `src/main/resources/db/migration`.

---

Nếu bạn muốn, mình có thể:
- Thêm `scripts/` với SQL seed nhanh.  
- Viết 2 unit test mẫu cho báo cáo bán hàng.  
- Tách template report thành các fragment nhỏ.

Hãy nói phần bạn muốn làm tiếp và mình sẽ triển khai.

---

## 11. Báo cáo Responsive UI

### 11.1 Mục tiêu
- Giao diện dùng tốt trên mobile/tablet, không vỡ layout.
- Menu thu gọn bằng nút, dễ thao tác một tay.
- Form, bảng, modal không bị tràn màn hình.

### 11.2 Phạm vi
- Admin + Staff UI (server-rendered Thymeleaf).
- Các trang chính: Profile, Nhân viên, Thực đơn, Thiết bị, Kho, Marketing, Bán hàng, Báo cáo.

### 11.3 Breakpoints
- Mobile chính: `<= 720px`
- Sidebar toggle: `<= 768px`

### 11.4 Thay đổi chính (tóm tắt)
- **Layout**: thêm nút toggle sidebar + overlay (mobile).
- **Form**: chuyển 2 cột -> 1 cột, input full width.
- **Table**:
  - Bảng có hành động bọc trong `.table-scroll` để cuộn ngang.
  - Cột “Hành động” sticky khi cuộn ngang (class `.action-col`).
  - Bảng ngân sách dùng style `report-table` để full width như báo cáo.
- **Sales modal**: chuẩn hóa khung modal (`.modal-card`) để co giãn đẹp trên mobile.
- **Bố cục thẻ**: `table-grid` 2 cột cho danh sách bàn trên mobile.

### 11.5 Các file liên quan
- CSS: `src/main/resources/static/css/admin.css`, `src/main/resources/static/css/responsive.css`
- Layout: `src/main/resources/templates/layout/base.html`
- Các trang đã chỉnh:
  - `src/main/resources/templates/admin/menu.html`
  - `src/main/resources/templates/admin/equipment.html`
  - `src/main/resources/templates/admin/marketing/list.html`
  - `src/main/resources/templates/admin/employees_list.html`
  - `src/main/resources/templates/admin/budget.html`
  - `src/main/resources/templates/admin/report/index.html`
  - `src/main/resources/templates/sales/fragments/*.html`

### 11.6 Checklist test nhanh (mobile)
1. Mở trang bất kỳ -> bấm nút **Menu** -> sidebar hiện/ẩn có overlay.
2. Trang danh sách (menu/nhân viên/thiết bị/khuyến mãi): bảng cuộn ngang, nút Sửa/Xóa luôn thấy.
3. Trang bán hàng: danh sách bàn hiển thị 2 cột; modal xem bàn/chọn món/tách/gộp/đặt bàn hiển thị gọn.
4. Trang báo cáo + ngân sách: form & bảng full width, không bị thu nhỏ.

### 11.7 Chỗ dán ảnh (tự thêm)
> Gợi ý: lưu ảnh vào `docs/screenshots/` rồi dán link tương đối.

- Mobile – Profile view  
  `![profile-mobile](docs/screenshots/01-profile-mobile.png)`
- Mobile – Employees list  
  `![employees-mobile](docs/screenshots/02-employees-mobile.png)`
- Mobile – Menu list (table actions)  
  `![menu-mobile](docs/screenshots/03-menu-mobile.png)`
- Mobile – Equipment list  
  `![equipment-mobile](docs/screenshots/04-equipment-mobile.png)`
- Mobile – Sales table grid  
  `![sales-mobile](docs/screenshots/05-sales-mobile.png)`
- Mobile – Report  
  `![report-mobile](docs/screenshots/06-report-mobile.png)`
