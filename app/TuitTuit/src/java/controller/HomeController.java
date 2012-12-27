/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import twitter.twitteroauth.twitterresponse.StatusType;
import twitter.twitteroauth.twitterresponse.Statuses;

/**
 *
 * @author Antonio
 */
@ManagedBean
@SessionScoped
public class HomeController implements Serializable {

    private String _PIN;
    private String _UsuarioNombre;
    private String _Status;

    public String getStatus() {
        return _Status;
    }

    public void setStatus(String _Status) {
        this._Status = _Status;
    }

    public String getUsuarioNombre() {
        return _UsuarioNombre;
    }

    public void setUsuarioNombre(String _UsuarioNombre) {
        this._UsuarioNombre = _UsuarioNombre;
    }
    private DataModel DMReporte = null;

    public DataModel getDMReporte() {
        if (client != null && client._Activo) {
            Statuses statuses = client.getFriendsTimeline(Statuses.class, null, null, null, "10");
            DMReporte = new ListDataModel(statuses.getStatus());
        }
        return DMReporte;
    }

    public void setDMReporte(DataModel DMReporte) {
        this.DMReporte = DMReporte;
    }

    public String getPIN() {
        return _PIN;
    }

    public void setPIN(String _PIN) {
        this._PIN = _PIN;
    }
    private Twitter_OAuth_id___format_JerseyClient client;

    public void solicitar() throws MalformedURLException, IOException {
        client = new Twitter_OAuth_id___format_JerseyClient();
        client.solicitarAutentificacion(new Form());


    }

    public void confirmar() throws IOException {
        HttpSession sesion = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        Form requestTokenResponse = (Form) sesion.getAttribute("requestTokenResponse");
        if (requestTokenResponse != null) {
            client.confirmarAutenteficacion(requestTokenResponse, getPIN());
            client.initOAuth();
            Statuses statuses = client.getFriendsTimeline(Statuses.class, null, null, null, "10");
            Statuses Usuario = client.getUserTimeline(Statuses.class, null, null, null, "1");
            StatusType st = Usuario.getStatus().get(0);
            setUsuarioNombre(st.getUser().getScreenName());
            DMReporte = new ListDataModel(statuses.getStatus());
        } else {
            client = new Twitter_OAuth_id___format_JerseyClient();
            client.solicitarAutentificacion(new Form());
        }

    }

    public void actualizarStatus() throws UnsupportedEncodingException {
        String rawStatus = getStatus();
        //String status = URLEncoder.encode(rawStatus, "UTF-8");
        client.makeOAuthRequestUnique();
        try {
            client.updateStatus(String.class, rawStatus, null);
        } catch (UniformInterfaceException ex) {
            System.out.println("Exception when calling updateStatus = " + ex.getResponse().getEntity(String.class));
        }
        Statuses statuses = client.getFriendsTimeline(Statuses.class, null, null, null, "10");
        DMReporte = new ListDataModel(statuses.getStatus());

    }

    public HomeController() {
    }

    static class Twitter_OAuth_id___format_JerseyClient {

        private WebResource webResource;
        private Client client;
        private static final String BASE_URI = "https://api.twitter.com/1";

        public Twitter_OAuth_id___format_JerseyClient() {
            com.sun.jersey.api.client.config.ClientConfig config = new com.sun.jersey.api.client.config.DefaultClientConfig();
            client = Client.create(config);
            String resourcePath = "statuses";
            webResource = client.resource(BASE_URI).path(resourcePath);

        }
        private static final String OAUTH_BASE_URL = "http://twitter.com/oauth";
        private static final String CONSUMER_KEY = ""; //Aqui pones tu Consumer Key de tu app registrada en Twitter
        private static final String CONSUMER_SECRET = "";//Aqui pones tu Consumer SECRET Key de tu app registrada en Twitter
        private OAuthParameters oauth_params;
        private OAuthSecrets oauth_secrets;
        private OAuthClientFilter oauth_filter;
        private String oauth_access_token;
        private String oauth_access_token_secret;
        public boolean _Activo = false;

        public <T> T getUserTimeline(Class<T> responseType, String since, String since_id, String page, String count) throws UniformInterfaceException {
            String[] queryParamNames = new String[]{"since", "since_id", "page", "count"};
            String[] queryParamValues = new String[]{since, since_id, page, count};
            return webResource.path("user_timeline.xml").queryParams(getQueryOrFormParams(queryParamNames, queryParamValues)).accept(javax.ws.rs.core.MediaType.TEXT_XML).get(responseType);
        }

