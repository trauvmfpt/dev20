FAQ

1. Bắt đầu từ đâu?
  - Vào app>src>main>java>com>example>dev20>MainActivity.java và đọc
  
2. Activity là gì?
  - Gần như controller trong mô hình MVC, để xử lí các hoạt động dưới giao diện của người dùng
  - VD: người dùng bấm vào nút trên giao diện, activity sẽ xử lí hoạt động bấm nút đấy qua hàm setOnClickListener
3. View ở đâu?
  - app>src>main>res>layout
  - mỗi activity có 1 view tương ứng (ngoại lệ có thể có nhiều view nhỏ để hiển thị danh sách khi dùng listview hay recycleview)
4. Config là gì? Services là gì?
  - Không cần quan tâm. Config để hứng data, còn Services để tuỳ chỉnh hoạt động khi nhận được tin nhắn từ Firebase Messaging.
5. Dev20.java là gì?
  - Em cũng không biết nữa. Đi cop từ đủ 7749 nguồn thì chỉ đến thế thôi
  
6. Nói qua về activity đi?
  - Vào activity 1 cái thì sẽ nhảy đến hàm onCreate. Hàm này có nhiệm vụ show ra view và (như mình toàn làm) xử lí các tác vụ trên cái view ấy như là người dùng bấm nút hay gửi dữ liệu lên database. Có thể chuyển hướng sang activity khác qua hàm startActivity(intent).
7. Intent là gì?
  - Nói chung là để chuyển từ activity này sang activity khác, hay chuyển từ giao diện này sang giao diện khác đối với người dùng.
8. Dùng GoogleMaps API với Firebase như thế nào?
  - C1: Đọc doc. C2: Xem youtube. C3: Hỏi thầy (và bị ăn chửi)
  - Trong project này em mới khởi tạo các thứ rất cơ bản của 2 service này, đọc qua thì cũng hiểu được chút nhưng muốn dùng thêm các chức năng khác thì phải tra thêm, như là tạo đường từ 2 điểm trên bản đồ ntn, vv..vv..

9. Mệt quá không đọc nữa thì sao?
  - Trượt môn nhé :D
10. Đọc mãi không hiểu thì sao?
  - Lên youtube gõ android google maps tutorial và xem. Không thì hỏi thầy.
