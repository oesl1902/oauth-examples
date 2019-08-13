/*
 *@author casern@bancolombia.com.co
 *@date: 17 febrero 2019
 *@description: cliente angular que interactua con la informacion de los formularios
*/

var app = angular.module('oauthApp', []);

app.controller('oauthCtrl', function($scope, $http) {
	
	//Se envian los datos para el flujo de authorization code
	$scope.enviardatos = function(){		
		var form = document.forms[0];
		var url_getaccesscode = '';
		if(form.catalog.value == 'sandbox'){
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/hackathon/v1/security/oauth-otp/oauth2/authorize';
		}
		else{
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/security/oauth-otp/oauth2/authorize';
		}
		
		var body = {
			client_id:form.client_id.value,
			client_secret:form.client_secret.value,
			catalog:form.catalog.value,
			redirect_uri:form.redirect_uri.value,
			scope:form.scope.value,
			response_type:'code'
		}
		
		$http({
			url: '/guardardatos',
			method: "GET",
			params: body
		})
		.then(function(data) {
			form.action = url_getaccesscode;
			form.submit();
		},
		function(data) {
			console.log('error');
		});
	}
	
	//Se envian los datos para el flujo de aplicacion
	$scope.enviardatosapp = function(){		
		var form = document.forms[0];
		var url_getaccesscode = '';
		if(form.catalog.value == 'sandbox'){
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/hackathon/v1/security/oauth-otp/oauth2/token';
		}
		else{
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/security/oauth-otp/oauth2/token';
		}
		
		var body = {
			client_id:form.client_id.value,
			client_secret:form.client_secret.value,
			catalog:form.catalog.value,
			scope:form.scope.value
		}
		
		$http({
			url: '/guardardatos',
			method: "GET",
			params: body
		})
		.then(function(objresponse) {
			form.action = '/generartokenapp';
			form.submit();
		},
		function(data) {
			console.log('error');
		});
	}
	
	//Se envian los datos para el flujo de password
	$scope.enviardatospass = function(){		
		var form = document.forms[0];
		var url_getaccesscode = '';
		if(form.catalog.value == 'sandbox'){
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/hackathon/v1/security/oauth-otp/oauth2/token';
		}
		else{
			url_getaccesscode = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+form.catalog.value+'/security/oauth-otp/oauth2/token';
		}
		
		var body = {
			username:form.username.value,
			password:form.password.value,
			client_id:form.client_id.value,
			client_secret:form.client_secret.value,
			catalog:form.catalog.value,
			scope:form.scope.value
		}
		
		$http({
			url: '/guardardatos',
			method: "GET",
			params: body
		})
		.then(function(objresponse) {
			form.action = '/generartokenpass';
			form.submit();
		},
		function(data) {
			console.log('error');
		});
	}
});