#!/bin/bash

# Script để merge branch tai-update vào main
# Giữ tất cả commit của cả 2 bên

echo "🔄 Đang chuyển sang branch main..."
git checkout main

echo "📥 Đang pull code mới nhất từ origin/main..."
git pull origin main

echo "🔀 Đang merge tai-update vào main..."
git merge tai-update --no-ff -m "Merge branch tai-update vào main"

# Kiểm tra conflict
if [ $? -ne 0 ]; then
    echo "⚠️  Có conflict! Đang giải quyết..."
    
    # Với các file conflict, ưu tiên code của main (người khác) trước
    # Sau đó merge với code của tai-update
    echo "📝 Đang giải quyết conflict tự động..."
    
    # List các file conflict
    git diff --name-only --diff-filter=U
    
    echo ""
    echo "❌ Cần giải quyết conflict thủ công cho các file trên"
    echo "💡 Hướng dẫn:"
    echo "   1. Mở từng file conflict"
    echo "   2. Tìm các dòng có <<<<<<< HEAD, =======, >>>>>>> tai-update"
    echo "   3. Giữ code của cả 2 bên hoặc chọn code phù hợp"
    echo "   4. Xóa các dòng conflict markers"
    echo "   5. Sau khi sửa xong, chạy: git add ."
    echo "   6. Tiếp tục: git commit"
    echo ""
    echo "Hoặc chạy lệnh sau để xem danh sách file conflict:"
    echo "   git diff --name-only --diff-filter=U"
else
    echo "✅ Merge thành công!"
    echo "📤 Đang push lên origin/main..."
    git push origin main
fi

