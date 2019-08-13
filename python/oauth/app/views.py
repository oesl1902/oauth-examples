from django.shortcuts import render, redirect
import requests
import base64

# Función que codifica una cadena de texto en base64
def stringToBase64(data):
    return base64.b64encode(data.encode('utf-8'))

# Función que renderiza la vista index de la aplicación
def oauth(request):
	context = {}
	return render(request, 'app/index.html', context)

# Función que renderiza formulario para el ingreso de datos requeridos por la app
def otpData(request):
	context = {}
	return render(request, 'app/otp-data.html', context)

# Función que redirecciona al otp de oauth bancolombia
def otpForm(request):
	context = {}
	if request.method == 'POST':
		#almacenamiento de datos en variables de sesión
		request.session['client_id'] = request.POST.get('client_id')
		request.session['client_secret'] = request.POST.get('client_secret')
		request.session['catalog'] = request.POST.get('catalog')
		request.session['scope'] = request.POST.get('scope')
		request.session['redirect_uri'] = request.POST.get('redirect_uri')
		# url redirect
		params = '?client_id='+request.session['client_id']+'&response_type=code'+'&scope='+request.session['scope']+'&redirect_uri='+request.session['redirect_uri'];
		if request.session['catalog'] == 'sandbox':
			url = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/authorize'+params
		else:
			url = 'https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/'+request.session['catalog']+'/security/oauth-otp/oauth2/authorize'+params
		return redirect(url)
	return render(request, 'app/oauth.html', context) # crear template de error y renderizarlo

# Función que obtiene el código de autorización de respuesta
def getAuthorizeCode(request):
	context = {}
	code = (request.GET.get('code'))
	context["authorization_code"] = code
	return render(request, 'app/authorize-code.html', context)

# Función que obtiene el token de acceso
def getAccessToken(request):
	context = {}
	if request.method == 'POST':
		if request.session['catalog'] == 'sandbox':
			url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/token"
		else:
			url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/"+request.session['catalog']+"/security/oauth-otp/oauth2/token"
		client = str(stringToBase64(request.session['client_id']+":"+request.session['client_secret']))[2:][:-1]
		# parametrización de headers de la petición
		headers = {
			'accept':'application/json',
			'apim-debug':'true',
			'content-type':'application/x-www-form-urlencoded',
			'authorization':'Basic '+client
		}
		# Definición de parametros por body
		params = {	
			'grant_type': 'authorization_code',
			'code': request.POST.get('authorization-code',''),
			'redirect_uri': request.session['redirect_uri'],
			'scope': request.session['scope']
		}
		print('URL: '+url)
		# ejecución de la petición
		r = requests.post(url, params = params, headers = headers)
		r = r.json()
		context['token_code'] = r['access_token']
	return render(request, 'app/token-code.html', context)