package co.bancolombia.com.oauth.oauth.controller;

import static co.bancolombia.com.oauth.oauth.controller.Config.ACCESS_TOKEN;
import static co.bancolombia.com.oauth.oauth.controller.Config.APPLICATION_JSON;
import static co.bancolombia.com.oauth.oauth.controller.Config.CLIENT_CREDENTIALS;
import static co.bancolombia.com.oauth.oauth.controller.Config.OTP_FORM;
import static co.bancolombia.com.oauth.oauth.controller.Config.PASSWORD;
import static co.bancolombia.com.oauth.oauth.controller.Config.PASSWORD_FORM;
import static co.bancolombia.com.oauth.oauth.controller.Config.TOKEN_CODE;
import static co.bancolombia.com.oauth.oauth.controller.Config.TOKEN_FORM;
import static co.bancolombia.com.oauth.oauth.controller.Config.TRUE;
import static co.bancolombia.com.oauth.oauth.controller.Config.WWW_FORM_URLENCODED;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import javax.net.ssl.HttpsURLConnection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Controller
public class OauthController {

    private Otp otp;

    /**
     * Función que renderiza la vista index de la aplicación
     * 
     * @return a la vista index
     */
    @GetMapping("/")
    public String oaut() {
        return Config.INDEX;
    }

    /**
     * Función que renderiza formulario para el ingreso de datos requeridos por la
     * app
     * 
     * @param modelo contiene los datos que viajan a la vista
     * @return a la vista otp-form
     */
    @GetMapping("/otp-form")
    public String otpForm(Model modelo) {
        modelo.addAttribute(Config.OTP, new Otp()); // Inserta un atributo de tipo Otp para capturar los datos del formulario
        return OTP_FORM;
    }
    
    @GetMapping("/token-form")
    public String tokenForm(Model modelo) {
        modelo.addAttribute(Config.OTP, new Otp()); // Inserta un atributo de tipo Otp para capturar los datos del formulario
        return TOKEN_FORM;
    }

    @GetMapping("/password")
    public String password(Model modelo) {
        modelo.addAttribute(Config.OTP, new Otp()); // Inserta un atributo de tipo Otp para capturar los datos del formulario
        return PASSWORD_FORM;
    }
    
