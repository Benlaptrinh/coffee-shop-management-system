# QuanCaPhe Pro

[![CI](https://img.shields.io/github/actions/workflow/status/Benlaptrinh/QuanCaphePro/ci.yml?branch=main&label=CI)](https://github.com/Benlaptrinh/QuanCaphePro/actions/workflows/ci.yml)
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.5.9-6DB33F?logo=springboot&logoColor=white)](./Backend)
[![Frontend](https://img.shields.io/badge/frontend-React%20%2B%20Vite-646CFF?logo=vite&logoColor=white)](./frontend)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Node](https://img.shields.io/badge/Node.js-20-339933?logo=node.js&logoColor=white)](https://nodejs.org/)

Hệ thống quản lý quán cà phê fullstack gồm:
- **Backend API:** Spring Boot + Spring Security + JPA + MySQL
- **Frontend Web:** React + TypeScript + Vite

Ứng dụng hỗ trợ quản lý bán hàng theo bàn, nhân sự, thực đơn, kho hàng, khuyến mãi, ngân sách và báo cáo vận hành.

## Mục lục

1. [Tính năng chính](#tính-năng-chính)
2. [Kiến trúc](#kiến-trúc)
3. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
4. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
5. [Yêu cầu môi trường](#yêu-cầu-môi-trường)
6. [Chạy local nhanh](#chạy-local-nhanh)
7. [Biến môi trường quan trọng](#biến-môi-trường-quan-trọng)
8. [Kiểm thử và CI](#kiểm-thử-và-ci)
9. [API và tài liệu liên quan](#api-và-tài-liệu-liên-quan)
10. [Đóng góp](#đóng-góp)

## Tính năng chính

- Xác thực và phân quyền người dùng (JWT, vai trò Admin/Staff)
- Quản lý nhân viên, chức vụ, tài khoản
- Quản lý thực đơn, khuyến mãi
- Quản lý kho hàng, đơn nhập/xuất, đơn vị tính
- Nghiệp vụ bán hàng tại quầy theo bàn:
  - thêm món, thanh toán
  - đặt bàn/hủy đặt bàn
  - chuyển bàn, gộp bàn, tách bàn
  - in/xem hóa đơn
- Báo cáo tài chính, doanh thu và nhân sự
- Tích hợp dịch vụ ngoài: OAuth2, Cloudinary, PayPal, Email

## Kiến trúc

```text
[React + Vite Frontend]  <----HTTP/JSON---->  [Spring Boot REST API]  <---->  [MySQL]
                                                             |
                                                             +----> [Redis Cache]
                                                             +----> [Cloudinary / PayPal / OAuth2 / Email]
```

- Frontend gọi API qua `VITE_API_BASE_URL` (mặc định `http://localhost:8080/api`)
- Backend dùng profile mặc định `dev`, có profile `test` cho CI

## Công nghệ sử dụng

| Nhóm | Công nghệ |
|---|---|
| Backend | Java 17, Spring Boot 3.5.9, Spring Web, Spring Security, Spring Data JPA, Flyway |
| Frontend | React 19, TypeScript, Vite, React Router, React Query, Axios, Tailwind CSS |
| Database/Cache | MySQL, Redis, H2 (test) |
| Tích hợp | OAuth2 (Google/GitHub), Cloudinary, PayPal, SMTP Email |
| DevOps | GitHub Actions (`backend-tests`, `frontend-build`) |

## Cấu trúc thư mục

```text
.
├── Backend/                # Spring Boot REST API
│   ├── src/main/java/...   # Controller, Service, Repository, Entity, Security
│   ├── src/main/resources/ # application-*.properties, templates, log config
│   └── pom.xml
├── frontend/               # React + TypeScript + Vite app
│   ├── src/
│   └── package.json
└── .github/workflows/ci.yml
```

## Yêu cầu môi trường

- Java 17+
- Node.js 20+ và npm
- MySQL 8+
- Redis 6+

## Chạy local nhanh

### 1) Clone dự án

```bash
git clone https://github.com/Benlaptrinh/QuanCaphePro.git
cd QuanCaphePro
```

### 2) Chạy Backend

```bash
cd Backend
./mvnw spring-boot:run
```

Backend mặc định chạy tại: `http://localhost:8080`

### 3) Chạy Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend mặc định chạy tại: `http://localhost:5173`

## Biến môi trường quan trọng

### Backend

- `SPRING_PROFILES_ACTIVE` (mặc định: `dev`)
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_SECONDS`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `CORS_ALLOWED_ORIGINS`

Ví dụ tối thiểu để chạy local:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL=jdbc:mysql://localhost:3306/tiemchung
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=replace_with_a_strong_secret
```

### Frontend

Tạo file `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Kiểm thử và CI

### Chạy test backend

```bash
cd Backend
./mvnw test
```

### Build frontend

```bash
cd frontend
npm ci
npm run build
```

GitHub Actions hiện cấu hình:
- `backend-tests`: chạy `./mvnw -q test` với profile `test`
- `frontend-build`: chạy `npm ci` và `npm run build`

## API và tài liệu liên quan

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Tài liệu backend chi tiết: [`Backend/README.md`](./Backend/README.md)
- Hướng dẫn cài đặt backend: [`Backend/INSTALL.md`](./Backend/INSTALL.md)

## Đóng góp

1. Tạo branch mới từ `main`
2. Commit theo từng thay đổi nhỏ, rõ ràng
3. Chạy test/build trước khi tạo PR
4. Mô tả phạm vi thay đổi và ảnh hưởng trong PR

---

Nếu bạn thấy README này còn thiếu phần nào (demo GIF, ảnh màn hình, kiến trúc C4, API examples), có thể mở issue hoặc PR để bổ sung.