        public <T> T getFriendsTimeline(Class<T> responseType, String since, String since_id, String page, String count) throws UniformInterfaceException {
            String[] queryParamNames = new String[]{"since", "since_id", "page", "count"};
            String[] queryParamValues = new String[]{since, since_id, page, count};
            return webResource.path("friends_timeline.xml").queryParams(getQueryOrFormParams(queryParamNames, queryParamValues)).accept(javax.ws.rs.core.MediaType.TEXT_XML).get(responseType);
        }

        public <T> T getPublicTimeline(Class<T> responseType) throws UniformInterfaceException {
            return webResource.path("public_timeline.xml").accept(javax.ws.rs.core.MediaType.TEXT_XML).get(responseType);
        }

        public <T> T updateStatus(Class<T> responseType, String status, String in_reply_to_status_id) throws UniformInterfaceException {
            String[] formParamNames = new String[]{"status", "in_reply_to_status_id"};
            String[] formParamValues = new String[]{status, in_reply_to_status_id};
            return webResource.path("update.xml").type(javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED).post(responseType, getQueryOrFormParams(formParamNames, formParamValues));
        }

        private MultivaluedMap getQueryOrFormParams(String[] paramNames, String[] paramValues) {
            MultivaluedMap<String, String> qParams = new com.sun.jersey.api.representation.Form();
            for (int i = 0; i < paramNames.length; i++) {
                if (paramValues[i] != null) {
                    qParams.add(paramNames[i], paramValues[i]);
                }
            }
            return qParams;
        }

        public void close() {
            client.destroy();
        }

        private Form getOAuthAccessToken(Form requestTokenResponse, String oauth_verifier) throws UniformInterfaceException {
            WebResource resource = client.resource(OAUTH_BASE_URL).path("access_token");
            oauth_params.token(requestTokenResponse.getFirst("oauth_token")).signatureMethod(com.sun.jersey.oauth.signature.HMAC_SHA1.NAME).version("1.0").nonce().timestamp().verifier(oauth_verifier);
            oauth_secrets.tokenSecret(requestTokenResponse.getFirst("oauth_token_secret"));
            resource.addFilter(oauth_filter);
            return resource.get(Form.class);
        }

        public void initOAuth() {
            oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY).token(oauth_access_token).signatureMethod(com.sun.jersey.oauth.signature.HMAC_SHA1.NAME).version("1.0").nonce().timestamp();
            oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET).tokenSecret(oauth_access_token_secret);
            oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);
            webResource.addFilter(oauth_filter);
        }

        public void makeOAuthRequestUnique() {
            if (oauth_params != null) {
                oauth_params.nonce().timestamp();
            }
        }

        private void sendAuthorizeConsumer(Form requestTokenResponse) throws IOException {
            try {
                HttpServletResponse resp = (HttpServletResponse) (FacesContext.getCurrentInstance().getExternalContext().getResponse());
                resp.sendRedirect(new java.net.URI("http://twitter.com/oauth/authorize?oauth_token=" + requestTokenResponse.getFirst("oauth_token")).toString());
            } catch (java.net.URISyntaxException ex) {
                System.out.println("Exception when authorizing consumer: " + ex);
            }
        }

        private void solicitarAutentificacion(Form form) throws IOException {
            form = getOAuthRequestToken();
            sendAuthorizeConsumer(form);
            HttpSession sesion = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
            sesion.setAttribute("requestTokenResponse", form);
        }

        private Form getOAuthRequestToken() throws UniformInterfaceException {
            WebResource resource = client.resource(OAUTH_BASE_URL).path("request_token");
            oauth_params = new OAuthParameters().consumerKey(CONSUMER_KEY).signatureMethod(com.sun.jersey.oauth.signature.HMAC_SHA1.NAME).version("1.0").nonce().timestamp();
            oauth_secrets = new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
            oauth_filter = new OAuthClientFilter(client.getProviders(), oauth_params, oauth_secrets);
            resource.addFilter(oauth_filter);
            return resource.get(Form.class);
        }

        private void confirmarAutenteficacion(Form requestTokenResponse, String piN) {
            Form accessTokenResponse = getOAuthAccessToken(requestTokenResponse, piN);
            oauth_access_token_secret = accessTokenResponse.getFirst("oauth_token_secret");
            oauth_access_token = accessTokenResponse.getFirst("oauth_token");
            _Activo = true;
        }
    }
}
