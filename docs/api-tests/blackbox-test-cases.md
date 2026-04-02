# QuanCaphePro - Blackbox API Test Freeze

## 1. Mục tiêu

Tài liệu này chốt spec test theo 2 mức để chạy regression ổn định:

- `Current behavior suite`: bám theo behavior backend hiện tại, dùng để chạy pass ổn định.
- `Expected/business suite`: behavior mong muốn theo nghiệp vụ, dùng để track backlog bug/fix.

## 2. Files cần dùng

- `docs/api-tests/QuanCaphePro-CurrentBehavior.postman_collection.json`
- `docs/api-tests/QuanCaphePro-ExpectedBusiness.postman_collection.json`
- `docs/api-tests/QuanCaphePro-Smoke-CurrentBehavior.postman_collection.json`
- `docs/api-tests/QuanCaphePro.local.postman_environment.json`

Collection gốc full coverage vẫn giữ lại:

- `docs/api-tests/QuanCaphePro-Blackbox.postman_collection.json`

## 3. Các chênh lệch đã đóng băng theo code hiện tại

Các case dưới đây đã tách rõ ở 2 suite để tránh fail giả:

- `DELETE /api/donvitinh/{id}` với `id` không tồn tại:
  - Current: `204`
  - Expected/business: `404`
- `DELETE /api/nhanvien/{id}` với `id` không tồn tại:
  - Current: `204`
  - Expected/business: `404`
- `POST /api/thucdon` với `giaHienTai <= 0`:
  - Current: đang cho qua `201`
  - Expected/business: nên `400`

Ngoài ra nhóm `HangHoa` có một số nhánh đang ném `RuntimeException` (không map riêng) nên current suite cho phép `500` ở nhánh lỗi tương ứng.

## 4. Thứ tự chạy chuẩn

1. `01 - Auth` (lấy `token_admin`, `token_staff`)
2. `02 -> 12`
3. `13 - Sales`
4. `14 - Report`
5. `15 - Cloudinary Upload`
6. `16 - PayPal`

## 5. Preflight environment trước khi chạy

Cần xác nhận các biến ID tồn tại trong local data trước khi run full:

- `user_id`, `nhanvien_id`, `chucvu_id`, `donvitinh_id`, `thucdon_id`, `thietbi_id`, `khuyenmai_id`, `hanghoa_id`
- `donnhap_id`, `donxuat_id`, `invoice_id`
- `table_using_id`, `table_empty_id`, `table_merge_source_id`
- `upload_file_path`, `cloudinary_public_id`
- `paypal_order_id`, `paypal_capture_id`
- `missing_id` (dùng cho test not-found)

Nếu ID không tồn tại, fail được phân loại là `test data/environment` thay vì bug API.

## 6. Smoke trước full

Smoke collection:

- `docs/api-tests/QuanCaphePro-Smoke-CurrentBehavior.postman_collection.json`

Smoke scope:

- Auth: login admin/staff
- Users: get me, list users admin
- ThucDon: list + create
- Sales: list tables + add item + pay
- Report: finance admin

Khi smoke pass mới chạy full current suite.

## 7. Phân loại fail khi chạy

Mỗi fail phải gắn ngay một loại:

1. `data/env fail`: thiếu seed, ID sai, token sai, credential ngoài hệ thống (Cloudinary/PayPal)
2. `expected mismatch`: testcase assert khác behavior hiện tại
3. `real bug`: behavior sai dù precondition/data đúng

## 8. Newman commands

### 8.1 Smoke

```bash
npx --yes newman run docs/api-tests/QuanCaphePro-Smoke-CurrentBehavior.postman_collection.json \
  -e docs/api-tests/QuanCaphePro.local.postman_environment.json
```

### 8.2 Full current behavior regression

```bash
npx --yes newman run docs/api-tests/QuanCaphePro-CurrentBehavior.postman_collection.json \
  -e docs/api-tests/QuanCaphePro.local.postman_environment.json
```

### 8.3 Business expectation/backlog run

```bash
npx --yes newman run docs/api-tests/QuanCaphePro-ExpectedBusiness.postman_collection.json \
  -e docs/api-tests/QuanCaphePro.local.postman_environment.json
```
