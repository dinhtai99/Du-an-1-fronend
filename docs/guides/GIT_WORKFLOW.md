# Hướng dẫn làm việc nhóm với Git/GitHub

## 📋 Mục lục
1. [Thiết lập ban đầu](#thiết-lập-ban-đầu)
2. [Quy trình làm việc hàng ngày](#quy-trình-làm-việc-hàng-ngày)
3. [Xử lý conflict](#xử-lý-conflict)
4. [Best practices](#best-practices)

## 🚀 Thiết lập ban đầu

### 1. Clone repository
```bash
git clone https://github.com/dinhtai99/Du-an-1-fronend.git
cd Du-an-1-fronend
```

### 2. Cấu hình Git (nếu chưa có)
```bash
git config --global user.name "Tên của bạn"
git config --global user.email "email@example.com"
```

### 3. Kiểm tra các branch hiện có
```bash
git branch -a
```

## 📝 Quy trình làm việc hàng ngày

### Bước 1: Cập nhật code mới nhất
**LUÔN làm điều này trước khi bắt đầu làm việc!**

```bash
# Chuyển sang branch main
git checkout main

# Lấy code mới nhất từ GitHub
git pull origin main

# Tạo branch mới cho feature của bạn (hoặc chuyển sang branch đã có)
git checkout -b feature/ten-tinh-nang-cua-ban
# Hoặc nếu branch đã tồn tại:
# git checkout feature/ten-tinh-nang-cua-ban
# git merge main  # Cập nhật branch của bạn với code mới nhất
```

### Bước 2: Làm việc trên code
- Code, test, commit thường xuyên
- Mỗi commit nên là một thay đổi logic hoàn chỉnh

### Bước 3: Commit code
```bash
# Xem những file đã thay đổi
git status

# Thêm file vào staging area
git add .
# Hoặc thêm từng file cụ thể:
# git add app/src/main/java/.../YourFile.java

# Commit với message rõ ràng
git commit -m "Thêm tính năng: Mô tả ngắn gọn những gì đã làm"
```

**Lưu ý về commit message:**
- ✅ Tốt: `"Thêm tính năng đánh giá sản phẩm"`
- ✅ Tốt: `"Sửa lỗi crash khi thanh toán"`
- ❌ Không tốt: `"update"`
- ❌ Không tốt: `"fix"`

### Bước 4: Push code lên GitHub
```bash
# Push branch của bạn lên GitHub
git push origin feature/ten-tinh-nang-cua-ban
```

### Bước 5: Tạo Pull Request (PR)
1. Vào GitHub: https://github.com/dinhtai99/Du-an-1-fronend
2. Click "Pull requests" → "New pull request"
3. Chọn branch của bạn → base branch: `main`
4. Điền mô tả về những thay đổi
5. Tag các thành viên trong nhóm để review
6. Click "Create pull request"

### Bước 6: Sau khi PR được merge
```bash
# Quay lại branch main
git checkout main

# Cập nhật code mới nhất
git pull origin main

# Xóa branch cũ (tùy chọn)
git branch -d feature/ten-tinh-nang-cua-ban
```

## ⚠️ Xử lý conflict

### Khi có conflict khi merge/pull:

1. **Git sẽ báo conflict:**
```bash
Auto-merging app/src/main/java/.../File.java
CONFLICT (content): Merge conflict in app/src/main/java/.../File.java
```

2. **Mở file có conflict:**
- Tìm các dòng có `<<<<<<<`, `=======`, `>>>>>>>`
- Quyết định giữ code nào (hoặc kết hợp cả hai)
- Xóa các dòng conflict markers

3. **Sau khi sửa xong:**
```bash
# Thêm file đã sửa
git add app/src/main/java/.../File.java

# Tiếp tục merge
git commit -m "Resolve conflict in File.java"
```

## ✅ Best practices

### 1. Commit thường xuyên
- Commit mỗi khi hoàn thành một tính năng nhỏ
- Không commit code chưa test
- Không commit file tạm, file debug

### 2. Không commit file nhạy cảm
Các file này đã được thêm vào `.gitignore`:
- `local.properties`
- `.gradle/`
- `build/`
- API keys, passwords (nếu có)

### 3. Luôn pull trước khi push
```bash
git pull origin main
# Hoặc
git pull origin feature/ten-branch-cua-ban
```

### 4. Tạo branch cho mỗi feature
- `feature/ten-tinh-nang` - Tính năng mới
- `fix/ten-loi` - Sửa lỗi
- `refactor/ten-module` - Refactor code

### 5. Review code trước khi merge
- Luôn có ít nhất 1 người review PR trước khi merge
- Sửa các comment từ reviewer

### 6. Cấu hình BASE_URL
Mỗi người cần cập nhật IP trong `ApiClient.java`:
- **Emulator:** `http://10.0.2.2:3000/`
- **Thiết bị thật:** `http://IP-CUA-BAN:3000/`

## 🔧 Các lệnh Git hữu ích

```bash
# Xem lịch sử commit
git log --oneline -10

# Xem thay đổi trong file
git diff

# Xem thay đổi của một commit cụ thể
git show <commit-hash>

# Hoàn tác commit (chưa push)
git reset --soft HEAD~1

# Xem branch hiện tại
git branch

# Xem remote repository
git remote -v

# Stash thay đổi tạm thời (khi cần chuyển branch gấp)
git stash
git stash pop  # Lấy lại thay đổi
```

## 📞 Liên hệ

Nếu gặp vấn đề với Git, liên hệ các thành viên trong nhóm để được hỗ trợ.