    /**
     * Función que redirecciona al otp de oauth bancolombia
     * 
     * @param otp Permite obtener los daos del formulario en un objeto tipo Otp
     * @return redirección a la vista Otp bancolombia
     */
    @RequestMapping(value = "/otp", method = RequestMethod.POST)
    public String formulario(@ModelAttribute Otp otp) {
        this.otp = otp; // se almacenan los datos de manera global
        String url = Config.OTP_FORM;
        try {
            
            String parameters = "?client_id=" + URLEncoder.encode(this.otp.getClientId(), "UTF-8")
            + "&response_type=code" + "&scope=" + URLEncoder.encode(this.otp.getScope(), "UTF-8")
            + "&redirect_uri=" + URLEncoder.encode(this.otp.getRedirectUri(), "UTF-8");
            
            String p = this.otp.getCatalog();
            
            if (p.equals("sandbox")){
                url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/authorize" + parameters;
            } else {
                url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/" + this.otp.getCatalog()
                + "/security/oauth-otp/oauth2/authorize" + parameters;
            }
            System.out.println(url);
            // Definición de parametros por queryString
            
            
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // ejecución de la petición
        return "redirect:" + url;
    }

    /**
     * Función que obtiene el código de autorización de respuesta
     * 
     * @param code   parametro que se obtiene por queryString de respuesta
     * @param modelo contiene los datos que viajan a la vista
     * @return a la vista authorize-code
     */
    @GetMapping("/redirect")
    public String authorizeCode(@RequestParam(Config.CODE) String code, Model modelo) {
        this.otp.setCode(code);
        modelo.addAttribute(Config.CODE, code); // Envio de code a la vista
        return Config.AUTHORIZE_CODE;
    }

    /**
     * Función que obtiene el token de acceso
     * 
     * @param modelo contiene los datos que viajan a la vista
     * @return a la vista token-code
     * @throws IOException
     */
    @PostMapping("/access-token")
    public String tokenCode(Model modelo) throws IOException {
        String code = this.otp.getCode();
        String url ="";
        
        if (this.otp.getCatalog() == "sandbox") {
            url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/token";
        } else {
            url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/" + this.otp.getCatalog()
            + "/security/oauth-otp/oauth2/token";
        }
         
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
        String BASE64 = base64(otp.getClientId() + ":" + otp.getClientSecret());
        con.setRequestMethod(Config.POST);
        // parametrización de headers de la petición
        con.setRequestProperty(Config.ACCEPT, Config.APPLICATION_JSON);
        con.setRequestProperty(Config.APIM_DEBUG, Config.TRUE);
        con.setRequestProperty(Config.CONTENT_TYPE, Config.WWW_FORM_URLENCODED);
        con.setRequestProperty(Config.AUTHORIZATION, Config.BASIC + " " + BASE64);
        // Definición de parametros por body
        String parameters = "grant_type=authorization_code" + "&code=" + URLEncoder.encode(code, "UTF-8")
                + "&redirect_uri=" + URLEncoder.encode(this.otp.getRedirectUri(), "UTF-8") + "&scope="
                + URLEncoder.encode(this.otp.getScope(), "UTF-8");
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        // Escritura de parametros en la peticion
        wr.writeBytes(parameters);
        wr.flush();
        wr.close();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        // leyendo la respuesta
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        // Obteniendo el token
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(response.toString()).getAsJsonObject();
        String token = object.get(Config.ACCESS_TOKEN).getAsString();
        modelo.addAttribute(Config.TOKEN, token); // Envio de token a la vista
        return Config.TOKEN_CODE;
    }
    
    @PostMapping("/token-credential")
    public ModelAndView tokenCredential(@ModelAttribute Otp otp) throws IOException {
      this.otp = otp; // se almacenan los datos de manera global
   	  String url ="";
       
              if (this.otp.getCatalog() == "sandbox") {
                  url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/token";
              } else {
                  url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/" + this.otp.getCatalog()
                  + "/security/oauth-otp/oauth2/token";
              }
               
              URL obj = new URL(url);
              ModelAndView m = new ModelAndView(Config.TOKEN_CODE);
              
              try {
              HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

              con.setRequestMethod(Config.POST);
              
              // parametrización de headers de la petición
              con.setRequestProperty(Config.ACCEPT, APPLICATION_JSON);
              con.setRequestProperty(Config.APIM_DEBUG, TRUE);
              con.setRequestProperty(Config.CONTENT_TYPE, WWW_FORM_URLENCODED);
              
              // Definición de parametros por body
              String parameters = "grant_type=" + CLIENT_CREDENTIALS 
              + "&client_id=" + otp.getClientId()
              + "&client_secret=" + otp.getClientSecret() 
              + "&scope=" + URLEncoder.encode(this.otp.getScope(), "UTF-8");
              con.setDoOutput(true);
              DataOutputStream wr = new DataOutputStream(con.getOutputStream());
              // Escritura de parametros en la peticion
              wr.writeBytes(parameters);
              wr.flush();
              wr.close();
              BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
              String inputLine;
              // leyendo la respuesta
              StringBuffer response = new StringBuffer();
              while ((inputLine = in.readLine()) != null) {
                  response.append(inputLine);
              }
              in.close();
              // Obteniendo el token
              JsonParser parser = new JsonParser();
              JsonObject object = parser.parse(response.toString()).getAsJsonObject();
              String token = object.get(Config.ACCESS_TOKEN).getAsString();

              m.addObject("token", token);
		} catch (Exception e) {
			 e.printStackTrace();
		}
 
          return m;
    }
    
    @PostMapping("/password-form")
    public ModelAndView password(@ModelAttribute Otp otp) throws IOException {
      this.otp = otp; // se almacenan los datos de manera global
   	  String url ="";
       
              if (this.otp.getCatalog() == "sandbox") {
                  url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/sandbox/hackathon/v1/security/oauth-otp/oauth2/token";
              } else {
                  url = "https://api.us.apiconnect.ibmcloud.com/bancolombiabluemix-dev/" + this.otp.getCatalog()
                  + "/security/oauth-otp/oauth2/token";
              }
              URL obj = new URL(url);
              ModelAndView m = new ModelAndView(Config.TOKEN_CODE);
              
              try {
                  HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
                  con.setRequestMethod(Config.POST);
                  
                  // parametrización de headers de la petición
                  con.setRequestProperty(Config.ACCEPT, APPLICATION_JSON);
                  con.setRequestProperty(Config.APIM_DEBUG, TRUE);
                  con.setRequestProperty(Config.CONTENT_TYPE, WWW_FORM_URLENCODED);
                  
                  // Definición de parametros por body
                  String parameters = "grant_type=" + PASSWORD 
                  + "&username=" + otp.getOwnerName()
                  + "&password=" + otp.getOwnerPassword() 
                  + "&client_id=" + otp.getClientId()
                  + "&client_secret=" + otp.getClientSecret() 
                  + "&scope=" + URLEncoder.encode(this.otp.getScope(), "UTF-8");
                  con.setDoOutput(true);
                  DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                  
                  // Escritura de parametros en la peticion
                  wr.writeBytes(parameters);
                  wr.flush();
                  wr.close();
                  BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                  String inputLine;
                  
                  // leyendo la respuesta
                  StringBuffer response = new StringBuffer();
                  while ((inputLine = in.readLine()) != null) {
                      response.append(inputLine);
                  }
                  in.close();
                  // Obteniendo el token
                  JsonParser parser = new JsonParser();
                  JsonObject object = parser.parse(response.toString()).getAsJsonObject();
                  String token = object.get(Config.ACCESS_TOKEN).getAsString();

                  m.addObject("token", token);
    		} catch (Exception e) {
    			 e.printStackTrace();
    		}
              
          return m;
    }
    

    /**
     * Función que codifica una cadena en base64
     * 
     * @param s cadena a codificar
     * @return cadena en codificación base64
     */
    public String base64(String s) {
        return Base64.getEncoder().withoutPadding().encodeToString(s.getBytes());
    }
}