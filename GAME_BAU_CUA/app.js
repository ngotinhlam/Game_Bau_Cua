var http = require('http');
var mysql = require('mysql');

var con = mysql.createConnection({
	host: "localhost",
	user: "root",
	password: "",
	database: "gamebaucua"
});

con.connect(function(err) {
	if (err == null) 
		console.log('Ket noi CSDL thanh cong');
	else
		console.log(err+'');
});

var server = http.createServer(function (req, res) {
});

var io = require('socket.io')(server);
server.listen(2000);

io.on('connection', function (socket) {
	console.log(socket.id);

	socket.on('sign-up', function (data){
		let username = data.username;
		let password = data.password;
		let sql = "SELECT * FROM `taikhoan` WHERE TenTaiKhoan = '"+username+"' AND MatKhau = '"+password+"'";
		con.query(sql, function (err, result) {
			if (err == null) {
				if (result.length > 0)
					socket.emit('sign-up-fail', {err:'Tài khoản đã tồn tại'});
				else {
					let sql = "INSERT INTO `taikhoan` (`TenTaiKhoan`, `MatKhau`, `Tien`) VALUES ('"+username+"', '"+password+"', '500000')";
					con.query(sql, function (err, result) {
						if (err == null)
							socket.emit('sign-up-success', {success: 'Đăng ký thành công'});
						else {
							socket.emit('sign-up-fail', {err:'Lỗi đăng ký, vui lòng thử lại sau'});
							console.log(err+'');
						}
					});
				}
			}
			else {
				socket.emit('sign-up-fail', {err:'Lỗi đăng ký, vui lòng thử lại sau'});
				console.log(err+'');
			}
		});
	});
});