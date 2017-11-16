var http = require('http');
var mysql = require('mysql');

//Tạo kết nối tới CSDL Mysql
var con = mysql.createConnection({
	host: "localhost",
	user: "root",
	password: "",
	database: "gamebaucua"
});

//Thực hiện kết nối CSDL
con.connect(function(err) {
	if (err == null) 
		console.log('Ket noi CSDL thanh cong');
	else
		console.log(err+'');
});

var server = http.createServer(function (req, res) {
});

var io = require('socket.io')(server);

//Server lắng nghe port 2000
server.listen(2000);

//Khi có client kết nối tới socketio của server
io.on('connection', function (socket) {
	console.log(socket.id + ' ket noi');
	//Khi client ngắt kết nối
	socket.on('disconnect', function () {
		console.log(socket.id + ' ngat ket noi');
	});

	//Lắng nghe sự kiện đăng ký tài khoản từ client
	socket.on('sign-up', function (data){
		let username = data.username;
		let password = data.password;
		let sql = "SELECT * FROM `taikhoan` WHERE TenTaiKhoan = '"+username+"'";
		con.query(sql, function (err, result) {
			if (err == null) {
				if (result.length > 0)
					socket.emit('sign-up-fail', {error:'Tài khoản đã tồn tại'});
				else {
					let sql = "INSERT INTO `taikhoan` (`TenTaiKhoan`, `MatKhau`, `Tien`) VALUES ('"+username+"', '"+password+"', '500000')";
					con.query(sql, function (err, result) {
						if (err == null)
							socket.emit('sign-up-success', {success: 'Đăng ký thành công'});
						else {
							socket.emit('sign-up-fail', {error:'Lỗi đăng ký, vui lòng thử lại sau'});
							console.log(err+'');
						}
					});
				}
			}
			else {
				socket.emit('sign-up-fail', {error:'Lỗi đăng ký, vui lòng thử lại sau'});
				console.log(err+'');
			}
		});
	});

	//Lắng nghe sự kiện đăng nhập tài khoản từ client
	socket.on('sign-in', function (data){
		let username = data.username;
		let password = data.password;
		let sql = "SELECT * FROM taikhoan WHERE TenTaiKhoan = '"+username+"' AND MatKhau = '"+password+"'";
		con.query(sql, function (err, result) {
			if (err == null) {
				if (result.length > 0)
					socket.emit('sign-in-success');
				else 
					socket.emit('sign-in-fail', {error: 'Tên tài khoản hoặc mật khẩu không đúng'});
			}
			else {
				socket.emit('sign-in-fail', {error:'Không thể đăng nhập lúc này, vui lòng thử lại sau'});
				console.log(err+'');
			}
		});
	});
});