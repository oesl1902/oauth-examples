/*
 *@author casern@bancolombia.com.co
 *@date: 17 febrero 2019
 *@description: servidor de la aplicacion
*/

var express = require('express');
var app = express();
var Client = require('node-rest-client').Client;
const utf8 = require('utf8');

//Aqui se realiza la declaracion de las vistas, para llamarlas desde el html
var fs = require('fs');
var indexhtml = fs.readFileSync('./index.html');
var otpdatahtml = fs.readFileSync('./otpdata.html');
var credentialsdatahtml = fs.readFileSync('./credentialsdata.html');
var passdatahtml = fs.readFileSync('./passdata.html');
 
var client = new Client();

//Todos los datos que se requieren en los diferentes formularios de los flujos OAuth2
var username = "";
var password = "";
var client_id = "";
var client_secret = "";
var catalog = "";
var redirect_uri = "";
var scope = "";
var response_type = "";
var code = "";
var encodedauth = "";

app.use(express.static('.'));

app.use(function (req, res, next) {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS, PUT, PATCH, DELETE');
    res.setHeader('Access-Control-Allow-Headers', 'X-Requested-With,content-type');
    res.setHeader('Access-Control-Allow-Credentials', true);
    next();
});

//Diferentes metodos para mostrar las vistas
app.get('/', function (req, res) {
    res.write(indexhtml);
});

app.get('/otpdata', function (req, res) {
    res.write(otpdatahtml);
	res.end();
}); 

app.get('/credentialsdata', function (req, res) {
    res.write(credentialsdatahtml);
	res.end();
});

app.get('/passdata', function (req, res) {
    res.write(passdatahtml);
	res.end();
}); 

//Metodo para guardar los datos que nos ingresan para los formularios, este se usa para los 3 flujos
app.get('/guardardatos', function(req, res) {
	username = req.query.username;
	password = req.query.password;
	client_id = req.query.client_id;
	client_secret = req.query.client_secret;
	catalog = req.query.catalog;
	redirect_uri = req.query.redirect_uri;
	scope = req.query.scope;
	response_type = req.query.response_type;
	
	//El client-id y client-secret se codigican a base64
	var authorization = client_id+":"+client_secret;
	encodedauth = new Buffer(authorization).toString('base64');
	res.send('Datos almacenados en server');
});

//Generacion token para flujo authorization code
app.get('/generartoken', function(req, res) {
	var args = {
		parameters: {
			grant_type: "authorization_code",
			code: code,
			redirect_uri: redirect_uri,
			scope: scope
		},
		headers: {
			"accept":"application/json",
			"apim-debug":"true",
			"content-type":"application/x-www-form-urlencoded",
			"authorization":"Basic "+encodedauth
		}
	};
	
	generarTokenOAuth(req, res, args);
});

//Generacion token para flujo aplicacion
app.get('/generartokenapp', function(req, res) {
	var args = {
		parameters: {
			grant_type: 'client_credentials',			
			scope: scope
		},
		headers: {
			"accept":"application/json",
			"apim-debug":"true",
			"content-type":"application/x-www-form-urlencoded",
			"authorization":"Basic "+encodedauth
		}
	};
	
	generarTokenOAuth(req, res, args);
});

//Generacion token para flujo password
app.get('/generartokenpass', function(req, res) {
	var args = {
		parameters: {
			grant_type: 'client_credentials',
			scope: scope
		},
		headers: {
			"accept":"application/json",
			"apim-debug":"true",
			"content-type":"application/x-www-form-urlencoded",
			"authorization":"Basic "+encodedauth
		}
	};
	
	generarTokenOAuth(req, res, args);
});

//Funcion para realizar el post al servicio que genera el token para los 3 flujos OAuth2
function generarTokenOAuth(req, res, args){
	try{
		var urlPost = '';
		if(catalog == 'sandbox'){
			urlPost = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+catalog+'/hackathon/v1/security/oauth-otp/oauth2/token';
		}
		else{
			urlPost = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+catalog+'/security/oauth-otp/oauth2/authorize';			
		}		
		
		client.post(urlPost, args, function (data, response) {
			let json = JSON.stringify(data);
			console.log(json);
			res.send('<div> <style media="screen" type="text/css">.container-code{width:100%;margin-top:150px;text-align:center;position:relative;-webkit-text-size-adjust:100%;-ms-text-size-adjust:100%}.container-code>div>div:nth-child(1)>span{color:#df001e;font-weight:700;font-size:20px}p{width:800px;height:100px;display:inline-block;word-wrap:break-word}.oauth-button{text-decoration:none!important;border-radius:4px;color:#fff;border-color:#005296;cursor:pointer;background-image:none;border:0;padding:17px 28px;font-size:15px;margin:0 5px}.oauth-button-blue{background-color:#00418b;box-shadow:0 0 20px #00418b}</style> <div class="container-code"> <div> <div> <span>Token code</span> </div> <p>'+data.access_token+'</p> <form> <div> <a href="/" class="oauth-button oauth-button-blue"> Go to home</a> </div> </form> </div> </div> </div>');
		});
	}
	catch(e){
		console.log(e);
	}
}

//Metodo para obtener el access code que se necesita para generar el token en el flujo de authorization code
app.get('/redirect', function(req, res) {
	code = req.query.code;
	res.setHeader('content-type', 'text/html; charset=utf-8');
	res.send('<div><style media="screen" type="text/css">.container-code{height: 70vh;display: flex;align-items: center;justify-content: center;}.container-code > div{display: flex;flex-direction: column;align-items: center;margin: 0px 100px !important;}.container-code > div > div:nth-child(1) > span{color: #df001e;font-weight: 700;font-size:20px;}.container-code > div > div:nth-child(2) > p{text-align:center;}.container-code > div > div{margin: 5px 0px;}.container-code > div > form{margin: 15px 0px;}.oauth-button {text-decoration: none !important;border-radius: 4px;color: #fff;border-color: #005296;cursor: pointer;background-image: none;border: none;padding: 17px 28px;font-size: 15px;margin: 0px 5px;}.oauth-button-blue{background-color: #00418b;box-shadow: 0 0 20px #00418b;}</style><div class="container-code"><div><div><span>Authorization code</span></div><div><p>'+code+'</p></div><form action=/generartoken><button class="oauth-button oauth-button-blue">Generar Token</button></form></div></div></div>');
});

app.listen(8000, function() {
  console.log('Aplicación escuchando el puerto 8000!');
});