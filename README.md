1. GIỚI THIỆU PHẦN MỀM
Ứng dụng TradeUp là một nền tảng di động được thiết kế để kết nối người mua và người bán, cho phép họ dễ dàng mua bán các mặt hàng đã qua sử dụng tại địa phương. Ứng dụng hỗ trợ các tính năng chính như quản lý người dùng, đăng tải và quản lý sản phẩm, tìm kiếm và duyệt sản phẩm nâng cao, giao tiếp an toàn trong ứng dụng, quản lý giao dịch và đề nghị mua, hệ thống đánh giá, và tích hợp thanh toán.
2. KIẾN TRÚC HỆ THỐNG
1.	CƠ SỞ DỮ LIỆU: MySQL
2.	BACKEND: SPRINGBOOT
3.	FRONTEND: ANDROID STUDIO
Các thành phần chính của ứng dụng Android:
•	LoginActivity: Quản lý đăng nhập người dùng, bao gồm đăng nhập bằng Google và sử dụng JWT + Refresh token để xác thực.
•	HomeActivity: Hiển thị danh sách sản phẩm, hỗ trợ các tùy chọn tìm kiếm và lọc nâng cao (filter, khoảng cách, tình trạng, sắp xếp).
•	ProductDetailActivity: Cho phép người dùng xem chi tiết sản phẩm và gửi đề nghị mua hàng.
•	ChatActivity: Hỗ trợ tính năng nhắn tin trực tiếp giữa người mua và người bán, bao gồm gửi tin nhắn văn bản, ảnh và emoji.
•	ChatListActivity: Hiển thị danh sách các cuộc hội thoại gần đây của người dùng.
•	TransactionHistoryActivity: Trình bày lịch sử các giao dịch đã thực hiện.
Thư viện sử dụng:
•  Retrofit
•  Glidex
•  Google Sign-In (OAuth2)
•  WebSocket: Được sử dụng để thiết lập kết nối song công cho các tính năng yêu cầu cập nhật thời gian thực như chat và vị trí.
•  Firebase: Được tích hợp để hỗ trợ các tính năng thời gian thực, đặc biệt là Firebase Cloud Messaging cho thông báo đẩy, và quản lý dữ liệu.

3. CÁC TÍNH NĂNG CHÍNH
Ứng dụng TradeUp cung cấp các tính năng cốt lõi sau:
•	Quản lý người dùng:
o	Đăng ký & Đăng nhập: Người dùng có thể đăng ký bằng email/mật khẩu (có xác thực định dạng) hoặc thông qua Google Sign-In. Yêu cầu xác minh email để kích hoạt tài khoản và hỗ trợ khôi phục mật khẩu qua email. Nút đăng nhập sẽ bị vô hiệu hóa cho đến khi các trường được điền đầy đủ và chính xác. Tùy chọn đăng xuất có thể truy cập qua hồ sơ/cài đặt.
o	Hồ sơ người dùng: Hồ sơ bao gồm tên hiển thị, ảnh đại diện, tiểu sử, thông tin liên hệ và xếp hạng. Người dùng có thể cập nhật hồ sơ và ảnh đại diện, cũng như tùy chọn hủy kích hoạt hoặc xóa vĩnh viễn tài khoản (yêu cầu xác nhận). Cho phép xem hồ sơ công khai của người dùng khác.
•	Đăng tải và Quản lý sản phẩm:
o	Thêm sản phẩm: Các trường bắt buộc bao gồm tiêu đề, mô tả, giá, danh mục, tình trạng, địa điểm và ít nhất 1 ảnh. Các trường tùy chọn: hành vi mặt hàng, thẻ bổ sung. Hỗ trợ tự động điền địa điểm bằng GPS (có kiểm tra quyền) và tải lên tối đa 10 ảnh (JPEG/PNG). Người dùng có thể xem trước sản phẩm trước khi đăng.
o	Quản lý sản phẩm đã đăng: Người dùng có thể xem, chỉnh sửa hoặc xóa các sản phẩm đã đăng từ bảng điều khiển cá nhân. Các tùy chọn trạng thái sản phẩm: Có sẵn, Đã bán, Tạm dừng. Cung cấp phân tích sản phẩm (lượt xem, tương tác).
•	Duyệt và Tìm kiếm:
o	Tìm kiếm: Cho phép tìm kiếm sản phẩm theo từ khóa, danh mục, khoảng giá, tình trạng và khoảng cách (với GPS hoặc nhập thủ công). Chức năng tìm kiếm kích hoạt 200ms sau khi bắt đầu nhập. Các tùy chọn sắp xếp: Mức độ liên quan, Mới nhất, Giá (tăng/giảm). Người dùng có thể tìm kiếm sản phẩm theo bán kính địa điểm (ví dụ: 5-100 km) và ghi đè GPS để nhập địa điểm tùy chỉnh.
o	Đề xuất: Các mặt hàng được tổ chức theo danh mục. Cung cấp các đề xuất được cá nhân hóa dựa trên lịch sử duyệt web của người dùng, mức độ phổ biến và các mặt hàng gần đó.
•	Giao tiếp:
o	Nhắn tin trong ứng dụng: Cung cấp tính năng trò chuyện an toàn giữa người mua và người bán. Hỗ trợ tin nhắn văn bản, emoji và chia sẻ ảnh tùy chọn. Người dùng có thể chặn/báo cáo các cuộc trò chuyện không phù hợp.
o	Thông báo: Gửi thông báo đẩy cho các sự kiện như tin nhắn mới, đề nghị giá, cập nhật danh sách sản phẩm và các chương trình khuyến mãi (tùy chọn).
•	Đề nghị & Giao dịch:
o	Đề nghị: Người mua có thể đưa ra đề nghị nếu giá sản phẩm có thể thương lượng. Người bán có thể chấp nhận/từ chối/đề xuất lại đề nghị.
o	Giao dịch: Người bán có thể đánh dấu mặt hàng là "Đã bán". Các mặt hàng đã bán được lưu trữ trong lịch sử giao dịch của người dùng.
•	Dịch vụ vị trí:
o	Người dùng có thể tìm kiếm sản phẩm theo bán kính địa điểm (ví dụ: 5-100 km).
o	Người dùng có thể ghi đè GPS và nhập địa điểm tùy chỉnh.
•	Đánh giá & Nhận xét:
o	Phản hồi: Người dùng có thể đánh giá lẫn nhau (1-5 sao) sau các giao dịch. Hỗ trợ phản hồi bằng văn bản tùy chọn.
o	Uy tín: Hồ sơ người dùng hiển thị xếp hạng trung bình và tổng số giao dịch. Các đánh giá được kiểm duyệt để loại bỏ nội dung lạm dụng.
•	Phân tích & Lịch sử:
o	Người bán: Theo dõi lượt xem, cuộc trò chuyện và đề nghị cho mỗi sản phẩm đã đăng. Cung cấp "Lịch sử bán hàng" cho các mặt hàng đã bán.
o	Người mua: Xem các mặt hàng đã lưu, lịch sử đề nghị và lịch sử mua hàng.
•	Thanh toán:
o	Cổng thanh toán: Hỗ trợ thanh toán trong ứng dụng qua thẻ tín dụng/ghi nợ và UPI hoặc Ví điện tử (nếu được hỗ trợ theo khu vực). Lịch sử thanh toán hiển thị trong hồ sơ người dùng.
